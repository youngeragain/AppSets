package xcj.app.web.webserver.base

import java.io.InputStream
import java.nio.channels.FileChannel

interface ReadableData {
    fun getLength(): Long
}

interface InputStreamReadableData : ReadableData {

    fun getInputStream(): InputStream
}

interface FileChanelReadableData : ReadableData {
    fun getFileChannel(): FileChannel
}