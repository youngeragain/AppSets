package xcj.app.main.ktx

import org.springframework.validation.BindingResult
import xcj.app.main.RequestParamsValidationException
import xcj.app.util.PurpleLogger

private const val TAG = "BindingResultKTX"

fun BindingResult.produceExceptionIfBindingResultContains() {
    PurpleLogger.current.d(TAG, "produceExceptionIfBindingResultContains, bindingResult:$this")
    RequestParamsValidationException.produceExceptionIfBindingResultContains(this)
}