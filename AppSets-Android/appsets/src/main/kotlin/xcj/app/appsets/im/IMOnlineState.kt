package xcj.app.appsets.im

sealed interface IMOnlineState {
    data object Offline : IMOnlineState

    data class Online(val info: Any? = null) : IMOnlineState
}