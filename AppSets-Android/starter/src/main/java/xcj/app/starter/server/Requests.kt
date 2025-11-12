package xcj.app.starter.server

import xcj.app.starter.foundation.http.DesignResponse

data class HttpRequestFail(
    val url: String? = null,
    val response: DesignResponse<*>? = null
) : Exception()

suspend inline fun <T> request(
    action: suspend () -> DesignResponse<T>,
): Result<T> {
    val designResponse = runCatching {
        action.invoke()
    }.getOrNull()
    if (designResponse == null) {
        val httpRequestFail = HttpRequestFail()
        return Result.failure(httpRequestFail)
    }
    if (designResponse.success && designResponse.data != null) {
        return Result.success(designResponse.data)
    }
    val httpRequestFail =
        HttpRequestFail(response = designResponse)
    return Result.failure(httpRequestFail)

}

suspend inline fun <T> requestRaw(
    action: suspend () -> T,
): Result<T> {
    return runCatching {
        action()
    }
}