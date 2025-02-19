package xcj.app.appsets.util.message_digest

object MessageDigestUtil {

    @JvmStatic
    suspend fun transformWithMD5(input: String): MD5Helper.TransformResult? {
        return MD5Helper.transform(input)
    }

    @JvmStatic
    suspend fun transformWithSHA256(input: String): SHA256Helper.TransformResult? {
        return SHA256Helper.transform(input)
    }

}