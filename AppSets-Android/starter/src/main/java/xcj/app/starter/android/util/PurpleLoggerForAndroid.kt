package xcj.app.starter.android.util

import android.util.Log
import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.purple_composer.IPurpleLogger
import xcj.app.starter.foundation.purple_composer.LoggingState
import xcj.app.starter.test.Purple

class PurpleLoggerForAndroid : IPurpleLogger {

    override var enable: Boolean = true

    override fun tagPrefix(): String? {
        return Purple.TAG
    }

    private fun withTag(tag: String): LoggingState {
        val loggingState = LoggingState(tag, tagPrefix())
        return loggingState
    }

    private fun withMessage(loggingState: LoggingState, message: Any?) {
        loggingState.messages.add(message)
    }

    private fun withThrowable(loggingState: LoggingState, throwable: Throwable?) {
        loggingState.throwable = throwable
    }

    private fun withModule(loggingState: LoggingState, moduleId: Identifiable<String>?) {
        loggingState.moduleId = moduleId
    }

    override fun logWithLevel(level: String, loggingState: LoggingState) {
        val tagOverride = loggingState.tagOverride
        when (level) {
            IPurpleLogger.LEVEL_DEBUG -> {
                Log.d(
                    tagOverride,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_ERROR -> {
                Log.e(
                    tagOverride,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_WARN -> {
                Log.w(
                    tagOverride,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_INFO -> {
                Log.i(
                    tagOverride,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }

            IPurpleLogger.LEVEL_VERBOSE -> {
                Log.v(
                    tagOverride,
                    loggingState.messages.joinToString(", "),
                    loggingState.throwable
                )
            }
        }
    }

    fun d(
        tag: String,
        message: Any?,
        tr: Throwable? = null,
        logModuleInfoProvider: LogModuleInfoProvider? = null
    ) {
        if (!enable) {
            return
        }
        if (logModuleInfoProvider != null && !logModuleInfoProvider.enable) {
            return
        }
        val loggingState = withTag(tag)
        withModule(loggingState, logModuleInfoProvider?.key())
        withMessage(loggingState, message)
        withThrowable(loggingState, tr)
        logWithLevel(IPurpleLogger.LEVEL_DEBUG, loggingState)
    }

    fun i(
        tag: String,
        message: Any?,
        tr: Throwable? = null,
        logModuleInfoProvider: LogModuleInfoProvider? = null
    ) {
        if (!enable) {
            return
        }
        if (logModuleInfoProvider != null && !logModuleInfoProvider.enable) {
            return
        }
        val loggingState = withTag(tag)
        withModule(loggingState, logModuleInfoProvider?.key())
        withMessage(loggingState, message)
        withThrowable(loggingState, tr)
        logWithLevel(IPurpleLogger.LEVEL_INFO, loggingState)
    }

    fun e(
        tag: String,
        message: Any?,
        tr: Throwable? = null,
        logModuleInfoProvider: LogModuleInfoProvider? = null
    ) {
        if (!enable) {
            return
        }
        if (logModuleInfoProvider != null && !logModuleInfoProvider.enable) {
            return
        }
        val loggingState = withTag(tag)
        withModule(loggingState, logModuleInfoProvider?.key())
        withMessage(loggingState, message)
        withThrowable(loggingState, tr)
        logWithLevel(IPurpleLogger.LEVEL_ERROR, loggingState)
    }

    fun w(
        tag: String,
        message: Any?,
        tr: Throwable? = null,
        logModuleInfoProvider: LogModuleInfoProvider? = null
    ) {
        if (!enable) {
            return
        }
        if (logModuleInfoProvider != null && !logModuleInfoProvider.enable) {
            return
        }
        val loggingState = withTag(tag)
        withModule(loggingState, logModuleInfoProvider?.key())
        withMessage(loggingState, message)
        withThrowable(loggingState, tr)
        logWithLevel(IPurpleLogger.LEVEL_WARN, loggingState)
    }
}