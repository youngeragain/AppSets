package xcj.app.share.base

sealed interface DataSendContent {

    data class WlanP2pContent(
        val dstDevice: ShareDevice.P2pShareDevice,
        val content: DataContent
    ) : DataSendContent

    data class HttpContent(
        val dstDevice: ShareDevice.HttpShareDevice,
        val content: DataContent
    ) : DataSendContent

}