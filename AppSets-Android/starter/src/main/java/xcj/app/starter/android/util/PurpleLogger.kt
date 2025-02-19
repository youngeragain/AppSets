package xcj.app.starter.android.util

import xcj.app.starter.foundation.staticProvider

private fun purpleLoggingForPlatform(): PurpleLoggerForAndroid {
    return PurpleLoggerForAndroid()
}

@JvmField
val PurpleLogger = staticProvider<PurpleLoggerForAndroid>().apply {
    this provide purpleLoggingForPlatform()
}