package xcj.app.io.components

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials
import com.tencent.qcloud.core.auth.SessionQCloudCredentials

class STSCredentialProvider(private val tencentCosInfoProvider: TencentCosInfoProvider) :
    BasicLifecycleCredentialProvider() {
    override fun fetchNewCredentials(): QCloudLifecycleCredentials {
        // 然后解析响应，获取临时密钥信息
        val tencentCosSTS = tencentCosInfoProvider.getTencentCosSTS()
        val tmpSecretId = tencentCosSTS.tmpSecretId // 临时密钥 SecretId

        val tmpSecretKey = tencentCosSTS.tmpSecretKey // 临时密钥 SecretKey

        val sessionToken = tencentCosSTS.sessionToken // 临时密钥 Token

        val serverSeconds = tencentCosSTS.serverTimeMills / 1000
        val expiredTime = serverSeconds + tencentCosSTS.duration //临时密钥有效截止时间戳，单位是秒

        //建议返回服务器时间作为签名的开始时间，避免由于用户手机本地时间偏差过大导致请求过期
        // 返回服务器时间作为签名的起始时间
        val startTime = serverSeconds //临时密钥有效起始时间，单位是秒
        // 最后返回临时密钥信息对象
        return SessionQCloudCredentials(
            tmpSecretId, tmpSecretKey,
            sessionToken, startTime, expiredTime
        )
    }
}