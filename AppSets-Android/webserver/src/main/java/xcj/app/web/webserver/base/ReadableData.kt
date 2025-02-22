package xcj.app.web.webserver.base

import java.io.InputStream

interface ReadableData {
    fun getLength(): Long
}

interface InputStreamReadableData : ReadableData {

    fun getInputStream(): InputStream
}