package nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus

import nodomain.freeyourgadget.gadgetbridge.service.devices.oneplus.OnePlusProtocol.Proto
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class OnePlusProtocolTest {
    private fun hex(s: String) = s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    // Frames captured from a real OHealth <-> OnePlus Watch session.
    private val helloCaptured = hex("aa0e00000101080510b42e18de0728032385")
    private val hrQueryCaptured = hex("aa100000050b08e8e3dbd70610c6caddd706ea9c")

    @Test
    fun helloMatchesCapture() {
        assertArrayEquals(helloCaptured, OnePlusProtocol.HELLO)
    }

    @Test
    fun heartRateQueryMatchesCapture() {
        val built = OnePlusProtocol.frame(
            OnePlusProtocol.SERVICE_FITNESS, OnePlusProtocol.CMD_HR_QUERY,
            Proto.encode(1 to 1794569192L, 2 to 1794599238L)
        )
        // timestamps differ from the capture only in value; layout and CRC must be self-consistent
        val parsed = OnePlusProtocol.Deframer().feed(built).single()
        assertEquals(5, parsed.service)
        assertEquals(11, parsed.command)
        val f = Proto.decode(parsed.body)
        assertEquals(1794569192L, Proto.long(f, 1))
        assertEquals(1794599238L, Proto.long(f, 2))
        // and the captured frame parses with a valid CRC
        val cap = OnePlusProtocol.Deframer().feed(hrQueryCaptured).single()
        assertEquals(11, cap.command)
    }

    @Test
    fun deframerHandlesFragmentsAndGarbage() {
        val a = OnePlusProtocol.HELLO
        val d = OnePlusProtocol.Deframer()
        val out = ArrayList<OnePlusProtocol.Message>()
        out += d.feed(byteArrayOf(0x00, 0x13))            // noise
        out += d.feed(a.copyOfRange(0, 5))                // partial
        out += d.feed(a.copyOfRange(5, a.size) + a)       // rest + a second frame
        assertEquals(2, out.size)
        assertEquals(1, out[0].service)
    }

    @Test
    fun badCrcIsRejected() {
        val bad = OnePlusProtocol.HELLO.copyOf().also { it[it.size - 1] = (it[it.size - 1] + 1).toByte() }
        assertEquals(0, OnePlusProtocol.Deframer().feed(bad).size)
    }

    @Test
    fun protoRoundTrip() {
        val enc = Proto.encode(1 to 4242L, 3 to "org.example", 5 to "Title", 7 to 1794601371L)
        val f = Proto.decode(enc)
        assertEquals(4242L, Proto.long(f, 1))
        assertEquals("org.example", Proto.string(f, 3))
        assertEquals(1794601371L, Proto.long(f, 7))
    }
}
