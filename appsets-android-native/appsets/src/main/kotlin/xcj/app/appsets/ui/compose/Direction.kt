package xcj.app.appsets.ui.compose

sealed interface Direction {
    object START : Direction
    object LEFT : Direction
    object TOP : Direction
    object END : Direction
    object RIGHT : Direction
    object BOTTOM : Direction
}