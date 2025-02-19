package xcj.app.util

private fun purpleLoggingForPlatform(): PurpleLoggerForJVM {
    return PurpleLoggerForJVM()
}

@JvmField
val PurpleLogger = staticProvider<PurpleLoggerForJVM>().apply {
    this provide purpleLoggingForPlatform()
}