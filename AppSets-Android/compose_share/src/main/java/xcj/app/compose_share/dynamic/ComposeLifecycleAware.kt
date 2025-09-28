package xcj.app.compose_share.dynamic

interface ComposeLifecycleAware {
    fun onComposeDispose(by: String?)
}