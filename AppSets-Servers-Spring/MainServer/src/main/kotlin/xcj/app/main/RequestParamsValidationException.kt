package xcj.app.main

import org.springframework.validation.BindingResult
import xcj.app.ApiDesignCode
import xcj.app.BaseResponseException
import xcj.app.DesignResponse

class RequestParamsValidationException(
    code: Int = ApiDesignCode.ERROR_CODE_FATAL,
    message: String
) : Exception(message), BaseResponseException<String> {

    override val response: DesignResponse<String> = DesignResponse(code, message)

    companion object {
        fun produceExceptionIfBindingResultContains(bindingResult: BindingResult) {
            if (bindingResult.allErrors.isNotEmpty()) {
                val allErrors = bindingResult.allErrors.joinToString { it.defaultMessage.toString() }
                throw RequestParamsValidationException(message = allErrors)
            }
        }
    }
}