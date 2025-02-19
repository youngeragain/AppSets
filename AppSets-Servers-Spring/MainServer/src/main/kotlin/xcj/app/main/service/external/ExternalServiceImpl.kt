package xcj.app.main.service.external

import com.google.gson.Gson
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.main.util.TokenHelper
import xcj.app.util.PurpleLogger

@Service
class ExternalServiceImpl(
    private val restTemplate: RestTemplate,
    private val tokenHelper: TokenHelper
) : ExternalService {

    private val gson = Gson()

    override fun getObjectStorageConfig(
        token: String,
        appToken: String,
        duration: Long
    ): DesignResponse<TCObjectStorageRes> {
        try {
            val uid = tokenHelper.getUidByToken(token)
            val payloadTCOS = PayloadTCOS(uid, PayloadTCOSPolicy(statement = listOf(Statement())))
            val payloadJsonStr = gson.toJson(payloadTCOS)
            val temporaryStsAuthStr = TencentCOSTest().getTemporaryStsAuthStr("SecretId", "SecretKey", payloadJsonStr)
            val authStr = temporaryStsAuthStr.first
            val timestamp = temporaryStsAuthStr.second
            val headers = HttpHeaders()
            headers.set("Content-Type", "application/json; charset=utf-8")
            headers.set("Host", "sts.tencentcloudapi.com")
            headers.set("Authorization", authStr)
            headers.set("X-TC-Action", "GetFederationToken")
            headers.set("X-TC-Timestamp", timestamp.toString())
            headers.set("X-TC-Version", "2018-08-13")
            headers.set("X-TC-RequestClient", "SDK_JAVA_3.1.552")
            headers.set("X-TC-Region", "ap-chengdu")
            headers.set("X-TC-Language", "zh-CN")//en-US
            val httpEntity: HttpEntity<PayloadTCOS> = HttpEntity(payloadTCOS, headers)
            val responseEntity =
                restTemplate.postForEntity(
                    "https://sts.tencentcloudapi.com/",
                    httpEntity,
                    TCObjectStorageWrapperRes::class.java
                )
            PurpleLogger.current.d("blue", "responseEntity body:${responseEntity.body}")
            if (responseEntity.statusCode == HttpStatus.OK) {
                return DesignResponse(data = responseEntity.body?.Response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL)
    }
}