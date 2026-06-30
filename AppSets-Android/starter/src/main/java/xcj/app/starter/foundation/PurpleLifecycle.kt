package xcj.app.starter.foundation

interface PurpleLifecycle {
    suspend fun onInit()
    suspend fun onStart()
    suspend fun onRefresh()
    suspend fun onReady()
    suspend fun onStop()
    suspend fun onDestroy()

    sealed interface State {
        data object INITIALED : State
        data object STARTED : State
        data object READY : State
        data object DESTROYED : State
    }
}

