package xcj.app.userinfo

import org.springframework.validation.BindingResult
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.BaseResponseException

class RequestParamsValidationException(
    code:Int= ApiDesignCode.ERROR_CODE_FATAL,
    message:String
):Exception(message), BaseResponseException<String> {
    override val response: DesignResponse<String> = DesignResponse(code, message)
    companion object{
        fun produceExceptionIfBindingResultContains(bindingResult: BindingResult){
            if(bindingResult.allErrors.isNotEmpty()){
                val allErrors = bindingResult.allErrors.map { it.defaultMessage }.joinToString()
                throw RequestParamsValidationException(message = allErrors)
            }
        }
    }
}