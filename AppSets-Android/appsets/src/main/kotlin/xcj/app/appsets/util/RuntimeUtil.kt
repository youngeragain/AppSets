package xcj.app.appsets.util

fun callableName(): String? {
    val stackTrace = Thread.currentThread().stackTrace
    if (stackTrace.isNotEmpty()) {
        return stackTrace[1].methodName
    }
    return null
}

fun funName(): String? {
    val stackTrace = Thread.currentThread().stackTrace
    if (stackTrace.isNotEmpty()) {
        return stackTrace[1].methodName
    }
    return null
}