package xcj.app.web.webserver.base

fun interface ProgressListener {
    fun onProgress(dataProgressInfo: DataProgressInfo)
}