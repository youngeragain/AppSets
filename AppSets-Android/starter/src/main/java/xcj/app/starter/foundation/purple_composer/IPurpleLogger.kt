package xcj.app.starter.foundation.purple_composer

import xcj.app.starter.foundation.DesignLogger

interface IPurpleLogger : DesignLogger {

    fun tagPrefix(): String?

    fun logWithLevel(level: String, loggingState: LoggingState)

    companion object {
        const val LEVEL_VERBOSE = "VERBOSE"
        const val LEVEL_INFO = "INFO"
        const val LEVEL_DEBUG = "DEBUG"
        const val LEVEL_WARN = "WARN"
        const val LEVEL_ERROR = "ERROR"
    }

}