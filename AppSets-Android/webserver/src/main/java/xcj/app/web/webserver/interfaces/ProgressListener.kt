package xcj.app.web.webserver.interfaces

import xcj.app.web.webserver.base.DataProgressInfo

fun interface ProgressListener {
    fun onProgress(dataProgressInfo: DataProgressInfo)
}