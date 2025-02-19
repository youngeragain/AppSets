package xcj.app.share.wlanp2p.base

import java.io.Closeable

interface ReadMethod : Closeable {
    fun reset()
    fun doRead()
}