package xcj.app.core.foundation

import kotlinx.coroutines.CoroutineScope

interface PurpleRawScope {
    val coroutineScope: CoroutineScope
    fun init()
    fun start()
    fun started()
    fun stop()
    fun stopped()
    fun destroy()
    fun refresh()
    fun ready()
}