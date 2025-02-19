package xcj.app.main.service.external

import jakarta.xml.bind.DatatypeConverter
import xcj.app.util.PurpleLogger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.Throws

data class Statement(
    val effect: String = "allow",
    val action: List<String> = listOf("name/cos:PutObject", "name/cos:GetObject"),
    val resource: List<String> = listOf("qcs::cos:ap-chengdu:uid/1258462798:appsets-2022-1258462798/pics/*")
)

data class PayloadTCOSPolicy(val version: String = "2.0", val statement: List<Statement>)

data class PayloadTCOS(val Name: String, val Policy: PayloadTCOSPolicy, val DurationSeconds: Long? = null)

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

class TencentCOSTest {
    companion object {
        private const val TAG = "TencentCOSTest"
    }

    @Throws(Exception::class)
    private fun hmac256(key: ByteArray?, msg: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key, mac.algorithm)
        mac.reset()
        mac.init(secretKeySpec)
        return mac.doFinal(msg.toByteArray(StandardCharsets.UTF_8))
    }

    @Throws(Exception::class)
    private fun sha256Hex(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val d = md.digest(s.toByteArray(StandardCharsets.UTF_8))
        return DatatypeConverter.printHexBinary(d).lowercase(Locale.getDefault())
    }

    private fun toHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (item in bytes) {
            sb.append(Integer.toHexString((item.toInt() and 0xFF) or 0x100).substring(1, 3))
        }
        return sb.toString().lowercase(Locale.getDefault())
    }

    @Throws(Exception::class)
    fun getTemporaryStsAuthStr(secretId: String, secretKey: String, payload: String): Pair<String, Long> {
        val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val calendar: Calendar = Calendar.getInstance()
        val currentSeconds = calendar.time.time / 1000

        val currentDate = simpleDateFormat.format(calendar.time)
        PurpleLogger.current.d(
            TAG,
            "getTemporaryStsAuthStr, current seconds:${currentSeconds} currentDate:$currentDate"
        )
        val service = "sts"
        val host = "sts.tencentcloudapi.com"
        val algorithm = "TC3-HMAC-SHA256"
        val timestamp = currentSeconds

        // ************* 步骤 1：拼接规范请求串 *************
        val httpRequestMethod = "POST"
        val canonicalUri = "/"
        val canonicalQueryString = ""
        val canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:$host\n"
        val signedHeaders = "content-type;host"
        val hashedRequestPayload = sha256Hex(payload)
        val canonicalRequest =
            "$httpRequestMethod\n$canonicalUri\n$canonicalQueryString\n$canonicalHeaders\n$signedHeaders\n$hashedRequestPayload"
        PurpleLogger.current.d(
            TAG, """
        getTemporaryStsAuthStr,
        payload:
        $payload
        payloadHex:
        $hashedRequestPayload
    """.trimIndent()
        )

        // ************* 步骤 2：拼接待签名字符串 *************
        val credentialScope = "$currentDate/$service/tc3_request"
        val hashedCanonicalRequest = sha256Hex(canonicalRequest)

        PurpleLogger.current.d(
            TAG, """
        getTemporaryStsAuthStr,
        canonicalRequest:
        $canonicalRequest
        hashedCanonicalRequestHex:
        $hashedCanonicalRequest
    """.trimIndent()
        )
        val stringToSign = "$algorithm\n$timestamp\n$credentialScope\n$hashedCanonicalRequest"
        // ************* 步骤 3：计算签名 *************
        val secretDate = hmac256(("TC3$secretKey").toByteArray(StandardCharsets.UTF_8), currentDate)
        val secretService = hmac256(secretDate, service)
        val secretSigning = hmac256(secretService, "tc3_request")
        val bytes = hmac256(secretSigning, stringToSign)
        val signature = DatatypeConverter.printHexBinary(bytes).lowercase(Locale.getDefault())
        PurpleLogger.current.d(TAG, "getTemporaryStsAuthStr, signatureHex:\n$signature")
        val authorization =
            "$algorithm Credential=$secretId/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"
        PurpleLogger.current.d(TAG, "getTemporaryStsAuthStr$authorization")
        return authorization to currentSeconds

    }
}