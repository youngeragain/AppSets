package xcj.app.appsets.ktx

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.FailDetails
import xcj.app.core.foundation.http.DesignResponse

suspend fun <T> requestBridge(
    requestAction: suspend () -> DesignResponse<T?>
):T?{
    runCatching {
        requestAction.invoke()
    }.onSuccess {
        return if(it.success){
            it.data
        }else{
            produceTokenErrorIfFound(it.code)
            null
        }
    }.onFailure {
        return null
    }
    return null
}

suspend fun <T> requestNotNullBridge(
    requestAction: suspend () -> DesignResponse<T>
):T?{
    runCatching {
        requestAction.invoke()
    }.onSuccess {
        return if(it.success&&it.data!=null){
            it.data
        }else{
            produceTokenErrorIfFound(it.code)
            null
        }
    }.onFailure {
        return null
    }
    return null
}


fun <T> CoroutineScope.request(
    requestAction: suspend () -> DesignResponse<T?>,
    onSuccess: suspend CoroutineScope.(T?) -> Unit = {},
    onFailed: suspend CoroutineScope.(FailDetails) -> Unit = {},
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Job {
    return launch(dispatcher) {
        var methodStackName:String? = null
        runCatching {
            methodStackName = Thread.getAllStackTraces()[Thread.currentThread()]?.getOrNull(0)?.methodName
            requestAction.invoke()
        }.onSuccess {
            if(it.success){
                onSuccess(it.data)
            }else{
                produceTokenErrorIfFound(it.code)
                val failInfo = FailDetails(it.code, it.info?:"", null, "unknown url", methodStackName)
                onFailed(failInfo)
            }
        }.onFailure {
            onFailed(FailDetails(-99, it.message?:"网络层错误", it, methodName = methodStackName))
        }
    }
}

fun <T> CoroutineScope.requestNotNull(
    requestAction: suspend () -> DesignResponse<T>,
    onSuccess: (suspend CoroutineScope.(T) -> Unit)? = null,
    onFailed: (suspend CoroutineScope.(FailDetails) -> Unit)? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Job {
    return launch(dispatcher) {
        var methodStackName:String? = null
        runCatching {
            methodStackName = Thread.getAllStackTraces()[Thread.currentThread()]?.getOrNull(0)?.methodName
            requestAction.invoke()
        }.onSuccess {
            if(it.success){
                onSuccess?.invoke(this, it.data!!)
            }else {
                produceTokenErrorIfFound(it.code)
                val failInfo =
                    FailDetails(it.code, it.info ?: "", null, "unknown url", methodStackName)
                onFailed?.invoke(this, failInfo)
            }
        }.onFailure {
            onFailed?.invoke(
                this,
                FailDetails(-1, it.message ?: "网络层错误", it, methodName = methodStackName)
            )
        }
    }
}

/**
 * 这种写法将尽可能的处理token过期行为
 */
fun <T> CoroutineScope.requestRaw(
    requestAction: suspend () -> T?,
    onSuccess: suspend CoroutineScope.(T?) -> Unit = {},
    onFailed: suspend CoroutineScope.(FailDetails) -> Unit = {},
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Job {
    return launch(dispatcher) {
        var methodStackName:String? = null
        runCatching {
            methodStackName = Thread.getAllStackTraces()[Thread.currentThread()]?.getOrNull(0)?.methodName
            requestAction.invoke()
        }.onSuccess {
            if (it is DesignResponse<*>) {
                produceTokenErrorIfFound(it.code)
            }
            onSuccess(it)
        }.onFailure {
            onFailed(FailDetails(-1, it.message?:"网络层错误", it, methodName = methodStackName))
        }
    }
}

/**
 * 这种写法将尽可能的处理token过期行为
 */
fun <T> CoroutineScope.requestNotNullRaw(
    requestAction: suspend CoroutineScope.() -> T,
    onSuccess: suspend (CoroutineScope.(T) -> Unit) = {},
    onFailed: suspend (FailDetails) -> Unit = {},
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Job {
    return launch(dispatcher) {
        var methodStackName:String? = null
        runCatching {
            methodStackName = Thread.getAllStackTraces()[Thread.currentThread()]?.getOrNull(0)?.methodName
            requestAction.invoke(this)
        }.onSuccess {
            if (it is DesignResponse<*>) {
                produceTokenErrorIfFound(it.code)
            }
            onSuccess(it)
        }.onFailure {
            onFailed(FailDetails(-1, it.message?:"网络层错误", it, methodName = methodStackName))
        }
    }
}

fun produceTokenErrorIfFound(code:Int){
    if(code==-2||code==-3||code==-4){
        LocalAccountManager.produceTokenError()
    }
}