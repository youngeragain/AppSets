package xcj.app

interface BaseResponseException<T> {
    val response: DesignResponse<T>
}

class ApiDesignPermissionException(
    override val message: String? = null,
    code: Int = ApiDesignCode.ERROR_CODE_FATAL
) : BaseResponseException<String>, Exception(message) {
    override val response: DesignResponse<String> = DesignResponse(code, message)
}

