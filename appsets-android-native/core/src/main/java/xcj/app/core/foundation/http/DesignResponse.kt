package xcj.app.core.foundation.http

data class DesignResponse<A>(
    val code: Int = 0,
    val info: String? = null,
    val data: A? = null
) : IResponseStatus {
    override val success: Boolean
        get() = code == 0
}