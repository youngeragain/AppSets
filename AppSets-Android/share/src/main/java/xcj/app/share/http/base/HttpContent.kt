package xcj.app.share.http.base

import xcj.app.share.base.DataContent
import xcj.app.share.base.DataSendContent

data class HttpContent(
    val dstDevice: HttpShareDevice,
    override val content: DataContent
) : DataSendContent