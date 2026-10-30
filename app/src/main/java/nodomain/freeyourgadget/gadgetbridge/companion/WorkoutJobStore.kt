/* SPDX-License-Identifier: AGPL-3.0-or-later */
package nodomain.freeyourgadget.gadgetbridge.companion

import android.os.Bundle
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.util.Locale

/** Durable, owner-isolated requests. Reusing an ID with different bytes is a conflict, not an update. */
class WorkoutJobStore(private val dir: File) {
    init { check(dir.mkdirs() || dir.isDirectory) }
    data class Job(val id: String, val owner: String, val request: String, val device: String,
                   val hash: String, val state: String = "QUEUED", val error: String = "") {
        fun bundle() = Bundle().apply {
            putString("jobId", id); putString("requestId", request); putString("device", device)
            putString("sha256", hash); putString("state", state); putString("error", error)
        }
        fun json() = JSONObject().put("id", id).put("owner", owner).put("request", request).put("device", device)
            .put("hash", hash).put("state", state).put("error", error)
    }
    fun all(): List<Job> = synchronized(LOCK) {
        dir.listFiles { f -> f.name.endsWith(".json") || f.name.endsWith(".json.bak") }.orEmpty()
            .map { it.name.removeSuffix(".bak").removeSuffix(".json") }.distinct()
            .mapNotNull { runCatching { get(it) }.getOrNull() }
    }
    fun forget(owner: String, request: String, device: String) = synchronized(LOCK) {
        val job = get(owner, request, device) ?: return@synchronized
        require(job.state in TERMINAL) { "job_not_finished" }
        file(job).delete()
        AtomicFile(File(dir, "${job.id}.json")).delete()
    }
    fun id(owner: String, request: String, device: String) = digest("$owner\n${device.uppercase(Locale.ROOT)}\n$request".toByteArray(Charsets.UTF_8))
    fun file(job: Job) = File(dir, "${job.id}.fit")
    fun get(owner: String, request: String, device: String): Job? = get(id(owner, request, device))
    fun get(id: String): Job? = synchronized(LOCK) {
        require(id.matches(Regex("[a-f0-9]{64}")))
        val f = AtomicFile(File(dir, "$id.json"))
        if (!f.baseFile.exists() && !File(f.baseFile.path + ".bak").exists()) return@synchronized null
        val o = try {
            JSONObject(String(f.readFully(), Charsets.UTF_8))
        } catch (e: Exception) {
            throw IllegalStateException("workout_job_unreadable", e)
        }
        Job(o.getString("id"), o.getString("owner"), o.getString("request"), o.getString("device"), o.getString("hash"), o.getString("state"), o.optString("error"))
    }
    fun submit(owner: String, request: String, device: String, bytes: ByteArray): Job = synchronized(LOCK) {
        require(request.isNotBlank() && request.length <= 128 && device.length <= 128 && bytes.isNotEmpty() && bytes.size <= MAX_BYTES)
        val hash = digest(bytes)
        val id = id(owner, request, device)
        get(id)?.let { require(it.hash == hash) { "request_id_conflict" }; return@synchronized it }
        val owned = all().filter { it.owner == owner }
        require(owned.size < MAX_JOBS) { "request_limit" }
        require(dir.listFiles { f -> f.extension == "fit" }.orEmpty().sumOf { it.length() } + bytes.size - File(dir, "$id.fit").length() <= 20L * 1024 * 1024) { "queue_size_limit" }
        val job = Job(id, owner, request, device.uppercase(Locale.ROOT), hash)
        write(AtomicFile(file(job)), bytes)
        try { save(job) } catch (e: Exception) { file(job).delete(); throw e }
        job
    }
    fun claim(id: String): Boolean = synchronized(LOCK) {
        val job = get(id) ?: return@synchronized false
        if (job.state != "QUEUED") return@synchronized false
        update(id, "TRANSFERRING")
        true
    }
    fun cancel(id: String): Job = synchronized(LOCK) {
        val job = requireNotNull(get(id))
        require(job.state != "TRANSFERRING") { "transfer_in_progress" }
        update(id, "CANCELLED")
    }
    fun update(id: String, state: String, error: String = ""): Job = synchronized(LOCK) {
        val job = requireNotNull(get(id))
        if (job.state in TERMINAL) return@synchronized job
        val next = job.copy(state = state, error = error)
        save(next)
        if (state in TERMINAL) file(next).delete()
        next
    }
    private fun save(job: Job) = write(AtomicFile(File(dir, "${job.id}.json")), job.json().toString().toByteArray(Charsets.UTF_8))
    private fun write(file: AtomicFile, bytes: ByteArray) {
        val out = file.startWrite()
        try { out.write(bytes); file.finishWrite(out) } catch (e: Exception) { file.failWrite(out); throw e }
    }
    companion object {
        const val MAX_BYTES = 5 * 1024 * 1024
        const val MAX_JOBS = 256
        val TERMINAL = setOf("TRANSFERRED", "FAILED", "UNCERTAIN", "CANCELLED")
        private val LOCK = Any()
    }
}
