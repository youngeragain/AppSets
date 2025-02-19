package xcj.app.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xcj.app.purple_composer.IPurpleLogger
import xcj.app.purple_composer.LoggingState
import kotlin.concurrent.getOrSet

class PurpleLoggerForJVM : IPurpleLogger {

    private val sJvmLogger: ThreadLocal<Logger> = ThreadLocal()

    override fun withTag(tag: String): LoggingState {
        val loggingState = LoggingState(tag)
        return loggingState
    }

    override fun withMessage(message: Any?, loggingState: LoggingState) {
        loggingState.messages.add(message)
    }

    override fun withThrowable(throwable: Throwable, loggingState: LoggingState) {
        loggingState.throwable = throwable
    }

    override fun logLevel(level: String, loggingState: LoggingState) {
        val jvmLogger = sJvmLogger.getOrSet {
            LoggerFactory.getLogger("PurpleLogger")
        }
        when (level) {
            IPurpleLogger.LEVEL_DEBUG -> {
                jvmLogger.debug("${alignTagLength(loggingState.tag)}: ${loggingState.messages}")
            }

            IPurpleLogger.LEVEL_INFO -> {
                jvmLogger.info("${alignTagLength(loggingState.tag)}: ${loggingState.messages}")
            }

            IPurpleLogger.LEVEL_ERROR -> {
                jvmLogger.error("${alignTagLength(loggingState.tag)}: ${loggingState.messages}")
            }
        }

        loggingState.clear()
    }

    private fun alignTagLength(tag: String?): String? {
        return tag
    }

    fun d(tag: String, message: Any?, tr: Throwable? = null) {
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_INFO, loggingState)
    }

    fun i(tag: String, message: Any?, tr: Throwable? = null) {
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_INFO, loggingState)
    }

    fun e(tag: String, message: Any?, tr: Throwable? = null) {
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_ERROR, loggingState)
    }

    fun w(tag: String, message: Any?, tr: Throwable? = null) {
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_WARN, loggingState)
    }
}