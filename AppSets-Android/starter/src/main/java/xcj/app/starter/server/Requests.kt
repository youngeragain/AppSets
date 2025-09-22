package xcj.app.starter.server

import xcj.app.starter.foundation.http.DesignResponse

data class RequestFail(
    var code: Int,
    var info: String,
    var e: Throwable? = null,
    var url: String? = null,
    var methodName: String? = null,
) : Exception()

suspend inline fun <T> request(
    action: suspend () -> DesignResponse<T>,
): Result<T> {

    val designResponse = runCatching {
        action.invoke()
    }.getOrNull()
    if (designResponse == null) {
        val failInfo =
            RequestFail(-1, "", null, "response is null")
        return Result.failure(failInfo)
    }
    if (designResponse.success && designResponse.data != null) {
        return Result.success(designResponse.data)
    } else {
        val failInfo =
            RequestFail(-2, "", null, "response is not valid")
        return Result.failure(failInfo)
    }
}

suspend inline fun <T> requestRaw(
    action: suspend () -> T,
): Result<T> {
    val result = runCatching {
        action()
    }.getOrNull()
    if (result == null) {
        val failInfo =
            RequestFail(-1, "", null, "response is null")
        return Result.failure(failInfo)
    }
    return Result.success(result)
}