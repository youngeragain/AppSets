package xcj.app.web.webserver.interfaces

import java.io.File

interface FileCreator {
    fun makeFileIfNeeded(name: String, createFile: Boolean = true): File?
}