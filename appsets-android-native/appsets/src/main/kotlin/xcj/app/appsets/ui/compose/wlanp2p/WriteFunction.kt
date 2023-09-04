package xcj.app.appsets.ui.compose.wlanp2p

import java.io.BufferedOutputStream

fun interface WriteFunction {
    fun writeContent(tag: String, bos: BufferedOutputStream)
}