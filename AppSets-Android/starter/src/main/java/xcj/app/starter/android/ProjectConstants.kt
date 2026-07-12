package xcj.app.starter.android

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.DesignEvent
import xcj.app.starter.foundation.KotlinObject
import xcj.app.starter.test.AndroidEvent
import xcj.app.starter.test.PurpleContextEventListener

@KotlinObject
object ProjectConstants : PurpleContextEventListener {

    private const val TAG = "ProjectConstants"

    var IS_IN_ANDROID_STUDIO_PREVIEW = true


    private fun initialRuntimeValueIfNeeded() {
        PurpleLogger.current.d(TAG, "initialRuntimeValueIfNeeded")
        IS_IN_ANDROID_STUDIO_PREVIEW = false
    }

    override suspend fun onEvent(event: DesignEvent) {
        when (event) {
            is AndroidEvent -> {
                initialRuntimeValueIfNeeded()
            }
        }
    }
}