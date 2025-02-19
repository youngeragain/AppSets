package xcj.app.share.base

fun interface ProgressListener {
    fun onProgress(
        dataProgressInfo: DataProgressInfo
    )
}