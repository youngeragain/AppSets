package xcj.app.appsets.util.encrypt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger
import java.io.File
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.util.UUID
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object RSAHelper {

    private const val TAG = "RSAHelper"

    data class EncryptionResult(
        val privateKeyFile: File,
        val publicKeyFile: File,
        val outContent: String,
        val outContentBase64: String,
        val encryptBy: String,
        val encryptTransformation:String,
    )

    data class DecryptionResult(
        val outContent: String,
        val outContentBase64: String
    )

    data class DecryptionInput(
        val keyFile: File,
        val inputContent: String,
        val base64Encoded: Boolean
    )

    @OptIn(ExperimentalEncodingApi::class)
    @JvmStatic
    suspend fun encrypt(
        input: String,
        transformation: String = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"
    ): EncryptionResult? {
        return withContext(Dispatchers.Default) {


            runCatching {
                val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
                keyPairGenerator.initialize(2048)
                val keyPair = keyPairGenerator.genKeyPair()
                val cipher = Cipher.getInstance(transformation)
                cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
                val inputBytes = input.toByteArray()
                val outBytes = cipher.doFinal(inputBytes)
                val outContent = String(outBytes)
                val outContentBase64 = Base64.encode(outBytes)

                val privateKeyFile = File.createTempFile("private_key_${UUID.randomUUID()}", ".key")
                privateKeyFile.writeBytes(Base64.encodeToByteArray(keyPair.private.encoded))

                val publicKeyFile = File.createTempFile("public_key_${UUID.randomUUID()}", ".key")
                publicKeyFile.writeBytes(Base64.encodeToByteArray(keyPair.public.encoded))

               return@withContext EncryptionResult(
                    privateKeyFile,
                    publicKeyFile,
                    outContent,
                    outContentBase64,
                    encryptBy = "public_key",
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
        transformation: String = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"
    ): DecryptionResult? {
        return withContext(Dispatchers.Default) {
            val cipher = Cipher.getInstance(transformation)

            val keyFactory = KeyFactory.getInstance("RSA")
            return@withContext tryDecrypt(cipher, keyFactory, input)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @JvmStatic
    private fun tryDecrypt(
        cipher: Cipher,
        keyFactory: KeyFactory,
        input: DecryptionInput
    ): DecryptionResult? {
        runCatching {
            val keyBytes = Base64.decode(input.keyFile.readBytes())
            val inputBytes = if (input.base64Encoded) {
                Base64.decode(input.inputContent)
            } else {
                input.inputContent.encodeToByteArray()
            }
            val pkcS8EncodedKeySpec = PKCS8EncodedKeySpec(keyBytes)
            val privateKey = keyFactory.generatePrivate(pkcS8EncodedKeySpec)

            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val outBytes = cipher.doFinal(inputBytes)
            val outContent = outBytes.decodeToString()
            val outContentBase64 = Base64.encode(outBytes)
            return DecryptionResult(outContent, outContentBase64)
        }.onFailure {
            PurpleLogger.current.e(TAG, "tryDecrypt failed!, ${it.message}")
        }
        PurpleLogger.current.d(TAG, "tryDecrypt failed!")
        return null
    }
}