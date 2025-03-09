package xcj.app.web.webserver.netty

import io.netty.channel.ChannelProgressiveFuture
import io.netty.channel.ChannelProgressiveFutureListener
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.interfaces.ProgressListener
import java.io.Closeable

class ContentDownloadProgressListener(
    private val id: String,
    private val name: String,
    private val length: Long,
    private val relatedCloseable: Closeable?,
    private val progressListener: ProgressListener?
) : ChannelProgressiveFutureListener {
    companion object {
        private const val TAG = "ContentDownloadProgressListener"
    }

    override fun operationProgressed(
        future: ChannelProgressiveFuture?,
        progress: Long,
        total: Long
    ) {
        if (progressListener != null) {
            val dataProgressInfo =
                DataProgressInfoPool.obtainById(id)
            dataProgressInfo.name = name
            dataProgressInfo.total = if (total != -1L) {
                total
            } else {
                length
            }
            dataProgressInfo.current = progress
            progressListener.onProgress(dataProgressInfo)
        }
    }

    override fun operationComplete(future: ChannelProgressiveFuture) {
        PurpleLogger.current.d(TAG, "operationComplete, id:${id}, name:${name}")
        relatedCloseable?.close()
        future.removeListener(this)
    }
}