package xcj.app.appsets.ui.compose.custom_component

import androidx.activity.BackEventCompat
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import xcj.app.appsets.ui.compose.LocalBackPressedDispatcher
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "BackPressHandler"

@Composable
fun BackPressHandler(onBackPressed: (OnBackPressedCallback?) -> Unit) {
    // Safely update the current `onBack` lambda when a new one is provided
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)
    // Remember in Composition a back callback that calls the `onBackPressed` lambda
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                PurpleLogger.current.d(TAG, "OnBackPressedCallback, handleOnBackPressed")
                currentOnBackPressed(this)
            }

            override fun handleOnBackStarted(backEvent: BackEventCompat) {
                PurpleLogger.current.d(TAG, "OnBackPressedCallback, handleOnBackStarted")
            }

            override fun handleOnBackCancelled() {
                PurpleLogger.current.d(TAG, "OnBackPressedCallback, handleOnBackCancelled")
            }

            override fun handleOnBackProgressed(backEvent: BackEventCompat) {
                PurpleLogger.current.d(TAG, "OnBackPressedCallback, handleOnBackProgressed")
            }
        }
    }

    val backDispatcher = LocalBackPressedDispatcher.current

    // Whenever there's a new dispatcher set up the callback
    DisposableEffect(backDispatcher) {
        PurpleLogger.current.d(TAG, "DisposableEffect, addCallback")
        backDispatcher.addCallback(backCallback)
        // When the effect leaves the Composition, or there's a new dispatcher, remove the callback
        onDispose {
            PurpleLogger.current.d(TAG, "DisposableEffect onDispose, removeCallback")
            backCallback.remove()
        }
    }
}