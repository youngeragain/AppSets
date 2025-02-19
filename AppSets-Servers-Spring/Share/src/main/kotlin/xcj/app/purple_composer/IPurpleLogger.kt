package xcj.app.purple_composer

import xcj.app.DesignLogger

interface IPurpleLogger : DesignLogger {

    fun withTag(tag: String): LoggingState

    fun withMessage(message: Any?, loggingState: LoggingState)

    fun withThrowable(throwable: Throwable, loggingState: LoggingState)

    fun logLevel(level: String, loggingState: LoggingState)

    companion object {
        const val LEVEL_VERBOSE = "VERBOSE"
        const val LEVEL_INFO = "INFO"
        const val LEVEL_DEBUG = "DEBUG"
        const val LEVEL_WARN = "WARN"
        const val LEVEL_ERROR = "ERROR"
    }

}