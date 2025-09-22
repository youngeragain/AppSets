package xcj.app.io.tencent

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials
import com.tencent.qcloud.core.auth.SessionQCloudCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

class STSCredentialProvider(
    private val coroutineScope: CoroutineScope,
    private val tencentCosInfoProvider: TencentCosInfoProvider,
) :
    BasicLifecycleCredentialProvider() {
    override fun fetchNewCredentials(): QCloudLifecycleCredentials {
        // 然后解析响应，获取临时密钥信息
        val tencentCosSTS = runBlocking(coroutineScope.coroutineContext) {
            tencentCosInfoProvider.getTencentCosSTS()
                ?: throw IllegalStateException("fetchNewCredentials failed with null sts info.")
        }

        val tmpSecretId = tencentCosSTS.tmpSecretId

        val tmpSecretKey = tencentCosSTS.tmpSecretKey

        val sessionToken = tencentCosSTS.sessionToken

        val serverSeconds = tencentCosSTS.serverTimeMills / 1000

        val expiredTime = serverSeconds + tencentCosSTS.duration + 43200

        val startTime = serverSeconds

        return SessionQCloudCredentials(
            tmpSecretId, tmpSecretKey,
            sessionToken, startTime,
            expiredTime
        )
    }
}