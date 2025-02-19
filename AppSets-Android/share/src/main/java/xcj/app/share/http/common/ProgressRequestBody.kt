package xcj.app.share.http.common

import android.content.Context
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
import xcj.app.share.base.DataProgressInfoPool
import xcj.app.share.base.ProgressListener
import xcj.app.starter.util.ContentType
import java.io.Closeable
import java.io.FileDescriptor
import java.io.FileInputStream

class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val dataContent: DataContent,
    private val progressListener: ProgressListener?,
    private val closeable: Closeable? = null
) : RequestBody(), Closeable {

    companion object {
        private const val TAG = "ProgressRequestBody"
    }

    private var bufferedSink: BufferedSink? = null

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    fun contentLength1(): Long {
        if (dataContent is DataContent.UriContent) {
            return dataContent.androidUriFile?.size ?: contentLength()
        }
        return requestBody.contentLength()
    }

    override fun close() {
        closeable?.close()
    }

    override fun writeTo(sink: BufferedSink) {
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

            var bytesWritten: Long = 0L
            var contentLength: Long = 0L

            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) {
                    contentLength = contentLength1() // 总长度，只获取一次
                }
                bytesWritten += byteCount

                val progressListener = progressListener
                if (progressListener != null) {
                    val dataProgressInfo = DataProgressInfoPool.obtainById(dataContent.id)
                    dataProgressInfo.total = contentLength
                    dataProgressInfo.current = bytesWritten
                    dataProgressInfo.percentage =
                        (bytesWritten / contentLength.toDouble()) * 100
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
            val parcelFileDescriptor =
                context.applicationContext.contentResolver.openFileDescriptor(this.uri, "r")
            if (parcelFileDescriptor == null) {
                return null
            }

            val requestBody =
                parcelFileDescriptor.fileDescriptor.toRequestBody1(ContentType.MULTIPART_FORM_DATA.toMediaTypeOrNull())
            return ProgressRequestBody(requestBody, this, progressListener, parcelFileDescriptor)
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
