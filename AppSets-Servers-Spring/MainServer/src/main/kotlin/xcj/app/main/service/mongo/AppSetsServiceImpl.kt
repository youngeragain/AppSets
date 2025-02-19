package xcj.app.main.service.mongo

import com.google.gson.Gson
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.main.dao.mongo.AppTokenDao
import xcj.app.main.model.common.AddAppSetsVersionForPlatformParams
import xcj.app.main.model.common.AppSetsVersionForPlatform
import xcj.app.main.model.common.UpdateCheckResult
import xcj.app.main.model.req.AddAppTokenParam
import xcj.app.main.model.req.GetAppTokenParam
import xcj.app.main.model.table.mongo.AppToken
import xcj.app.main.util.Helpers
import xcj.app.main.util.TokenHelper
import java.util.*

@Service
class AppSetsServiceImpl(
    private val appTokenDao: AppTokenDao,
    private val tokenHelper: TokenHelper,
    private val redisTemplate: StringRedisTemplate
) : AppSetsService {

    private val gson = Gson()

    override fun getTokenAppSetsAppId(getAppTokenParams: GetAppTokenParam): DesignResponse<String?> {
        val appToken =
            appTokenDao.getAppTokenByKeySecret(getAppTokenParams.appSetsAppId)
        return if (appToken != null) {
            DesignResponse(data = appToken.appToken)
        } else
            DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, data = null)

    }

    override fun createAppSetsId(addAppTokenParam: AddAppTokenParam): DesignResponse<String> {
        return if (
            addAppTokenParam.account == "9a8dcc5232be24ef84a0d02f14936ea3" &&//"Are you ok?"
            addAppTokenParam.password == "33dc70d7a44861ebbf39e2d261416fe6"
        )//"I'm not ok!"
        {
            val appSetsAppId: String = Helpers.generateAppSetsAppId()
            val appToken = tokenHelper.generateAppToken(appSetsAppId)
            appTokenDao.addToken(
                AppToken(
                    id = null,
                    appSetsAppId = appSetsAppId,
                    appToken = appToken
                )
            )
            DesignResponse(data = appSetsAppId)
        } else {
            DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, data = null)
        }
    }

    override fun appsetsClientCheckUpdate(versionCode: Int, platform: String): DesignResponse<UpdateCheckResult?> {
        val opsForZSet = redisTemplate.opsForZSet()
        val keyOfPlatform = "AppSets-appclient-for-${platform}-versions"
        if (!redisTemplate.hasKey(keyOfPlatform)) {
            return DesignResponse()
        }
        val appsetsAppClientVersionsForPlatform = opsForZSet.range(keyOfPlatform, 0, Short.MAX_VALUE.toLong())
        if (appsetsAppClientVersionsForPlatform.isNullOrEmpty()) {
            return DesignResponse()
        }
        val addAppSetsVersionForPlatformParamsList = appsetsAppClientVersionsForPlatform.map {
            gson.fromJson(it, AppSetsVersionForPlatform::class.java)
        }.sortedByDescending { it.versionCode }
        val newestVersionForPlatform = addAppSetsVersionForPlatformParamsList.first()
        return if (versionCode > newestVersionForPlatform.versionCode) {
            DesignResponse()
        } else if (versionCode == newestVersionForPlatform.versionCode) {
            DesignResponse(
                data = UpdateCheckResult(
                    versionCode, versionCode,
                    newestVersionForPlatform.version, newestVersionForPlatform.updateChangesHtml,
                    newestVersionForPlatform.forceUpdate,
                    newestVersionForPlatform.downloadUrl,
                    newestVersionForPlatform.publishDateTime ?: "1970/01/01 00:00:00",
                    false
                )
            )
        } else {
            DesignResponse(
                data = UpdateCheckResult(
                    versionCode, newestVersionForPlatform.versionCode,
                    newestVersionForPlatform.version, newestVersionForPlatform.updateChangesHtml,
                    newestVersionForPlatform.forceUpdate, newestVersionForPlatform.downloadUrl,
                    newestVersionForPlatform.publishDateTime ?: "1970/01/01 00:00:00",
                    true
                )
            )
        }
    }

    override fun addAppSetsClientUpdate(addAppSetsClientUpdateParams: AddAppSetsVersionForPlatformParams): DesignResponse<Boolean> {
        val keyOfPlatform = "AppSets-appclient-for-${addAppSetsClientUpdateParams.platform}-versions"
        val opsForZSet = redisTemplate.opsForZSet()
        val isAddVersionExist = opsForZSet.range(keyOfPlatform, 0, Short.MAX_VALUE.toLong())?.map {
            gson.fromJson(it, AddAppSetsVersionForPlatformParams::class.java)
        }?.firstOrNull {
            it.versionCode == addAppSetsClientUpdateParams.versionCode
        } != null
        if (isAddVersionExist) {
            return DesignResponse(data = false, info = "A same version has been added!")
        }
        val appSetsVersionForPlatform = AppSetsVersionForPlatform.fromAddParams(addAppSetsClientUpdateParams)
        val appSetsVersionForPlatformJson = gson.toJson(appSetsVersionForPlatform)
        val addResult = opsForZSet.add(keyOfPlatform, appSetsVersionForPlatformJson, 0.0)
        return DesignResponse(data = addResult)
    }

    override fun getApplicationClientUpdateHistory(
        minVersionCode: Int?,
        platform: String
    ): DesignResponse<List<UpdateCheckResult>?> {
        val keyOfPlatform = "AppSets-appclient-for-${platform}-versions"
        val opsForZSet = redisTemplate.opsForZSet()
        val meetWithVersions = opsForZSet.range(keyOfPlatform, 0, Short.MAX_VALUE.toLong())?.map {
            gson.fromJson(it, AppSetsVersionForPlatform::class.java)
        }?.filter {
            if (minVersionCode != null) {
                it.versionCode >= minVersionCode
            } else
                true
        }?.map {
            UpdateCheckResult(
                it.versionCode, it.versionCode,
                it.version, it.updateChangesHtml,
                it.forceUpdate,
                it.downloadUrl,
                it.publishDateTime ?: "1970/01/01 00:00:00",
                false
            )
        }
        return DesignResponse(data = meetWithVersions)
    }

    override fun updateAppSetsClientUpdate(addAppSetsVersionForPlatformParams: AddAppSetsVersionForPlatformParams): DesignResponse<Boolean> {
        val keyOfPlatform = "AppSets-appclient-for-${addAppSetsVersionForPlatformParams.platform}-versions"
        val opsForZSet = redisTemplate.opsForZSet()
        val existVersion = opsForZSet.range(keyOfPlatform, 0, Short.MAX_VALUE.toLong())?.map {
            gson.fromJson(it, AddAppSetsVersionForPlatformParams::class.java)
        }?.firstOrNull {
            it.versionCode == addAppSetsVersionForPlatformParams.versionCode
        }
        if (existVersion == null)
            return DesignResponse(data = false, info = "no version founded when update!")
        //TODO delete bug
        opsForZSet.remove(keyOfPlatform, gson.toJson(existVersion))
        val appSetsVersionForPlatform = AppSetsVersionForPlatform.fromAddParams(addAppSetsVersionForPlatformParams)
        val appSetsVersionForPlatformJson = gson.toJson(appSetsVersionForPlatform)
        val addResult = opsForZSet.add(keyOfPlatform, appSetsVersionForPlatformJson, 0.0)
        return DesignResponse(data = addResult)
    }

    override fun getIMBrokerProperties(): DesignResponse<String?> {
        val str = redisTemplate.opsForValue().get(KEY_IM_BROKER_PROPERTIES)
        if (str.isNullOrEmpty()) {
            return DesignResponse()
        }
        val encoded = Base64.getEncoder().encodeToString(str.toByteArray())
        return DesignResponse(data = encoded)
    }

    companion object {
        private const val KEY_IM_BROKER_PROPERTIES = "im_broker_properties"
    }
}