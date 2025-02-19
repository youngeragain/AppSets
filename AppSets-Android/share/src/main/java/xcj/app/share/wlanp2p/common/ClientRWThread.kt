package xcj.app.share.wlanp2p.common

data class ClientRWThread(
    val readThread: ReadThread,
    val writeThread: WriteThread
)
