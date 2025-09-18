package xcj.app.starter.server

import xcj.app.starter.foundation.http.DesignResponse

data class RequestFail(
    var code: Int,
    var info: String,
    var e: Throwable? = null,
    var url: String? = null,
    var methodName: String? = null
)

suspend inline fun <T> requestBridge(
    action: suspend () -> DesignResponse<T>,
): T? {
    runCatching {
        action.invoke()
    }.onSuccess {
        if (it.success && it.data != null) {
            return it.data
        } else {
            return null
        }
    }.onFailure {
        return null
    }
    return null
}

suspend inline fun <T> request(
    action: suspend () -> DesignResponse<T>,
    noinline onSuccess: (suspend (T?) -> Unit)? = null,
    noinline onFailed: (suspend (RequestFail) -> Unit)? = null,
) {
    runCatching {
        action.invoke()
    }.onSuccess {
        if (it.success) {
            onSuccess?.invoke(it.data)
        } else {
            val failInfo =
                RequestFail(it.code, it.info ?: "", null, "unknown url")
            onFailed?.invoke(failInfo)
        }
    }.onFailure {
        onFailed?.invoke(
            RequestFail(-99, it.message ?: "network layer error", it)
        )
    }
}

suspend inline fun <T> requestNotNull(
    action: suspend () -> DesignResponse<T>,
    noinline onSuccess: (suspend (T) -> Unit)? = null,
    noinline onFailed: (suspend (RequestFail) -> Unit)? = null,
) {
    runCatching {
        action.invoke()
    }.onSuccess {
        val data = it.data
        if (it.success && data != null) {
            onSuccess?.invoke(data)
        } else {
            val failInfo =
                RequestFail(it.code, it.info ?: "", null, "unknown url")
            onFailed?.invoke(failInfo)
        }
    }.onFailure {
        onFailed?.invoke(
            RequestFail(-1, it.message ?: "network layer error", it)
        )
    }
}

suspend inline fun <T> requestRaw(
    action: suspend () -> T?,
    noinline onSuccess: (suspend (T?) -> Unit)? = null,
    noinline onFailed: (suspend (RequestFail) -> Unit)? = null,
) {
    var methodStackName: String? = null
    runCatching {
        methodStackName = Thread.currentThread().stackTrace.getOrNull(1)?.methodName
        action()
    }.onSuccess {
        onSuccess?.invoke(it)
    }.onFailure {
        onFailed?.invoke(
            RequestFail(-1, it.message ?: "network layer error", it, methodName = methodStackName)
        )
    }
}

suspend inline fun <T> requestNotNullRaw(
    action: suspend () -> T,
    noinline onSuccess: (suspend ((T) -> Unit))? = null,
    noinline onFailed: (suspend (RequestFail) -> Unit)? = null,
) {
    var methodStackName: String? = null
    runCatching {
        methodStackName = Thread.currentThread().stackTrace.getOrNull(1)?.methodName
        action()
    }.onSuccess {
        onSuccess?.invoke(it)
    }.onFailure {
        onFailed?.invoke(
            RequestFail(-1, it.message ?: "network layer error", it, methodName = methodStackName)
        )
    }
}