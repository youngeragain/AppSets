package xcj.app.compose_share.foundation_extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> T.rememberState(): State<T> {
    val s = remember {
        mutableStateOf(this)
    }
    return s
}

@Composable
fun <T> T.remember(): T {
    val s = remember {
        this
    }
    return s
}