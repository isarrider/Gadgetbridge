package nodomain.freeyourgadget.gadgetbridge.companion

import nodomain.freeyourgadget.gadgetbridge.test.TestBase
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class WorkoutJobStoreTest : TestBase() {
    @get:Rule val folder = TemporaryFolder()
    private val watch = "AA:BB:CC:DD:EE:FF"
    private val bytes = byteArrayOf(1, 2, 3)
    @Test fun survivesRestartAndReturnsTheSameJobForAnIdenticalRequest() {
        val dir = folder.newFolder()
        val first = WorkoutJobStore(dir).submit("app|signer", "request", watch, bytes)
        val restarted = WorkoutJobStore(dir)
        assertEquals(first, restarted.get("app|signer", "request", watch))
        assertEquals(first, restarted.submit("app|signer", "request", watch.lowercase(), bytes))
        assertArrayEquals(bytes, restarted.file(first).readBytes())
    }
    @Test fun interruptedAtomicWritesRecoverTheirBackupOnOlderAndroid() {
        val dir = folder.newFolder()
        val first = WorkoutJobStore(dir).submit("app", "request", watch, bytes)
        val metadata = java.io.File(dir, "${first.id}.json")
        assertTrue(metadata.renameTo(java.io.File(dir, "${first.id}.json.bak")))
        val restarted = WorkoutJobStore(dir)
        assertEquals(listOf(first), restarted.all())
        assertEquals(first, restarted.get(first.id))
    }
    @Test fun requestIdCannotBeReusedForDifferentContent() {
        val store = WorkoutJobStore(folder.newFolder())
        store.submit("app|signer", "request", watch, bytes)
        assertThrows(IllegalArgumentException::class.java) { store.submit("app|signer", "request", watch, byteArrayOf(4)) }
    }
    @Test fun ownersAndDevicesCannotReadEachOthersRequests() {
        val store = WorkoutJobStore(folder.newFolder())
        store.submit("first|signer", "request", watch, bytes)
        assertNull(store.get("other|signer", "request", watch))
        assertNull(store.get("first|other-signer", "request", watch))
        assertNull(store.get("first|signer", "request", "11:22:33:44:55:66"))
    }
    @Test fun cancellationAndTransferClaimAreMutuallyExclusive() {
        val store = WorkoutJobStore(folder.newFolder())
        val cancelled = store.submit("app", "cancelled", watch, bytes)
        store.cancel(cancelled.id)
        assertFalse(store.claim(cancelled.id))
        assertFalse(store.file(cancelled).exists())
        val sending = store.submit("app", "sending", watch, bytes)
        assertTrue(store.claim(sending.id))
        assertFalse(store.claim(sending.id))
        assertThrows(IllegalArgumentException::class.java) { store.cancel(sending.id) }
        assertEquals("TRANSFERRING", WorkoutJobStore(folder.root.listFiles()!!.first()).get(sending.id)?.state)
    }
    @Test fun terminalResultsSurviveAndCannotBeOverwrittenByLateCallbacks() {
        val store = WorkoutJobStore(folder.newFolder())
        val job = store.submit("app", "request", watch, bytes)
        store.claim(job.id)
        store.update(job.id, "UNCERTAIN", "transfer_interrupted")
        assertEquals("UNCERTAIN", store.update(job.id, "TRANSFERRED").state)
        assertFalse(store.file(job).exists())
        assertEquals("UNCERTAIN", store.submit("app", "request", watch, bytes).state)
        store.forget("app", "request", watch)
        assertNull(store.get(job.id))
    }
    @Test fun emptyOversizeAndPathLikeIdsAreHandledWithoutEscapingTheStore() {
        val store = WorkoutJobStore(folder.newFolder())
        assertThrows(IllegalArgumentException::class.java) { store.submit("app", "request", watch, byteArrayOf()) }
        assertThrows(IllegalArgumentException::class.java) { store.submit("app", "request", watch, ByteArray(WorkoutJobStore.MAX_BYTES + 1)) }
        val job = store.submit("app", "../../outside", watch, bytes)
        assertTrue(store.file(job).canonicalPath.startsWith(folder.root.canonicalPath))
    }
}
