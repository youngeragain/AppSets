package xcj.app.appsets.ui.compose.apps.tools.file_manager

interface AbstractFileActions {
    suspend fun createFile(name: String, content: ByteArray): Boolean

    suspend fun createFileFolder(name: String): Boolean

    suspend fun delete(): Boolean

    fun isFolder(): Boolean

    fun isWriteable(): Boolean

    fun isReadable(): Boolean
}