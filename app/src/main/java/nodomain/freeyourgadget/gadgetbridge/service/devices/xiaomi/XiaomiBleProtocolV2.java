package nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi;

import static nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport.calcMaxWriteChunk;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSppPacketV2.PACKET_PREAMBLE;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSppPacketV2.PACKET_TYPE_ACK;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSppPacketV2.PACKET_TYPE_DATA;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSppPacketV2.PACKET_TYPE_NACK;
import static nodomain.freeyourgadget.gadgetbridge.service.devices.xiaomi.XiaomiSppPacketV2.PACKET_TYPE_SESSION_CONFIG;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventUpdateDeviceInfo;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.proto.xiaomi.XiaomiProto;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLESingleDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.util.GB;

/** @noinspection LoggingSimilarMessage*/
public class XiaomiBleProtocolV2 extends AbstractXiaomiBleProtocol {
    private static final Logger LOG = LoggerFactory.getLogger(XiaomiBleProtocolV2.class);

    /// Used until the device announces its own window in the session config.
    static final int DEFAULT_TX_WINDOW = XiaomiSppV2SendWindow.MAX_TRACKED_FRAMES;
    /// Used until the device announces its own send timeout in the session config.
    static final long DEFAULT_RESEND_TIMEOUT_MILLIS = 10_000L;

    private final XiaomiSupport xiaomiSupport;
    private final AbstractBTLESingleDeviceSupport commsSupport;
    private final GBDevice mGbDevice;

    private BluetoothGattCharacteristic btCharacteristicRead;
    private BluetoothGattCharacteristic btCharacteristicWrite;

    private int maxWriteSize = 244; // MTU of 247 - 3 bytes for the ATT overhead

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final Map<XiaomiChannelHandler.Channel, XiaomiChannelHandler> mChannelHandlers = new HashMap<>();

    /// Data packets written to the device and not yet acked, and the sequence numbers.
    private final XiaomiSppV2SendWindow sendWindow = new XiaomiSppV2SendWindow();
    /// Data packets that are encoded but wait for room in the device's window, oldest first.
    private final ArrayDeque<PendingFrame> pendingFrames = new ArrayDeque<>();
    /// Guards {@link #pendingFrames}, {@link #txWindow} and the decision to write or hold a frame.
    private final Object sendLock = new Object();
    private int txWindow = DEFAULT_TX_WINDOW;
    private long resendTimeoutMillis = DEFAULT_RESEND_TIMEOUT_MILLIS;
    /// Sequence number a nack-triggered resend started from, -1 if none since the last ack progress.
    private int resendFromSequenceNumber = -1;

    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeoutRunnable = this::onResendTimeout;
    private boolean timeoutPending = false;
    private int timeoutsWithoutAck = 0;

    private static final class PendingFrame {
        final String taskName;
        final int sequenceNumber;
        final byte[] bytes;

        PendingFrame(final String taskName, final int sequenceNumber, final byte[] bytes) {
            this.taskName = taskName;
            this.sequenceNumber = sequenceNumber;
            this.bytes = bytes;
        }
    }

    public XiaomiBleProtocolV2(final XiaomiBleSupport xiaomiBleSupport) {
        this.xiaomiSupport = xiaomiBleSupport.getXiaomiSupport();
        this.commsSupport = xiaomiBleSupport.getCommsSupport();
        this.mGbDevice = xiaomiSupport.getDevice();

        mChannelHandlers.put(XiaomiChannelHandler.Channel.Version, this::handleVersionPacket);
        mChannelHandlers.put(XiaomiChannelHandler.Channel.ProtobufCommand, this.xiaomiSupport::handleCommandBytes);
        mChannelHandlers.put(XiaomiChannelHandler.Channel.Activity, this.xiaomiSupport.getHealthService().getActivityFetcher()::addChunk);
    }

