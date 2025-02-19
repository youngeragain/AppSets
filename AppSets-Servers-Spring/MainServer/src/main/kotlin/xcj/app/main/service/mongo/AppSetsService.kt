package xcj.app.main.service.mongo

import xcj.app.DesignResponse
import xcj.app.main.model.common.AddAppSetsVersionForPlatformParams
import xcj.app.main.model.common.UpdateCheckResult
import xcj.app.main.model.req.AddAppTokenParam
import xcj.app.main.model.req.GetAppTokenParam

interface AppSetsService {

    fun getTokenAppSetsAppId(getAppTokenParams: GetAppTokenParam): DesignResponse<String?>

    fun createAppSetsId(addAppTokenParam: AddAppTokenParam): DesignResponse<String>

    fun appsetsClientCheckUpdate(versionCode: Int, platform: String): DesignResponse<UpdateCheckResult?>

    fun addAppSetsClientUpdate(addAppSetsClientUpdateParams: AddAppSetsVersionForPlatformParams): DesignResponse<Boolean>

    fun getApplicationClientUpdateHistory(
        minVersionCode: Int?,
        platform: String
    ): DesignResponse<List<UpdateCheckResult>?>

    fun updateAppSetsClientUpdate(addAppSetsVersionForPlatformParams: AddAppSetsVersionForPlatformParams): DesignResponse<Boolean>

    fun getIMBrokerProperties(): DesignResponse<String?>
}