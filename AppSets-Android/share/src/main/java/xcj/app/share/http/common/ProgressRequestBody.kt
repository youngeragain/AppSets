package xcj.app.share.http.common

import android.content.Context
import android.os.ParcelFileDescriptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
import okio.source
import xcj.app.share.base.DataContent
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.interfaces.ProgressListener
import java.io.Closeable
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.InputStream

class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val dataContent: DataContent,
    private val progressListener: ProgressListener?,
    private val relatedCloseable: Closeable? = null
) : RequestBody() {

    companion object {
        private const val TAG = "ProgressRequestBody"
    }

    private var bufferedSink: BufferedSink? = null

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return contentLength1()
    }

    fun contentLength1(): Long {
        if (dataContent is DataContent.UriContent) {
            return dataContent.androidUriFile?.size ?: requestBody.contentLength()
        }
        return requestBody.contentLength()
    }

    fun close() {
        PurpleLogger.current.d(TAG, "close")
        relatedCloseable?.close()
    }

    override fun writeTo(sink: BufferedSink) {
        PurpleLogger.current.d(TAG, "writeTo")
        if (bufferedSink == null) {
            // 将传入的 Sink 包装成 CountingSink
            bufferedSink = sink(sink).buffer()
        }
        val bufferedSink = bufferedSink
        if (bufferedSink != null) {
            // 实际执行写入
            requestBody.writeTo(bufferedSink)
            // 必须调用 flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush()
        }
    }

    private fun sink(sink: Sink): Sink {
        return object : ForwardingSink(sink) {

            var totalBytesWritten: Long = 0L
            var contentLength: Long = 0L

            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) {
                    contentLength = contentLength1()
                }
                totalBytesWritten += byteCount
                val progressListener = progressListener
                if (progressListener != null) {
                    val dataProgressInfo = DataProgressInfoPool.obtainById(dataContent.id)
                    dataProgressInfo.total = contentLength
                    dataProgressInfo.current = totalBytesWritten
                    progressListener.onProgress(dataProgressInfo)
                }
            }
        }
    }
}

fun DataContent.asProgressRequestBody(
    context: Context,
    contentType: MediaType? = null,
    progressListener: ProgressListener?
): ProgressRequestBody? {
    when (this) {
        is DataContent.FileContent -> {
            val requestBody = this.file.asRequestBody(contentType)
            return ProgressRequestBody(requestBody, this, progressListener)
        }

        is DataContent.UriContent -> {
            val inputStream =
                context.applicationContext.contentResolver.openInputStream(this.uri)
            if (inputStream == null) {
                return null
            }

            val requestBody =
                inputStream.toRequestBody(ContentType.MULTIPART_FORM_DATA.toMediaTypeOrNull())
            return ProgressRequestBody(requestBody, this, progressListener)
        }

        is DataContent.ByteArrayContent -> {
            val requestBody = this.bytes.toRequestBody(contentType)
            return ProgressRequestBody(requestBody, this, progressListener)
        }

        else -> {
            return null
        }
    }

    return null
}

fun InputStream.toRequestBody(contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {

        override fun contentType() = contentType

        override fun isOneShot(): Boolean = true

        override fun writeTo(sink: BufferedSink) {
            use {
                sink.writeAll(it.source())
            }
        }
    }
}

fun ParcelFileDescriptor.toRequestBody(contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {

        override fun contentType() = contentType

        override fun isOneShot(): Boolean = true

        override fun writeTo(sink: BufferedSink) {
            FileInputStream(fileDescriptor).use {
                sink.writeAll(it.source())
            }
        }
    }
}

fun FileDescriptor.toRequestBody1(contentType: MediaType? = null): RequestBody {
    return object : RequestBody() {

        override fun contentType() = contentType

        override fun isOneShot(): Boolean = true

        override fun writeTo(sink: BufferedSink) {
            FileInputStream(this@toRequestBody1).use {
                sink.writeAll(it.source())
            }
        }
    }
}
