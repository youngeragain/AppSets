package xcj.app.web.webserver.base

import java.io.Closeable
import java.io.InputStream

interface ReadableData {
    fun getLength(): Long
}

interface InputStreamReadableData : ReadableData {
    fun getRelatedCloseable(): Closeable?
    fun getInputStream(): InputStream
}