    @Override
    public boolean initializeDevice(final TransactionBuilder builder) {
        reset();

        btCharacteristicRead = commsSupport.getCharacteristic(XiaomiUuids.BLE_V2_CHARACTERISTIC_RX_UUID);
        btCharacteristicWrite = commsSupport.getCharacteristic(XiaomiUuids.BLE_V2_CHARACTERISTIC_TX_UUID);

        if (btCharacteristicRead == null || btCharacteristicWrite == null) {
            return false;
        }

        // FIXME unsetDynamicState unsets the fw version, which causes problems..
        if (mGbDevice.getFirmwareVersion() == null) {
            mGbDevice.setFirmwareVersion(xiaomiSupport.getCachedFirmwareVersion() != null ?
                    xiaomiSupport.getCachedFirmwareVersion() :
                    "N/A");
        }

        // request highest possible MTU; device should response with the highest supported MTU anyway
        builder.requestMtu(512);
        builder.setDeviceState(GBDevice.State.INITIALIZING);
        builder.notify(btCharacteristicRead, true);
        builder.setDeviceState(GBDevice.State.AUTHENTICATING);

        writeChunks(
                builder,
                XiaomiSppPacketV2.newSessionConfigPacketBuilder()
                        .setOpCode(XiaomiSppPacketV2.SessionConfigPacket.OPCODE_START_SESSION_REQUEST)
                        .setSequenceNumber(0)
                        .build()
                        .encode(null)
        );

        return true;
    }

    @Override
    public void reset() {
        cancelResendTimeout();
        synchronized (sendLock) {
            sendWindow.clear();
            pendingFrames.clear();
            txWindow = DEFAULT_TX_WINDOW;
            resendTimeoutMillis = DEFAULT_RESEND_TIMEOUT_MILLIS;
            resendFromSequenceNumber = -1;
        }
        buffer.reset();
    }

    @Override
    public void dispose() {
        cancelResendTimeout();
    }

    @Override
    public boolean onCharacteristicChanged(final BluetoothGatt gatt,
                                           final BluetoothGattCharacteristic characteristic,
                                           final byte[] value) {
        final UUID characteristicUUID = characteristic.getUuid();

        if (btCharacteristicRead.getUuid().equals(characteristicUUID)) {
            try {
                buffer.write(value);
            } catch (IOException ex) {
                LOG.error("Exception while writing buffer: ", ex);
            }

            processBuffer();
            return true;
        }

        return false;
    }

    private void skipBuffer(int newStart) {
        final byte[] bufferState = buffer.toByteArray();
        buffer.reset();

        if (newStart < 0) {
            newStart = bufferState.length;
        }

        if (newStart >= bufferState.length) {
            return;
        }

        buffer.write(bufferState, newStart, bufferState.length - newStart);
    }

    private void processBuffer() {
        boolean shouldProcess = true;
        while (shouldProcess) {
            final byte[] bufferState = buffer.toByteArray();
            final AbstractXiaomiSppProtocol.ParseResult parseResult = processPacket(bufferState);
            LOG.debug("processBuffer(): protocol.processPacket() returned status {}", parseResult.status);
            int skipBytes;

            switch (parseResult.status) {
                case Incomplete:
                    skipBytes = 0;
                    shouldProcess = false;
                    break;
                case Complete:
                    skipBytes = parseResult.packetSize;
                    break;
                case Invalid:
                    skipBytes = findNextPacketOffset(bufferState);
                    if (skipBytes < 0) {
                        skipBytes = bufferState.length;
                    }
                    break;
                default:
                    throw new IllegalStateException(String.format("Unhandled parse state %s", parseResult.status));
            }

            if (skipBytes > 0) {
                LOG.debug("processBuffer(): skipping {} bytes for state {}", skipBytes, parseResult.status);
                skipBuffer(skipBytes);
            }
        }
    }

    protected void onPacketReceived(final XiaomiChannelHandler.Channel channel, final byte[] payload) {
        final XiaomiChannelHandler handler = mChannelHandlers.get(channel);
        if (handler != null) {
            handler.handle(payload);
        } else {
            LOG.warn("Unhandled SppPacket on channel {}", channel);
        }
    }

