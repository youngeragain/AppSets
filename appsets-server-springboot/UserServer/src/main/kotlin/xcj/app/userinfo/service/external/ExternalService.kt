package xcj.app.userinfo.service.external

import xcj.app.DesignResponse

interface ExternalService {
    fun getObjectStorageConfig(token:String, appToken:String, duration:Long):DesignResponse<TCObjectStorageRes>
}