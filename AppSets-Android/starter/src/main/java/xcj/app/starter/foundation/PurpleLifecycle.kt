package xcj.app.starter.foundation

interface PurpleLifecycle {
    fun onInit()
    fun onStart()
    fun onRefresh()
    fun onReady()
    fun onStop()
    fun onDestroy()

    sealed interface State {
        data object INITIALED : State
        data object STARTED : State
        data object READY : State
        data object DESTROYED : State
    }
}

