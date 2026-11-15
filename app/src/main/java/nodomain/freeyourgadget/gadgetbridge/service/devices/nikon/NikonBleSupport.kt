package nodomain.freeyourgadget.gadgetbridge.service.devices.nikon

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.content.edit
import nodomain.freeyourgadget.gadgetbridge.devices.nikon.AbstractNikonCoordinator.Companion.PREF_CLASSIC_ADDRESS
import nodomain.freeyourgadget.gadgetbridge.devices.nikon.AbstractNikonCoordinator.Companion.PREF_IDENTIFIER
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLESingleDeviceSupport
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder
import nodomain.freeyourgadget.gadgetbridge.service.gallery.GallerySourceRegistry
import nodomain.freeyourgadget.gadgetbridge.util.GB
import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.Calendar
import java.util.UUID

@SuppressLint("MissingPermission")
class NikonBleSupport : AbstractBTLESingleDeviceSupport(LOG) {
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var gallerySource: NikonGallerySource

    // Auth
    private val random = SecureRandom()
    private val localNonce = ByteArray(8)

    // Classic discovery
    private var classicReceiverRegistered = false
    private var discoveryAttempts = 0
    private var bondingAddress: String? = null
    private val classicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if (bondingAddress != null) {
                        return
                    }
                    val device = IntentCompat.getParcelableExtra(
                        intent,
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    ) ?: return
                    // The camera pads its name with spaces
                    val name = (intent.getStringExtra(BluetoothDevice.EXTRA_NAME) ?: device.name)?.trim()
                    LOG.debug("Discovered device {} ({}), type {}", device.address, name, device.type)
                    // The discovery also reports the LE advertisement, with the same name
                    if (name == classicName() && device.type != BluetoothDevice.DEVICE_TYPE_LE) {
                        bondClassic(device)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (bondingAddress != null) {
                        return
                    }
                    if (discoveryAttempts < MAX_DISCOVERY_ATTEMPTS && btAdapter.startDiscovery()) {
                        discoveryAttempts++
                        LOG.debug(
                            "Classic discovery attempt {} finished without finding {}",
                            discoveryAttempts,
                            classicName(),
                        )
                        return
                    }
                    failClassicBond("Classic discovery failed to find ${classicName()}")
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = IntentCompat.getParcelableExtra(
                        intent,
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                    if (device == null || device.address != bondingAddress) {
                        return
                    }
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    LOG.debug("Bond state of {} changed to {}", device.address, state)
                    when (state) {
                        BluetoothDevice.BOND_BONDED -> {
                            stopClassicBond()
                            saveClassicAddress(device.address)
                            setInitialized()
                        }

                        BluetoothDevice.BOND_NONE -> failClassicBond("Bond with ${device.address} failed")
                    }
                }
            }
        }
    }

    init {
        addSupportedService(UUID_SERVICE_NIKON)
    }

    override fun setContext(gbDevice: GBDevice, btAdapter: BluetoothAdapter, context: Context) {
        super.setContext(gbDevice, btAdapter, context)
        this.btAdapter = btAdapter
        this.gallerySource = NikonGallerySource()
        GallerySourceRegistry.register(gbDevice.address, gallerySource)
    }

    override fun dispose() {
        synchronized(ConnectionMonitor) {
            stopClassicBond()
            GallerySourceRegistry.unregister(gbDevice.address, gallerySource)
            super.dispose()
        }
    }

    override fun onSetTime() {
        createTransactionBuilder("set time")
            .write(UUID_CHARACTERISTIC_CURRENT_TIME, *encodeTimeBytes())
            .queue()
    }

    override fun useAutoConnect(): Boolean {
        return false
    }

    override fun initializeDevice(builder: TransactionBuilder): TransactionBuilder {
        builder.setDeviceState(GBDevice.State.INITIALIZING)

        random.nextBytes(localNonce)

        builder.notify(UUID_CHARACTERISTIC_AUTHENTICATION, true)
        builder.notify(UUID_CHARACTERISTIC_CONTROL_POINT, true)
        builder.setDeviceState(GBDevice.State.AUTHENTICATING)
        builder.write(
            UUID_CHARACTERISTIC_AUTHENTICATION,
            *(byteArrayOf(AUTH_STAGE_1) + localNonce + getOrCreateIdentifier())
        )

        return builder
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
    ): Boolean {
        when (characteristic.uuid) {
            UUID_CHARACTERISTIC_AUTHENTICATION -> {
                handleAuthMessage(value)
                return true
            }

            UUID_CHARACTERISTIC_CONTROL_POINT -> {
                handleControlPoint(value)
                return true
            }
        }

        return super.onCharacteristicChanged(gatt, characteristic, value)
    }

    private fun handleAuthMessage(value: ByteArray) {
        LOG.debug("Got auth message: {}", GB.hexdump(value))

        if (value.size != 17) {
            LOG.error("Unexpected auth message length {}", value.size)
            return
        }

        val nonce = value.copyOfRange(1, 9)
        val payload = value.copyOfRange(9, 17)

        when (value[0]) {
            AUTH_STAGE_2 -> {
                val reply = NikonCrypto.computeReply(localNonce, nonce, payload)
                if (reply == null) {
                    // TODO disconnect?
                    reportPairingError("Failed to verify the hash sent by the camera", false)
                    LOG.error("Failed to verify the hash sent by the camera")
                    return
                }
                createTransactionBuilder("auth stage 3")
                    .write(UUID_CHARACTERISTIC_AUTHENTICATION, *(byteArrayOf(AUTH_STAGE_3) + localNonce + reply))
                    .queue()
            }

            AUTH_STAGE_4 -> {
                LOG.info("Auth stage 4: nonce={}, payload={}", nonce.toHexString(), payload.toHexString())
            }

            else -> LOG.warn("Unknown auth stage {}", value[0])
        }
    }

    private fun handleControlPoint(value: ByteArray) {
        LOG.debug("Got control point: {}", GB.hexdump(value))

        if (value.size != 2 || value[0] != CONTROL_POINT_REQUEST) {
            LOG.warn("Unexpected control point value")
            return
        }

        createTransactionBuilder("control point ack")
            .write(UUID_CHARACTERISTIC_CONTROL_POINT, CONTROL_POINT_ACK, value[1])
            .write(UUID_CHARACTERISTIC_CLIENT_NAME, *"Gadgetbridge".toByteArray().copyOf(CLIENT_NAME_LENGTH))
            .write(UUID_CHARACTERISTIC_CURRENT_TIME, *encodeTimeBytes())
            .queue()

        startClassicBond()
    }

    private fun setInitialized() {
        createTransactionBuilder("initialized")
            .setDeviceState(GBDevice.State.INITIALIZED)
            .queue()
    }

    private fun encodeTimeBytes(): ByteArray {
        val now = Calendar.getInstance()
        val rawOffsetMinutes = now.get(Calendar.ZONE_OFFSET) / 60_000
        val dstQuarterHours = now.get(Calendar.DST_OFFSET) / 900_000
        val year = now.get(Calendar.YEAR)
        return byteArrayOf(
            year.toByte(),
            (year shr 8).toByte(),
            (now.get(Calendar.MONTH) + 1).toByte(),
            now.get(Calendar.DAY_OF_MONTH).toByte(),
            now.get(Calendar.HOUR_OF_DAY).toByte(),
            now.get(Calendar.MINUTE).toByte(),
            now.get(Calendar.SECOND).toByte(),
            dstQuarterHours.toByte(),
            (rawOffsetMinutes / 60).toByte(),
            (rawOffsetMinutes % 60).toByte(),
        )
    }

    /**
     * The camera will only add the phone to its list of paired devices after the Classic bond. The Classic address is
     * not the LE address, so the camera is found by its name. The device is only marked as initialized when the bond
     * exists.
     */
    private fun startClassicBond() {
        val storedAddress = devicePrefs.getString(PREF_CLASSIC_ADDRESS, null)
        val bonded = btAdapter.bondedDevices.firstOrNull {
            it.address == storedAddress || it.name?.trim() == classicName()
        }
        if (bonded != null) {
            LOG.debug("Bond for classic device {} already exists", bonded.address)
            saveClassicAddress(bonded.address)
            setInitialized()
            return
        }

        if (classicReceiverRegistered) {
            return
        }
        LOG.info("Starting Classic discovery for {}", classicName())
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        ContextCompat.registerReceiver(context, classicReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
        classicReceiverRegistered = true
        bondingAddress = null
        discoveryAttempts = 1
        if (!btAdapter.startDiscovery()) {
            failClassicBond("Failed to start Classic discovery")
        }
    }

    private fun stopClassicBond() {
        if (!classicReceiverRegistered) {
            return
        }
        classicReceiverRegistered = false
        bondingAddress = null
        context.unregisterReceiver(classicReceiver)
        btAdapter.cancelDiscovery()
    }

    private fun failClassicBond(reason: String) {
        LOG.error("Classic bond failed: {}", reason)
        stopClassicBond()
        disconnect()
    }

    private fun bondClassic(device: BluetoothDevice) {
        LOG.info("Bonding with classic device {}", device.address)
        bondingAddress = device.address
        btAdapter.cancelDiscovery()
        if (!device.createBond()) {
            failClassicBond("Failed to start the bond with ${device.address}")
        }
    }

    private fun saveClassicAddress(address: String) {
        LOG.debug("Saving classic address {}", address)
        devicePrefs.preferences.edit {
            putString(PREF_CLASSIC_ADDRESS, address)
        }
    }

    /**
     * The Classic name is the same as the LE name.
     */
    private fun classicName(): String = gbDevice.name.trim()

    /**
     * The phone's identifier in stage 1. After pairing, the camera advertises the first 4 bytes.
     */
    private fun getOrCreateIdentifier(): ByteArray {
        val existing = devicePrefs.getString(PREF_IDENTIFIER, null)
        if (existing != null && existing.length == 16) {
            return existing.hexToByteArray()
        }

        val identifier = ByteArray(8)
        random.nextBytes(identifier)
        devicePrefs.preferences.edit {
            putString(PREF_IDENTIFIER, identifier.toHexString())
        }
        return identifier
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(NikonBleSupport::class.java)

        private val UUID_SERVICE_NIKON: UUID = UUID.fromString("0000de00-3dd4-4255-8d62-6dc7b9bd5561")
        private val UUID_CHARACTERISTIC_AUTHENTICATION: UUID = UUID.fromString("00002000-3dd4-4255-8d62-6dc7b9bd5561")
        private val UUID_CHARACTERISTIC_CLIENT_NAME: UUID = UUID.fromString("00002002-3dd4-4255-8d62-6dc7b9bd5561")
        private val UUID_CHARACTERISTIC_CURRENT_TIME: UUID = UUID.fromString("00002006-3dd4-4255-8d62-6dc7b9bd5561")
        private val UUID_CHARACTERISTIC_CONTROL_POINT: UUID = UUID.fromString("00002008-3dd4-4255-8d62-6dc7b9bd5561")

        private const val AUTH_STAGE_1: Byte = 0x01
        private const val AUTH_STAGE_2: Byte = 0x02
        private const val AUTH_STAGE_3: Byte = 0x03
        private const val AUTH_STAGE_4: Byte = 0x04

        private const val CONTROL_POINT_REQUEST: Byte = 0x01
        private const val CONTROL_POINT_ACK: Byte = 0x00
        private const val CLIENT_NAME_LENGTH = 32
        private const val MAX_DISCOVERY_ATTEMPTS = 5
    }
}
