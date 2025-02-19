package xcj.app

data class DesignResponse<D>(
    val code: Int = ApiDesignCode.CODE_DEFAULT,
    val info: String? = null,
    val data: D? = null
) {
    companion object {
        fun <D> somethingWentWrong(message: String? = "Something went wrong!", data: D? = null) =
            DesignResponse<D>(ApiDesignCode.ERROR_CODE_FATAL, info = message, data = data)

        fun True() = DesignResponse(data = true)

        fun False() = DesignResponse(data = false)
    }
}

