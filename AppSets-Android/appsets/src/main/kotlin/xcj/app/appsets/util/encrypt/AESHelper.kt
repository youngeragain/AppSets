package xcj.app.appsets.util.encrypt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger
import java.io.File
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object AESHelper {

    private const val TAG = "AESHelper"
    const val ALGORITHM_AES = "AES"
    const val ALGORITHM_DES = "DES"
    const val TRANSFORM_AES_DEFAULT = "AES/ECB/PKCS5Padding"
    const val TRANSFORM_DES_DEFAULT = "DES/ECB/PKCS5Padding"

    data class EncryptionResult(
        val password: String,
        val privateKeyFile: File,
        val outContent: String,
        val outContentBase64: String,
        val encryptTransformation: String,
    )

    data class DecryptionResult(
        val outContent: String,
        val outContentBase64: String,
    )

    data class DecryptionInput(
        val keyFile: File,
        val inputContent: String,
        val base64Encoded: Boolean,
    )

    @OptIn(ExperimentalEncodingApi::class)
    @JvmStatic
    suspend fun encrypt(
        input: String,
        algorithm: String = ALGORITHM_AES,
        transformation: String = TRANSFORM_AES_DEFAULT,
    ): EncryptionResult? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val uuid = UUID.randomUUID().toString().uppercase()
                val password = if (algorithm == ALGORITHM_AES) {
                    uuid.substring(0, 16)
                } else {
                    uuid.substring(0, 8)
                }
                val privateKey = SecretKeySpec(password.encodeToByteArray(), algorithm)
                val cipher = Cipher.getInstance(transformation)
                cipher.init(Cipher.ENCRYPT_MODE, privateKey)
                val inputBytes = input.toByteArray()
                val outBytes = cipher.doFinal(inputBytes)
                val outContent = String(outBytes)
                val outContentBase64 = Base64.encode(outBytes)
                val privateKeyFile =
                    File.createTempFile("private_key__${UUID.randomUUID()}", ".key")
                privateKeyFile.writeBytes(Base64.encodeToByteArray(privateKey.encoded))
                return@withContext EncryptionResult(
                    password,
                    privateKeyFile,
                    outContent,
                    outContentBase64,
                    encryptTransformation = transformation
                )
            }.onFailure {
                PurpleLogger.current.e(TAG, "encrypt failed!, ${it.message}")
            }
            PurpleLogger.current.d(TAG, "encrypt failed!")
            return@withContext null
        }
    }

    @JvmStatic
    suspend fun decrypt(
        input: DecryptionInput,
        algorithm: String = ALGORITHM_AES,
        transformation: String = TRANSFORM_AES_DEFAULT,
    ): DecryptionResult? {
        return withContext(Dispatchers.Default) {
            val cipher = Cipher.getInstance(transformation)
            tryDecrypt(input, algorithm, cipher)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun tryDecrypt(
        input: DecryptionInput,
        algorithm: String,
        cipher: Cipher,
    ): DecryptionResult? {
        runCatching {
            val keyBytes = Base64.decode(input.keyFile.readBytes())
            val privateKey = SecretKeySpec(keyBytes, algorithm)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val inputBytes = if (input.base64Encoded) {
                Base64.decode(input.inputContent)
            } else {
                input.inputContent.encodeToByteArray()
            }
            val outBytes = cipher.doFinal(inputBytes)
            val outContent = outBytes.decodeToString()
            val outContentBase64 = Base64.encode(outBytes)
            return DecryptionResult(
                outContent, outContentBase64
            )
        }.onFailure {
            PurpleLogger.current.e(TAG, "tryDecrypt failed!, ${it.message}")
        }
        PurpleLogger.current.d(TAG, "tryDecrypt failed!")
        return null
    }
}