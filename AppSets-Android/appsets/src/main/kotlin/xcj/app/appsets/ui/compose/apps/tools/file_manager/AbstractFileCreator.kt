package xcj.app.appsets.ui.compose.apps.tools.file_manager

import android.content.Context

interface AbstractFileCreator {
    fun create(context: Context): AbstractFile<*>
}