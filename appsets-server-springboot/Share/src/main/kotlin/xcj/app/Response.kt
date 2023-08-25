package xcj.app

data class DesignResponse<D>(
    val code: Int = ApiDesignCode.CODE_DEFAULT,
    val info: String?=null,
    val data: D?=null
){
    companion object{
        fun <T> somethingWentWrong(message:String?="Something went wrong!", t:T? = null) =
            DesignResponse<T>(ApiDesignCode.ERROR_CODE_FATAL, info = message, data = t)
        fun True() =
            DesignResponse(data = true)

        fun False() =
            DesignResponse(data = false)
    }
}

