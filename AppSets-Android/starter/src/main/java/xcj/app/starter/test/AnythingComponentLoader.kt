package xcj.app.starter.test

import xcj.app.starter.android.util.PurpleLogger

class AnythingComponentLoader {

    companion object {
        private const val TAG = "AnythingComponentLoader"
    }

    fun loadComponents(purpleContext: PurpleContext) {
        if (purpleContext is SimplePurpleForAndroidContext) {
            val components = purpleContext.definitionAnythingComponents
            PurpleLogger.current.d(TAG, "loadComponents, components:[${components.joinToString()}]")
            components.forEach { anythingClassName ->
                runCatching {
                    Class.forName(anythingClassName)
                }.onSuccess {
                    PurpleLogger.current.d(
                        TAG,
                        "loadComponents, component:${anythingClassName} ready!"
                    )
                }.onFailure {
                    PurpleLogger.current.d(
                        TAG,
                        "loadComponents, component:${anythingClassName} failed!"
                    )
                }
            }
        }

    }
}