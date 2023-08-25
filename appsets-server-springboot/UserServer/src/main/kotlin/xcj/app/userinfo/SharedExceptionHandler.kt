package xcj.app.userinfo

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import xcj.app.DesignResponse
import xcj.app.BaseResponseException
import xcj.app.CoreLogger

@ControllerAdvice
class SharedExceptionHandler{
    @ExceptionHandler
    fun handException(exception: Exception):ResponseEntity<DesignResponse<*>>{
        return when (exception) {
            is BaseResponseException<*> -> {
                ResponseEntity.ok(exception.response)
            }
            is HttpMessageNotReadableException -> {
                ResponseEntity.ok(DesignResponse.somethingWentWrong(exception.message, t=null))
            }
            else -> {
                CoreLogger.d("red", "Other exception:${exception}\n${exception.stackTraceToString()}")
                ResponseEntity.ok(DesignResponse.somethingWentWrong(t=null))
            }
        }
    }
}