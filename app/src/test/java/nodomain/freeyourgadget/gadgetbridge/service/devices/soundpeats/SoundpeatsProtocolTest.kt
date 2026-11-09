/*  Copyright (C) 2026 Pedro

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.service.devices.soundpeats

import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsAncMode
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsCustomEqStore
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsEqImport
import nodomain.freeyourgadget.gadgetbridge.devices.soundpeats.SoundpeatsEqPresets
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundpeatsProtocolTest {
    @Test
    fun testEncodeAncMode() {
        val encoded = SoundpeatsProtocol.encodeSetAncMode(SoundpeatsAncMode.TRANSPARENCY)

        // SET_NOISE_CANCELLATION_MODE = 0x0311, vendor 0x000A, payload = mode code (2)
        assertEquals(0x00.toByte(), encoded[4])
        assertEquals(0x0A.toByte(), encoded[5])
        assertEquals(0x03.toByte(), encoded[6])
        assertEquals(0x11.toByte(), encoded[7])
        assertEquals(0x02.toByte(), encoded[8])
        // the Q3 Pro implements three modes only
        assertEquals(3, SoundpeatsAncMode.entries.size)
    }

    @Test
    fun testEncodeWaterDrainage() {
        val encoded = SoundpeatsProtocol.encodeSetWaterDrainage(true)

        assertEquals(0x03.toByte(), encoded[6])
        assertEquals(0x3E.toByte(), encoded[7])
        assertEquals(0x01.toByte(), encoded[8])
    }

    @Test
    fun testDecodeEqV2BandFromCapture() {
        // Real payload captured from the Q3 Pro: band 2, 62 Hz, +2.00 dB, Q 0.7, master -2 dB
        val payload = byteArrayOf(
            0x10, 0x02, 0xFF.toByte(), 0x88.toByte(), 0x00, 0xBA.toByte(),
            0x00, 0x78, 0x0B, 0x33,
        )

        val band = SoundpeatsProtocol.decodeEqV2Band(payload)

        assertTrue(band != null)
        assertEquals(2, band!!.index)
        assertEquals(62, band.freqHz) // frequency is carried as Hz * 3
        assertEquals(2.0, band.gainDb, 0.001)
        assertEquals(0.7, band.q, 0.001) // Q is carried as Q * 4096 (0x0B33 = 2867)
        // master gain and band count live in the first four bytes
        assertEquals(0x10, payload[0].toInt())
        assertEquals(-120, ((payload[2].toInt() and 0xFF) shl 8) or (payload[3].toInt() and 0xFF) - 0x10000)
    }

    @Test
    fun testEncodeEqV2BandMatchesCapture() {
        // Encoding the captured band must reproduce the exact bytes the app sent:
        // 10 01 FF 88 00 3C FE AA 03 33  (band 1, 20 Hz, -5.70 dB, Q 0.2, master -2 dB)
        val encoded = SoundpeatsProtocol.encodeEqV2Band(
            bandCount = 16,
            masterGainDb = -2.0,
            band = EqBand(index = 1, freqHz = 20, gainDb = -5.7, q = 0.2),
        )

        // EQ uses vendor 0x001D and command 0x0E02
        assertEquals(0x00.toByte(), encoded[4])
        assertEquals(0x1D.toByte(), encoded[5])
        assertEquals(0x0E.toByte(), encoded[6])
        assertEquals(0x02.toByte(), encoded[7])
        val expectedPayload = byteArrayOf(
            0x10, 0x01, 0xFF.toByte(), 0x88.toByte(), 0x00, 0x3C,
            0xFE.toByte(), 0xAA.toByte(), 0x03, 0x33,
        )
        assertArrayEquals(expectedPayload, encoded.copyOfRange(8, encoded.size))
    }

    @Test
    fun testEncodeEqV2ProfileSendsOneFramePerBand() {
        val bands = listOf(
            EqBand(index = 1, freqHz = 20, gainDb = -5.7, q = 0.2),
            EqBand(index = 2, freqHz = 62, gainDb = 2.0, q = 0.7),
            EqBand(index = 3, freqHz = 180, gainDb = -9.4, q = 1.0),
        )

        val frames = SoundpeatsProtocol.encodeEqV2(SoundpeatsEqPresets.MASTER_GAIN_DB, bands)

        assertEquals(3, frames.size)
        frames.forEachIndexed { i, frame ->
            assertEquals(0x0E.toByte(), frame[6])
            assertEquals(0x02.toByte(), frame[7])
            assertEquals(3, frame[8].toInt())
            assertEquals(i + 1, SoundpeatsProtocol.decodeEqV2Band(frame.copyOfRange(8, frame.size))!!.index)
        }
    }

    @Test
    fun testClassicEqPresetMatchesCapture() {
        // The vendor app applying "Classic" to the Q3 Pro produced exactly these 16 frames.
        val expected = listOf(
            "ff04000a001d0e021001ff88003cfeaa0333",
            "ff04000a001d0e021002ff8800ba00000ccc",
            "ff04000a001d0e021003ff88017700000ccc",
            "ff04000a001d0e021004ff88021cfdcc1000",
            "ff04000a001d0e021005ff8802ee00000ccc",
            "ff04000a001d0e021006ff8804ecfd6c0800",
            "ff04000a001d0e021007ff8805dc00000ccc",
            "ff04000a001d0e021008ff880bb800000ccc",
            "ff04000a001d0e021009ff881770ff043000",
            "ff04000a001d0e02100aff88177000000ccc",
            "ff04000a001d0e02100bff8820d000e41333",
            "ff04000a001d0e02100cff882c88ff403000",
            "ff04000a001d0e02100dff882ee000000ccc",
            "ff04000a001d0e02100eff885dc000000ccc",
            "ff04000a001d0e02100fff886018ff884000",
            "ff04000a001d0e021010ff88a41000d20199",
        )

        val frames = SoundpeatsProtocol.encodeEqV2(
            SoundpeatsEqPresets.MASTER_GAIN_DB,
            SoundpeatsEqPresets.Preset.CLASSIC.toBands(),
        )

        assertEquals(expected.size, frames.size)
        expected.forEachIndexed { i, hex ->
            val actual = frames[i].joinToString("") { "%02x".format(it) }
            assertEquals("frame $i", hex, actual)
        }
    }

    @Test
    fun testCustomEqExpandsToDeviceSlots() {
        val gains = listOf(1.0, -2.0, 3.0, -4.0, 5.0, -6.0, 7.0, -8.0)

        val bands = SoundpeatsCustomEqStore.expandGains(gains)

        assertEquals(16, bands.size)
        // slots are one-based and follow the device table
        assertEquals((1..16).toList(), bands.map { it.index })
        assertEquals(SoundpeatsEqPresets.SLOT_FREQUENCIES, bands.map { it.freqHz })
        // the eight edited bands land on their own slot
        SoundpeatsCustomEqStore.EDIT_BANDS.forEachIndexed { i, frequency ->
            val band = bands.first { it.freqHz == frequency && it.gainDb == gains[i] }
            assertEquals(gains[i], band.gainDb, 0.001)
        }
        // everything else is sent flat
        assertEquals(0.0, bands.first { it.freqHz == 20 }.gainDb, 0.001)
        assertEquals(SoundpeatsEqPresets.FILLER_Q, bands.first { it.freqHz == 20 }.q, 0.001)
    }

    @Test
    fun testImportParametricEq() {
        val text = """
            Preamp: -6.0 dB
            Filter 1: ON PK Fc 62 Hz Gain 4.0 dB Q 0.70
            Filter 2: ON PK Fc 250 Hz Gain -3.0 dB Q 1.00
            Filter 3: ON LSC Fc 8000 Hz Gain 2.5 dB Q 0.70
        """.trimIndent()

        val filters = SoundpeatsEqImport.parse(text)
        assertEquals(3, filters.size)
        assertEquals(62.0, filters[0].frequencyHz, 0.01)
        assertEquals(4.0, filters[0].gainDb, 0.01)

        val gains = SoundpeatsEqImport.toBandGains(filters)
        assertEquals(8, gains.size)
        // bands are 62 / 125 / 250 / 500 / 1k / 2k / 4k / 8k
        assertEquals(4.0, gains[0], 0.01)
        assertEquals(-3.0, gains[2], 0.01)
        assertEquals(2.5, gains[7], 0.01)
    }
}
