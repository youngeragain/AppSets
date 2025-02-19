package xcj.app.main.service.external

import com.tencent.cloud.CosStsClient
import com.tencent.cloud.Response
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.main.model.common.TencentCosRegionBucket
import xcj.app.main.model.common.TencentCosSTS
import java.util.*

@Service
class SimpleThirdPartServiceImplV1(val redisTemplate: StringRedisTemplate) : ThirdPartService {
    companion object {
        const val KEY_TENCENT_COS = "thirdpart_tencent_cos"
    }

    override fun getTencentCosSTS(): DesignResponse<TencentCosSTS?> {
        try {
            val opsForHash = redisTemplate.opsForHash<String, String>()
            val subAccountSecretId = opsForHash.get(KEY_TENCENT_COS, "SecretId")
            val subAccountSecretKey = opsForHash.get(KEY_TENCENT_COS, "SecretKey")
            val subAccountBucketName = opsForHash.get(KEY_TENCENT_COS, "BucketName")
            val subAccountRegion = opsForHash.get(KEY_TENCENT_COS, "Region")
            val subAccountSTSHost = opsForHash.get(KEY_TENCENT_COS, "STSHost")
            val subAccountSTSDuration = opsForHash.get(KEY_TENCENT_COS, "STSDuration")
            val subAccountSTSAllowPrefixes = opsForHash.get(KEY_TENCENT_COS, "STSAllowPrefixes")
            val subAccountSTSAllowActions = opsForHash.get(KEY_TENCENT_COS, "STSAllowActions")
            if (subAccountSecretId.isNullOrEmpty() || subAccountSecretKey.isNullOrEmpty()
                || subAccountBucketName.isNullOrEmpty() || subAccountRegion.isNullOrEmpty()
                || subAccountSTSDuration.isNullOrEmpty()
            ) {
                return DesignResponse()
            }

            val config = TreeMap<String, Any>()
            // 云 api 密钥 SecretId
            config["secretId"] = subAccountSecretId
            // 云 api 密钥 SecretKey
            config["secretKey"] = subAccountSecretKey
            /* if (properties.containsKey("https.proxyHost")) {
                 System.setProperty("https.proxyHost", properties.getProperty("https.proxyHost"))
                 System.setProperty("https.proxyPort", properties.getProperty("https.proxyPort"))
             }*/
            // 设置域名,可通过此方式设置内网域名
            subAccountSTSHost?.let {
                config["host"] = it
            }
            // 临时密钥有效时长，单位是秒
            val duration = subAccountSTSDuration.toIntOrNull() ?: 1800
            config["durationSeconds"] = duration

            // 换成你的 bucket
            config["bucket"] = subAccountBucketName
            // 换成 bucket 所在地区
            config["region"] = subAccountRegion

            // 可以通过 allowPrefixes 指定前缀数组, 例子： a.jpg 或者 a/* 或者 * (使用通配符*存在重大安全风险, 请谨慎评估使用)
            /**
             * arrayOf(
             *   "exampleobject",
             *   "exampleobject2"
             * )
             */
            subAccountSTSAllowPrefixes?.split(',')?.toTypedArray()?.let {
                config["allowPrefixes"] = it
            }
            /**
             * // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
             *                 val allowActions = arrayOf( // 简单上传
             *                     "name/cos:PutObject",
             *                     "name/cos:PostObject",  // 分片上传
             *                     "name/cos:InitiateMultipartUpload",
             *                     "name/cos:ListMultipartUploads",
             *                     "name/cos:ListParts",
             *                     "name/cos:UploadPart",
             *                     "name/cos:CompleteMultipartUpload"
             *                 )
             */
            subAccountSTSAllowActions?.split(',')?.toTypedArray()?.let {
                config["allowActions"] = it
            }

            val response: Response = CosStsClient.getCredential(config)
            val cosSTS = TencentCosSTS(
                response.credentials.tmpSecretId,
                response.credentials.tmpSecretKey,
                response.credentials.sessionToken,
                duration,
                System.currentTimeMillis()
            )
            return DesignResponse(data = cosSTS)
        } catch (e: Exception) {
            throw IllegalArgumentException("no valid secret when get tencent cos sts!")
        }
    }

    override fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket?> {
        val opsForHash = redisTemplate.opsForHash<String, String>()
        val subAccountBucketName = opsForHash.get(KEY_TENCENT_COS, "BucketName")
        val subAccountRegion = opsForHash.get(KEY_TENCENT_COS, "Region")
        val subAccountFilePathPrefix = opsForHash.get(KEY_TENCENT_COS, "FilePathPrefix")
        if (subAccountBucketName.isNullOrEmpty() || subAccountRegion.isNullOrEmpty() || subAccountFilePathPrefix.isNullOrEmpty()) {
            return DesignResponse()
        }
        val tencentCosRegionBucket =
            TencentCosRegionBucket(
                subAccountRegion,
                subAccountBucketName,
                subAccountFilePathPrefix
            ).apply {
                encode()
            }
        return DesignResponse(data = tencentCosRegionBucket)
    }
}