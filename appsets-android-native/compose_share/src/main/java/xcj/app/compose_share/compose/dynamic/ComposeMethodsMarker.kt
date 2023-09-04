package xcj.app.compose_share.compose.dynamic

/**
 * 给任意类添加此注解将会视为同IComposeMethods一样的效果，且内部需要有content方法或者ContentMarker注解标记的方法
 * @see IComposeMethods
 * @see IComposeMethods.content
 * @see ContentMarker
 */
annotation class ComposeMethodsMarker