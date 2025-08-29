package xcj.app.appsets.ui.compose.apps.tools.file_manager

import android.net.Uri

interface AbstractFile<T> : AbstractFileActions {
    val name: String

    val nameWithoutExtension: String

    val extension: String

    val isRoot: Boolean


    fun listChildren(): List<T>?

    fun path(): String

    fun getParent(): T?

    fun asUri(): Uri

    fun newInstance(): AbstractFile<T>
}