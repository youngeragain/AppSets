package xcj.app.userinfo.service.external


import com.google.gson.Gson
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.CoreLogger
import xcj.app.userinfo.TokenHelper

@Service
class ExternalServiceImpl(
    private val restTemplate: RestTemplate,
    private val tokenHelper: TokenHelper
    ): ExternalService {
    private val sid = "SecretId"
    private val sk = "SecretKey"
    private val gson = Gson()
    override fun getObjectStorageConfig(token:String, appToken:String, duration: Long): DesignResponse<TCObjectStorageRes> {
        try {
            val uid = tokenHelper.getUidByToken(token)
            val payloadTCOS = PayloadTCOS(uid, PayloadTCOSPolicy(statement = listOf(Statement())))
            val payloadJsonStr = gson.toJson(payloadTCOS)
            val temporaryStsAuthStr = getTemporaryStsAuthStr(sid, sk, payloadJsonStr)
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
            val httpEntity:HttpEntity<PayloadTCOS> = HttpEntity(payloadTCOS, headers)
            val responseEntity =
                restTemplate.postForEntity("https://sts.tencentcloudapi.com/", httpEntity, TCObjectStorageWrapperRes::class.java)
            CoreLogger.d("blue", "responseEntity body:${responseEntity.body}")
            if(responseEntity.statusCode==HttpStatus.OK)
                return DesignResponse(data = responseEntity.body?.Response)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL)
    }
}

data class Statement(val effect:String="allow",
                     val action:List<String> = listOf("name/cos:PutObject", "name/cos:GetObject"),
                     val resource:List<String> = listOf("qcs::cos:ap-chengdu:uid/1258462798:appsets-2022-1258462798/pics/*")
)
data class PayloadTCOSPolicy(val version:String="2.0", val statement:List<Statement>)

data class PayloadTCOS(val Name:String, val Policy:PayloadTCOSPolicy, val DurationSeconds:Long?=null)

data class TCObjectStorageWrapperRes(
    val Response: TCObjectStorageRes?
)

data class TCObjectStorageRes(
    val Credentials: Credentials?,
    val ExpiredTime: Int?,
    val RequestId: String?
)

data class Credentials(
    val TmpSecretId: String?,
    val TmpSecretKey: String?,
    val Token: String?
)