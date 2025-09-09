package xcj.app.starter.foundation.http

data class DesignResponse<A>(
    val code: Int = 0,
    val info: String? = null,
    val data: A? = null
) {
    val success: Boolean
        get() = code == 0

    companion object {
        val NOT_FOUND: DesignResponse<Any>
            get() = DesignResponse(code = 404, data = null)
        val BAD_REQUEST: DesignResponse<Any>
            get() = DesignResponse(code = 400, data = null)
    }
}