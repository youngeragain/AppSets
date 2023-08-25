package xcj.app.userinfo.ktx

import org.springframework.validation.BindingResult
import xcj.app.CoreLogger
import xcj.app.userinfo.RequestParamsValidationException

fun BindingResult.produceExceptionIfBindingResultContains(){
    CoreLogger.d("blue", "bindingResult:$this")
    RequestParamsValidationException.produceExceptionIfBindingResultContains(this)
}