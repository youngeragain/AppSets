package xcj.app.starter.android.util

import android.util.Log
import xcj.app.starter.foundation.purple_composer.IPurpleLogger
import xcj.app.starter.foundation.purple_composer.LoggingState
import xcj.app.starter.test.Purple

class PurpleLoggerForAndroid : IPurpleLogger {

    override var enable: Boolean = true

    override fun addPurpleTagPrefix(): Boolean {
        return true
    }

    override fun withTag(tag: String): LoggingState {
        val loggingState = if (addPurpleTagPrefix()) {
            LoggingState("${Purple.TAG}|$tag")
        } else {
            LoggingState(tag)
        }
        return loggingState
    }

    override fun withMessage(message: Any?, loggingState: LoggingState) {
        loggingState.messages.add(message)
    }

    override fun withThrowable(throwable: Throwable, loggingState: LoggingState) {
        loggingState.throwable = throwable
    }

    override fun logLevel(level: String, loggingState: LoggingState) {
        when (level) {
            IPurpleLogger.LEVEL_DEBUG -> {
                Log.d(
                    loggingState.tag,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_ERROR -> {
                Log.e(
                    loggingState.tag,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_WARN -> {
                Log.w(
                    loggingState.tag,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_INFO -> {
                Log.i(
                    loggingState.tag,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_VERBOSE -> {
                Log.v(
                    loggingState.tag,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }
        }
    }

    fun d(tag: String, message: Any?, tr: Throwable? = null) {
        if (!enable) {
            return
        }
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_DEBUG, loggingState)
    }

    fun i(tag: String, message: Any?, tr: Throwable? = null) {
        if (!enable) {
            return
        }
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_INFO, loggingState)
    }

    fun e(tag: String, message: Any?, tr: Throwable? = null) {
        if (!enable) {
            return
        }
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_ERROR, loggingState)
    }

    fun w(tag: String, message: Any?, tr: Throwable? = null) {
        if (!enable) {
            return
        }
        val loggingState = withTag(tag)
        withMessage(message, loggingState)
        tr?.let {
            withThrowable(it, loggingState)
        }
        logLevel(IPurpleLogger.LEVEL_WARN, loggingState)
    }
}