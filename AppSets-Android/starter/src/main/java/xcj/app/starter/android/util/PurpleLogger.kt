package xcj.app.starter.android.util

import xcj.app.starter.foundation.lazyStaticProvider

private fun purpleLoggingForPlatform(): PurpleLoggerForAndroid {
    return PurpleLoggerForAndroid()
}

@JvmField
val PurpleLogger = lazyStaticProvider<PurpleLoggerForAndroid>().apply {
    provide {
        purpleLoggingForPlatform()
    }
}