    @Override
    public void onMtuChanged(final BluetoothGatt gatt, final int mtu, final int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            return;
        }
        this.maxWriteSize = calcMaxWriteChunk(mtu);
    }

    @Override
    public void onAuthSuccess() {

    }

    @Override
    public void sendCommand(final String taskName, final XiaomiProto.Command command) {
        if (this.btCharacteristicWrite == null) {
            // Can sometimes happen in race conditions when connecting + receiving calendar event or weather updates
            LOG.warn("Unable to send command for {} - btCharacteristicWrite is null!", taskName);
            return;
        }

        try {
            final TransactionBuilder builder = this.commsSupport.createTransactionBuilder("send " + taskName);
            sendCommand(builder, command);
            builder.queue();
        } catch (final Exception ex) {
            LOG.error("Caught unexpected exception while sending command, device may not have been informed!", ex);
        }
    }

    @Override
    public void sendCommand(final TransactionBuilder builder, final XiaomiProto.Command command) {
        if (this.btCharacteristicWrite == null) {
            // Can sometimes happen in race conditions when connecting + receiving calendar event or weather updates
            LOG.warn("Unable to send command for {} - btCharacteristicWrite is null!", builder.getTaskName());
            return;
        }

        final byte[] commandBytes = command.toByteArray();
        LOG.debug("sendCommand(): encoded command for task '{}': {}", builder.getTaskName(), GB.lazyHexdump(commandBytes));
        if (command.getType() == XiaomiAuthService.COMMAND_TYPE) {
            sendDataPacket(builder, XiaomiChannelHandler.Channel.Authentication, commandBytes);
        } else {
            sendDataPacket(builder, XiaomiChannelHandler.Channel.ProtobufCommand, commandBytes);
        }
        // do not queue here, that's the job of the caller
    }

    @Override
    public void sendDataChunk(final String taskName, final byte[] chunk, @Nullable final XiaomiSendCallback callback) {
        LOG.debug("sendDataChunk(): encoded data chunk for task '{}': {}", taskName, GB.lazyHexdump(chunk));
        final TransactionBuilder builder = this.commsSupport.createTransactionBuilder("send " + taskName);
        sendDataPacket(builder, XiaomiChannelHandler.Channel.Data, chunk);
        builder.queue();

        if (callback != null) {
            // callback puts a SetProgressAction onto the queue
            callback.onSend();
        }
    }

    private void handleVersionPacket(final byte[] payloadBytes) {
        if (payloadBytes != null && payloadBytes.length > 0) {
            LOG.debug("Received SPP protocol version: {}", GB.hexdump(payloadBytes));

            // show in details
            final GBDeviceEventUpdateDeviceInfo event = new GBDeviceEventUpdateDeviceInfo("SPP_PROTOCOL: ", GB.hexdump(payloadBytes));
            xiaomiSupport.evaluateGBDeviceEvent(event);

            // TODO handle different protocol versions
            // Right now, we expect 3
        }

        xiaomiSupport.getAuthService().startEncryptedHandshake();
    }

    private void handleSessionConfig(final XiaomiSppPacketV2.SessionConfigPacket sessionConfig) {
        LOG.info("Received session config, opcode={}", sessionConfig.getOpCode());

        synchronized (sendLock) {
            final int deviceTxWindow = sessionConfig.getTxWindow();
            if (deviceTxWindow > 0) {
                txWindow = Math.min(deviceTxWindow, XiaomiSppV2SendWindow.MAX_TRACKED_FRAMES);
            }
            final int deviceSendTimeout = sessionConfig.getSendTimeoutMillis();
            if (deviceSendTimeout > 0) {
                resendTimeoutMillis = deviceSendTimeout;
            }
            LOG.debug("Using tx window {} and resend timeout {} ms", txWindow, resendTimeoutMillis);
        }

        xiaomiSupport.getAuthService().startEncryptedHandshake();
    }

    private void sendAck(final int sequenceNumber) {
        final TransactionBuilder builder = commsSupport.createTransactionBuilder(String.format(Locale.ROOT, "send ack for %d", sequenceNumber));
        writeChunks(builder, new XiaomiSppPacketV2.AckPacket.Builder()
                .setSequenceNumber(sequenceNumber)
                .build()
                .encode(null));
        builder.queue();
    }

    public int findNextPacketOffset(byte[] buffer) {
        for (int i = 1; i < buffer.length; i++) {
            if (buffer[i] == PACKET_PREAMBLE[0])
                return i;
        }

        return -1;
    }

    public AbstractXiaomiSppProtocol.ParseResult processPacket(byte[] rxBuf) {
        if (rxBuf.length < 8) {
            LOG.debug("processPacket(): not enough bytes in buffer to process packet (got {} of required {} bytes)",
                    rxBuf.length,
                    8);
            return new AbstractXiaomiSppProtocol.ParseResult(AbstractXiaomiSppProtocol.ParseResult.Status.Incomplete);
        }

        final ByteBuffer buffer = ByteBuffer.wrap(rxBuf).order(ByteOrder.LITTLE_ENDIAN);
        final byte[] headerMagic = new byte[PACKET_PREAMBLE.length];
        buffer.get(headerMagic);

        if (!Arrays.equals(PACKET_PREAMBLE, headerMagic)) {
            LOG.warn("processPacket(): invalid header magic (expected {}, got {})",
                    GB.hexdump(PACKET_PREAMBLE),
                    GB.hexdump(headerMagic));
            return new AbstractXiaomiSppProtocol.ParseResult(AbstractXiaomiSppProtocol.ParseResult.Status.Invalid);
        }

        buffer.get(); // flags and packet type
        buffer.get(); // packet sequence number
        final int packetSize = 8 + (buffer.getShort() & 0xffff);
        buffer.getShort(); // checksum

        if (rxBuf.length < packetSize) {
            LOG.debug("processPacket(): missing {} bytes (got {}/{} bytes)",
                    packetSize - rxBuf.length,
                    rxBuf.length,
                    packetSize);
            return new AbstractXiaomiSppProtocol.ParseResult(AbstractXiaomiSppProtocol.ParseResult.Status.Incomplete);
        }

        final XiaomiSppPacketV2 decodedPacket = XiaomiSppPacketV2.decode(rxBuf);
        if (decodedPacket != null) {
            switch (decodedPacket.getPacketType()) {
                case PACKET_TYPE_SESSION_CONFIG:
                    handleSessionConfig((XiaomiSppPacketV2.SessionConfigPacket) decodedPacket);
                    break;
                case PACKET_TYPE_DATA:
                    XiaomiSppPacketV2.DataPacket dataPacket = (XiaomiSppPacketV2.DataPacket) decodedPacket;
                    try {
                        onPacketReceived(dataPacket.getChannel(), dataPacket.getPayloadBytes(xiaomiSupport.getAuthService()));
                    } catch (final Exception ex) {
                        LOG.error("Exception while handling received packet", ex);
                    }
                    // TODO: only directly ack protobuf packets, bulk ack others
                    sendAck(decodedPacket.getSequenceNumber());
                    break;
                case PACKET_TYPE_ACK:
                    LOG.debug("receive ack for packet {}", decodedPacket.getSequenceNumber());
                    onAck(decodedPacket.getSequenceNumber());
                    break;
                case PACKET_TYPE_NACK:
                    onNack(decodedPacket.getSequenceNumber());
                    break;
                default:
                    LOG.warn("Unhandled packet with type {} (decoded type {})", decodedPacket.getPacketType(), decodedPacket.getClass().getSimpleName());
                    break;
            }
        }

        return new AbstractXiaomiSppProtocol.ParseResult(AbstractXiaomiSppProtocol.ParseResult.Status.Complete, packetSize);
    }

    /**
     * Encodes a data packet with the next sequence number. It is written to the given builder if
     * the device's window has room, otherwise it waits in {@link #pendingFrames} and is written in
     * its own transaction once enough earlier packets are acked.
     */
    private void sendDataPacket(final TransactionBuilder builder,
                                final XiaomiChannelHandler.Channel channel,
                                final byte[] payloadBytes) {
        synchronized (sendLock) {
            final int sequenceNumber = sendWindow.nextSequenceNumber();
            final byte[] frame = encodePacket(channel, sequenceNumber, payloadBytes);

            if (pendingFrames.isEmpty() && sendWindow.size() < txWindow) {
                writeChunks(builder, frame);
                sendWindow.onSent(sequenceNumber, frame);
                armResendTimeout();
            } else {
                LOG.debug("tx window full ({} unacked), holding packet {} for task '{}'",
                        sendWindow.size(), sequenceNumber, builder.getTaskName());
                pendingFrames.addLast(new PendingFrame(builder.getTaskName(), sequenceNumber, frame));
            }
        }
    }

    /**
     * Writes held packets, oldest first, while the device's window has room.
     */
    private void sendPendingFrames() {
        synchronized (sendLock) {
            while (!pendingFrames.isEmpty() && sendWindow.size() < txWindow) {
                final PendingFrame pending = pendingFrames.removeFirst();
                LOG.debug("tx window has room, sending held packet {} for task '{}'",
                        pending.sequenceNumber, pending.taskName);
                final TransactionBuilder builder = commsSupport.createTransactionBuilder(pending.taskName);
                writeChunks(builder, pending.bytes);
                // track before queueing, so an ack that arrives right after the write finds the frame
                sendWindow.onSent(pending.sequenceNumber, pending.bytes);
                builder.queue();
            }
        }
        armResendTimeout();
    }

    private void onAck(final int sequenceNumber) {
        if (sendWindow.onAck(sequenceNumber) > 0) {
            onAckProgress();
            sendPendingFrames();
        }
    }

    private void onNack(final int expectedSequenceNumber) {
        // everything before the expected packet has arrived
        onAck((expectedSequenceNumber - 1) & 0xff);

        if (sendWindow.getOldestUnacked() != expectedSequenceNumber) {
            LOG.warn("receive nack, watch expects packet {}, which is not pending", expectedSequenceNumber);
            return;
        }

        synchronized (sendLock) {
            // the watch nacks every packet that arrives after a gap; resend once per gap, not once per nack
            if (resendFromSequenceNumber == expectedSequenceNumber) {
                LOG.debug("receive nack, watch expects packet {}, already resent", expectedSequenceNumber);
                return;
            }
            resendFromSequenceNumber = expectedSequenceNumber;
        }

        LOG.warn("receive nack, watch expects packet {}, resending", expectedSequenceNumber);
        resendUnacked(String.format(Locale.ROOT, "resend from %d", expectedSequenceNumber));
    }

    private void resendUnacked(final String taskName) {
        final List<byte[]> frames = sendWindow.getUnackedFrames();
        if (frames.isEmpty()) {
            return;
        }
        LOG.debug("resending {} unacked packets", frames.size());
        final TransactionBuilder builder = commsSupport.createTransactionBuilder(taskName);
        for (final byte[] frame : frames) {
            writeChunks(builder, frame);
        }
        builder.queue();
        armResendTimeout();
    }

    private void onAckProgress() {
        synchronized (sendLock) {
            resendFromSequenceNumber = -1;
        }
        synchronized (timeoutRunnable) {
            timeoutsWithoutAck = 0;
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutPending = false;
        }
        armResendTimeout();
    }

    private void armResendTimeout() {
        if (sendWindow.isEmpty()) {
            return;
        }
        final long timeoutMillis;
        synchronized (sendLock) {
            timeoutMillis = resendTimeoutMillis;
        }
        synchronized (timeoutRunnable) {
            if (!timeoutPending) {
                timeoutPending = true;
                timeoutHandler.postDelayed(timeoutRunnable, timeoutMillis);
            }
        }
    }

    private void cancelResendTimeout() {
        synchronized (timeoutRunnable) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutPending = false;
            timeoutsWithoutAck = 0;
        }
    }

    private void onResendTimeout() {
        final int timeouts;
        synchronized (timeoutRunnable) {
            timeoutPending = false;
            if (sendWindow.isEmpty()) {
                timeoutsWithoutAck = 0;
                return;
            }
            timeouts = ++timeoutsWithoutAck;
        }

        // A lost BLE link is noticed and reset by the BLE stack, so keep resending while connected
        LOG.warn("no ack for packet {} (timeout #{}), resending", sendWindow.getOldestUnacked(), timeouts);
        resendUnacked(String.format(Locale.ROOT, "resend from %d after timeout", sendWindow.getOldestUnacked()));
    }

    private byte[] encodePacket(final XiaomiChannelHandler.Channel channel,
                                final int sequenceNumber,
                                final byte[] payloadBytes) {
        return XiaomiSppPacketV2.newDataPacketBuilder()
                .setChannel(channel)
                .setSequenceNumber(sequenceNumber)
                .setOpCode(XiaomiSppPacketV2.DataPacket.getOpCodeForChannel(channel))
                .setPayload(payloadBytes)
                .build()
                .encode(xiaomiSupport.getAuthService());
    }

    private void writeChunks(final TransactionBuilder builder, final byte[] value) {
        builder.writeChunkedData(btCharacteristicWrite, value, maxWriteSize);
    }
}
