package xcj.app.compose_share.components

interface VisibilityComposeStateProvider {
    /**
     * @param name, or key for ComposeContainerState
     */
    fun provideState(name: String): VisibilityComposeState
}