package xcj.app.appsets.util.encrypt

object EncryptionUtil {

    @JvmStatic
    suspend fun encryptWithAES(input: String): AESHelper.EncryptionResult? {
        return AESHelper.encrypt(input)
    }

    @JvmStatic
    suspend fun decryptForAES(input: AESHelper.DecryptionInput): AESHelper.DecryptionResult? {
        return AESHelper.decrypt(input)
    }

    @JvmStatic
    suspend fun encryptWithDES(input: String): AESHelper.EncryptionResult? {
        return AESHelper.encrypt(
            input,
            algorithm = AESHelper.ALGORITHM_DES,
            AESHelper.TRANSFORM_DES_DEFAULT
        )
    }

    @JvmStatic
    suspend fun decryptForDES(input: AESHelper.DecryptionInput): AESHelper.DecryptionResult? {
        return AESHelper.decrypt(
            input,
            algorithm = AESHelper.ALGORITHM_DES,
            AESHelper.TRANSFORM_DES_DEFAULT
        )
    }

    @JvmStatic
    suspend fun encryptWithRSA(input: String): RSAHelper.EncryptionResult? {
        return RSAHelper.encrypt(input)
    }

    @JvmStatic
    suspend fun decryptForRSA(input: RSAHelper.DecryptionInput): RSAHelper.DecryptionResult? {
        return RSAHelper.decrypt(input)
    }

}