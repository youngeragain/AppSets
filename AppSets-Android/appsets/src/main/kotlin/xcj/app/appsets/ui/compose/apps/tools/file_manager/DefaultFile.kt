package xcj.app.appsets.ui.compose.apps.tools.file_manager

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class DefaultFile(
    val file: File,
    val accessible: Boolean = true,
    var parentDefaultFile: DefaultFile? = null
) : AbstractFile<DefaultFile> {

    override val name: String = file.name

    override val nameWithoutExtension: String = file.nameWithoutExtension

    override val extension: String = file.extension

    override val isRoot: Boolean
        get() = parentDefaultFile == null || parentDefaultFile == this

    override fun getParent(): DefaultFile? {
        return parentDefaultFile
    }

    override fun path(): String {
        return file.path
    }

    override fun asUri(): Uri {
        return file.toUri()
    }

    override fun listChildren(): List<DefaultFile>? {
        if (!file.isDirectory) {
            return null
        }
        val files = file.listFiles()
        return files?.map {
            DefaultFile(it, parentDefaultFile = this)
        }
    }

    override suspend fun createFile(name: String, content: ByteArray): Boolean = withContext(
        Dispatchers.IO
    ) {
        if (!isFolder()) {
            return@withContext false
        }
        if (!isWriteable()) {
            return@withContext false
        }
        runCatching {
            val tempFile = File("${file.path}${File.separator}${name}")
            tempFile.createNewFile()
            tempFile.outputStream().buffered().use {
                it.write(content)
            }
            return@withContext true
        }.onFailure {
            return@withContext false
        }
        return@withContext false
    }

    override suspend fun createFileFolder(name: String): Boolean = withContext(Dispatchers.IO) {
        if (!isFolder()) {
            return@withContext false
        }
        if (!isWriteable()) {
            return@withContext false
        }
        runCatching {
            val tempFile = File("${file.path}${File.separator}${name}${File.separator}")
            return@withContext tempFile.mkdir()
        }.onFailure {
            return@withContext false
        }
        return@withContext false
    }

    override suspend fun delete(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            return@withContext file.delete()
        }.onFailure {
            return@withContext false
        }
        return@withContext false
    }

    override fun isFolder(): Boolean {
        return file.isDirectory
    }

    override fun isReadable(): Boolean {
        return file.canRead()
    }

    override fun isWriteable(): Boolean {
        return file.canWrite()
    }

    override fun newInstance(): AbstractFile<DefaultFile> {
        return copy()
    }

    class DefaultFileAbstractFileCreator() : AbstractFileCreator {
        override fun create(context: Context): DefaultFile {
            val storageDirectory = Environment.getExternalStorageDirectory()
            val defaultFile = DefaultFile(

                storageDirectory,
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            )
            defaultFile.parentDefaultFile = defaultFile
            return defaultFile
        }
    }
}