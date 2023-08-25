package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignEncodeStr
import xcj.app.DesignResponse
import xcj.app.userinfo.service.external.ExternalService
import xcj.app.userinfo.service.external.TCObjectStorageRes

@RequestMapping("/appsets")
@RestController
class ExternalController(private val externalService: ExternalService) {

    @RequestMapping("external/object-storage/config/{duration}")
    fun getObjectStorageConfig(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestHeader(name = ApiDesignEncodeStr.appTokenStrToMd5) appToken:String,
        @PathVariable(name = "duration") duration: Long,
    ):DesignResponse<TCObjectStorageRes>{
        return externalService.getObjectStorageConfig(token, appToken, duration)
    }


    @RequestMapping("external/test")
    fun test():DesignResponse<Int>{
        return DesignResponse(data = (0..100).random())
    }
}