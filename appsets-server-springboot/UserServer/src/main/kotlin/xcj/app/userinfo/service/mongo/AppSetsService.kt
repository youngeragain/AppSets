package xcj.app.userinfo.service.mongo

import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.AddAppTokenParam
import xcj.app.userinfo.model.req.GetAppTokenParam

interface AppSetsService {
    fun getTokenAppSetsAppId(getAppTokenParams: GetAppTokenParam):DesignResponse<String?>
    fun createAppSetsId(addAppTokenParam: AddAppTokenParam):DesignResponse<String>

    fun appsetsClientCheckUpdate(versionCode: Int, platform:String): DesignResponse<UpdateCheckResult?>
    fun addAppSetsClientUpdate(addAppSetsClientUpdateParams:AddAppSetsVersionForPlatformParams): DesignResponse<Boolean>
    fun getApplicationClientUpdateHistory(minVersionCode: Int?, platform: String): DesignResponse<List<UpdateCheckResult>?>
}