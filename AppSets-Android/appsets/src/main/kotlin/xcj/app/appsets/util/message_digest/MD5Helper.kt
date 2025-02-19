package xcj.app.appsets.util.message_digest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ByteUtil
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object MD5Helper {

    private const val TAG = "MD5Helper"

    data class TransformResult(
        val outContent: String,
        val outContentBase64: String
    )

    @JvmStatic
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun transform(input: String): TransformResult? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val messageDigest = MessageDigest.getInstance("MD5")
                val outBytes = messageDigest.digest(input.toByteArray())
                val outContent = ByteUtil.toHexString(outBytes)
                val outContentBase64 = Base64.encode(outBytes)
                return@withContext TransformResult(outContent, outContentBase64)
            }.onFailure {
                PurpleLogger.current.e(TAG, "encrypt failed!, ${it.message}")
            }
            PurpleLogger.current.d(TAG, "encrypt failed!")
            return@withContext null
        }
    }
}