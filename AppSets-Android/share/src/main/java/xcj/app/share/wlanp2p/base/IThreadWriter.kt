package xcj.app.share.wlanp2p.base

import android.content.Context
import xcj.app.share.base.DataSendContent

interface IThreadWriter {
    suspend fun writeContent(
        context: Context,
        dataSendContent: DataSendContent.WlanP2pContent
    )
}