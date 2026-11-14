/*  Copyright (C) 2026 Gadgetbridge contributors

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus

import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice
import nodomain.freeyourgadget.gadgetbridge.service.btbr.AbstractBTBRDeviceSupport
import nodomain.freeyourgadget.gadgetbridge.service.btbr.TransactionBuilder
import nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus.OnePlusProtocol.Proto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * OnePlus Watch (2021, W301GB): Bluetooth Classic / RFCOMM, plain protobuf, no app-level authentication
 * (the only requirement is a normal Bluetooth bond). See PROTOCOL.md in the reverse-engineering notes.
 *
 * Implemented: connect, first-time setup.
 */
class OnePlusWatchSupport : AbstractBTBRDeviceSupport(LOG, BUFFER_SIZE) {
    private val deframer = OnePlusProtocol.Deframer()

    override fun getSupportedService(): UUID = SERVICE_UUID

    override fun useAutoConnect(): Boolean = true

    override fun initializeDevice(builder: TransactionBuilder): TransactionBuilder {
        deframer.reset()
        oobeStarted = false
        builder.write(*OnePlusProtocol.HELLO)
        builder.setDeviceState(GBDevice.State.INITIALIZED)
        return builder
    }

    override fun onSocketRead(data: ByteArray) {
        for (msg in deframer.feed(data)) {
            try {
                handle(msg)
            } catch (e: Exception) {
                LOG.warn("Failed to handle {}", msg, e)
            }
        }
    }

    private fun handle(msg: OnePlusProtocol.Message) {
        LOG.debug("RX {}.{} ({} bytes)", msg.service, msg.command, msg.body.size)
        when (msg.service) {
            OnePlusProtocol.SERVICE_DEVICE -> when (msg.command) {
                OnePlusProtocol.CMD_HELLO -> onHelloReply()
            }
            OnePlusProtocol.SERVICE_OOBE -> handleOobe(msg)
        }
    }

    // ---------------------------------------------------------------- first-time setup (OOBE)

    private var oobeStarted = false

    /** The watch repeats an empty 11.12 while it sits on its pairing/setup screen: answer with OHealth's setup steps. */
    private fun handleOobe(msg: OnePlusProtocol.Message) {
        if (msg.command == OnePlusProtocol.CMD_OOBE_WAITING && !oobeStarted) {
            oobeStarted = true
            LOG.info("Watch is waiting for setup: starting OOBE sequence")
            val b = createTransactionBuilder("oobe")
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_DEVICE, OnePlusProtocol.CMD_BIND_REQUEST))
            b.sleep(700)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_START_SETTING, Proto.encode(1 to 1L, 2 to 2L)))
            b.sleep(700)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_CONNECT_RESULT, Proto.encode(2 to 2L)))
            b.sleep(700)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_GET_STATUS))
            b.sleep(1500)
            // wrist side (0/1), then OHealth's capability-sync handshake
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_HAND, Proto.encode(1 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_SYNC_START,
                Proto.encode(5 to 11L, 4 to 10L, 6 to 9L, 3 to 12L, 2 to 13L, 9 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_SYNC_END,
                Proto.encode(5 to 10L, 2 to 11L, 6 to 9L, 4 to 12L, 9 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_SYNC_FINISH,
                Proto.encode(5 to 10L, 2 to 11L, 6 to 9L, 4 to 12L, 9 to 1L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_TRANSFER_STATE, Proto.encode(1 to 18L)))
            b.sleep(1500)
            b.write(*OnePlusProtocol.frame(OnePlusProtocol.SERVICE_OOBE, OnePlusProtocol.CMD_OOBE_GET_STATUS))
            b.queue()
        }
    }

    private fun onHelloReply() {
        LOG.info("Watch is ready")
    }

    // ---------------------------------------------------------------- helpers

    private fun send(task: String, service: Int, command: Int, body: ByteArray) {
        createTransactionBuilder(task).write(*OnePlusProtocol.frame(service, command, body)).queue()
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(OnePlusWatchSupport::class.java)

        /** SDP service "SPP SERVER2" (RFCOMM channel 4 on the W301GB). */
        val SERVICE_UUID: UUID = UUID.fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3")
        private const val BUFFER_SIZE = 8192
    }
}
