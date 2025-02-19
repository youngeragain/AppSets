package xcj.app.main

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import xcj.app.BaseResponseException
import xcj.app.DesignResponse
import xcj.app.util.PurpleLogger

@ControllerAdvice
class SharedExceptionHandler {

    companion object {
        private const val TAG = "SharedExceptionHandler"
    }

    @ExceptionHandler
    fun handException(exception: Exception): ResponseEntity<DesignResponse<*>> {
        return when (exception) {
            is BaseResponseException<*> -> {
                ResponseEntity.ok(exception.response)
            }

            is HttpMessageNotReadableException -> {
                ResponseEntity.ok(DesignResponse.somethingWentWrong(exception.message, data = null))
            }

            else -> {
                PurpleLogger.current.d(TAG, "handException, other exception:${exception.message}}")
                ResponseEntity.ok(DesignResponse.somethingWentWrong(data = null))
            }
        }
    }
}