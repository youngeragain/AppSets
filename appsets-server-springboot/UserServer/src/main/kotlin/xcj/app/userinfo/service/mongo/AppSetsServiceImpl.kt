package xcj.app.userinfo.service.mongo

import com.google.gson.Gson
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.userinfo.Helpers
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mongo.AppTokenDao
import xcj.app.userinfo.model.req.AddAppTokenParam
import xcj.app.userinfo.model.req.GetAppTokenParam
import xcj.app.userinfo.model.table.mongo.AppToken
import java.text.SimpleDateFormat
import java.util.*

@Service
class AppSetsServiceImpl(
    private val appTokenDao: AppTokenDao,
    private val tokenHelper: TokenHelper,
    private val redisTemplate: StringRedisTemplate
):AppSetsService {
    override fun getTokenAppSetsAppId(getAppTokenParams: GetAppTokenParam):DesignResponse<String?> {
        val appToken =
            appTokenDao.getAppTokenByKeySecret(getAppTokenParams.appSetsAppId)
        return if(appToken!=null){
            DesignResponse(data = appToken.appToken)
        }else
            DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, data = null)

    }

    override fun createAppSetsId(addAppTokenParam: AddAppTokenParam): DesignResponse<String> {
        return if(addAppTokenParam.account=="9a8dcc5232be24ef84a0d02f14936ea3"&&//"Are you ok?"
            addAppTokenParam.password=="33dc70d7a44861ebbf39e2d261416fe6")//"I'm not ok!"
        {
            val appSetsAppId:String = Helpers.generateAppSetsAppId()
            val appToken = tokenHelper.generateAppToken(appSetsAppId)
            appTokenDao.addToken(AppToken(
                id = null,
                appSetsAppId = appSetsAppId,
                appToken = appToken))
            DesignResponse(data = appSetsAppId)
        }else{
            DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, data = null)
        }
    }

    override fun appsetsClientCheckUpdate(versionCode: Int, platform: String): DesignResponse<UpdateCheckResult?> {
        val opsForZSet = redisTemplate.opsForZSet()
        val keyOfPlatform = "AppSets-appclient-for-${platform}-versions"
        if(!redisTemplate.hasKey(keyOfPlatform))
            return DesignResponse()
        val appsetsAppClientVersionsForPlatform = opsForZSet.range(keyOfPlatform, 0, Short.MAX_VALUE.toLong())
        if(appsetsAppClientVersionsForPlatform.isNullOrEmpty())
            return DesignResponse()
        val gson = Gson()
        val addAppSetsVersionForPlatformParamsList = appsetsAppClientVersionsForPlatform.map {
            gson.fromJson(it, AppSetsVersionForPlatform::class.java)
        }.sortedByDescending { it.versionCode }
        val newestVersionForPlatform = addAppSetsVersionForPlatformParamsList.first()
        return if(versionCode>newestVersionForPlatform.versionCode){
            DesignResponse()
        } else if(versionCode==newestVersionForPlatform.versionCode){
            DesignResponse(data = UpdateCheckResult(versionCode, versionCode,
                newestVersionForPlatform.version, newestVersionForPlatform.updateChangesHtml,
                newestVersionForPlatform.forceUpdate,
                newestVersionForPlatform.downloadUrl,
                newestVersionForPlatform.publishDateTime?:"1970/01/01 00:00:00",
                false))
        }else{
            DesignResponse(data = UpdateCheckResult(versionCode, newestVersionForPlatform.versionCode,
                newestVersionForPlatform.version, newestVersionForPlatform.updateChangesHtml,
                newestVersionForPlatform.forceUpdate, newestVersionForPlatform.downloadUrl,
                newestVersionForPlatform.publishDateTime?:"1970/01/01 00:00:00",
                true)
            )
        }
    }

    override fun addAppSetsClientUpdate(addAppSetsClientUpdateParams: AddAppSetsVersionForPlatformParams): DesignResponse<Boolean> {
        val keyOfPlatform = "AppSets-appclient-for-${addAppSetsClientUpdateParams.platform}-versions"
        val opsForZSet = redisTemplate.opsForZSet()
        val gson = Gson()
        val isAddVersionExist = opsForZSet.range(keyOfPlatform, 0, Short.MAX_VALUE.toLong())?.map {
            gson.fromJson(it, AddAppSetsVersionForPlatformParams::class.java)
        }?.firstOrNull {
            it.versionCode == addAppSetsClientUpdateParams.versionCode
        }!=null
        if(isAddVersionExist)
            return DesignResponse(data = false, info = "A same version has been added!")
        val appSetsVersionForPlatform = AppSetsVersionForPlatform.fromAddParams(addAppSetsClientUpdateParams)
        val appSetsVersionForPlatformJson = gson.toJson(appSetsVersionForPlatform)
        val addResult = opsForZSet.add(keyOfPlatform, appSetsVersionForPlatformJson, 0.0)
        return DesignResponse(data = addResult)
    }

    override fun getApplicationClientUpdateHistory(minVersionCode: Int?, platform: String): DesignResponse<List<UpdateCheckResult>?> {
        val keyOfPlatform = "AppSets-appclient-for-${platform}-versions"
        val opsForZSet = redisTemplate.opsForZSet()
        val gson = Gson()
        val meetWithVersions = opsForZSet.range(keyOfPlatform, 0, Short.MAX_VALUE.toLong())?.map {
            gson.fromJson(it, AppSetsVersionForPlatform::class.java)
        }?.filter {
            if(minVersionCode!=null){
                it.versionCode>=minVersionCode
            }else
                true
        }?.map {
            UpdateCheckResult(it.versionCode, it.versionCode,
                it.version, it.updateChangesHtml,
                it.forceUpdate,
                it.downloadUrl,
                it.publishDateTime?:"1970/01/01 00:00:00",
                false)
        }
        return DesignResponse(data = meetWithVersions)
    }
}


data class UpdateCheckResult(
    val versionCode: Int,
    val newestVersionCode:Int,
    val newestVersion:String?,
    val updateChangesHtml:String?,
    val forceUpdate:Boolean?,
    val downloadUrl:String?,
    val publishDateTime: String?,
    val canUpdate:Boolean)

data class AddAppSetsVersionForPlatformParams(
    val versionCode: Int,
    val version:String,
    val platform:String,
    val forceUpdate:Boolean,
    val updateChangesHtml: String,
    val downloadUrl: String?)

data class AppSetsVersionForPlatform(
    val versionCode: Int,
    val version:String,
    val platform:String,
    val forceUpdate:Boolean,
    val updateChangesHtml: String,
    val downloadUrl: String?,
    val publishDateTime:String?,
    val publishTimestamp: Long?,
){
    companion object{
        fun fromAddParams(addParams: AddAppSetsVersionForPlatformParams): AppSetsVersionForPlatform {
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            val publishDateTime = simpleDateFormat.format(calendar.time)
            return AppSetsVersionForPlatform(
                addParams.versionCode, addParams.version,
                addParams.platform, addParams.forceUpdate, addParams.updateChangesHtml,
                addParams.downloadUrl,
                publishDateTime,
                calendar.time.time
            )
        }
    }
}