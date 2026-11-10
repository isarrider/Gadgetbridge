package tasks

import javax.inject.Inject
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional

interface GitValueSourceParameters : ValueSourceParameters {
    @get:InputFiles
    @get:Optional
    val gitFiles: ListProperty<RegularFile>
}

abstract class GitCommitCountValueSource : ValueSource<Int, GitValueSourceParameters> {
    @get:Inject
    abstract val providers: ProviderFactory

    override fun obtain(): Int {
        return try {
            val count = providers.exec {
                commandLine("git", "rev-list", "HEAD", "--count")
            }.standardOutput.asText.get().trim().toIntOrNull() ?: 0
            count
        } catch (ignored: Exception) {
            0
        }
    }
}

abstract class GitHashValueSource : ValueSource<String, GitValueSourceParameters> {
    @get:Inject
    abstract val providers: ProviderFactory

    override fun obtain(): String {
        return try {
            val hash = providers.exec {
                commandLine("git", "rev-parse", "HEAD")
            }.standardOutput.asText.get().trim()
            hash.take(10)
        } catch (ignored: Exception) {
            ""
        }
    }
}

abstract class GitDirtyFlagValueSource : ValueSource<String, GitValueSourceParameters> {
    @get:Inject
    abstract val providers: ProviderFactory

    override fun obtain(): String {
        return try {
            val status = providers.exec {
                commandLine("git", "status", "--porcelain")
            }.standardOutput.asText.get().trim()
            if (status.isBlank()) "" else " (dirty)"
        } catch (ignored: Exception) {
            " (unknown status)"
        }
    }
}
