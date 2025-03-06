package xcj.app.share.wlanp2p.base

import xcj.app.share.base.DataContent
import xcj.app.share.base.DataSendContent

data class WlanP2pContent(
    val dstDevice: P2pShareDevice,
    override val content: DataContent
) : DataSendContent