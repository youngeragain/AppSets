package xcj.app.userinfo

import org.bouncycastle.util.encoders.HexEncoder
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Helpers {
    const val API_DESIGN_TABLE_DEFAULT_ID = -1

    private fun commonGen():String{
        val intRange = 0..9999999
        val random0 = intRange.random()
        val random1 = intRange.random()
        val part1 = getPart(random0)
        val part2 = getPart(random1)
        val currentYearStr = getCurrentYearStr()
        val accountLast = "$part1$part2"
        return "$currentYearStr$accountLast"
    }
    private fun getCurrentYearStr():String{
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val monthTemp = if(month in 0..9){
            "0$month"
        }else{
            "$month"
        }
        val dayOfMonthTemp = if(dayOfMonth in 0..9){
            "0$dayOfMonth"
        }else{
            "$dayOfMonth"
        }
        return "$year$monthTemp$dayOfMonthTemp"
    }
    private fun getPart(input:Int):String{
        return when(input){
            in 0 until 10->"000000$input"
            in 10 until 100->"00000$input"
            in 100 until 1000->"0000$input"
            in 1000 until 10000->"000$input"
            in 10000 until 100000->"00$input"
            in 100000 until 1000000->"0$input"
            in 1000000 until 10000000->"$input"
            else->"0000000"
        }
    }
    fun generateUserId(): String {
        val commonGen = commonGen()
        return "U$commonGen"
    }

    fun generateGroupId():String{
        val commonGen = commonGen()
        return "G$commonGen"
    }
    fun generateAppId(appName:String?, packageName:String?):String{
        val commonGen = commonGen()
        return "APP$commonGen"
    }

    fun generateAppSetsAppId():String{
        val commonGen = commonGen()
        return "APPSETS$commonGen"
    }

    fun generateReviewId(): String {
        val commonGen = commonGen()
        return "R$commonGen"
    }

    fun generateScreenId():String{
        val commonGen = commonGen()
        return "S$commonGen"
    }
    fun generateId(prefix:String):String{
        val commonGen = commonGen()
        return "$prefix$commonGen"
    }

    fun generateRequestId(): String {
        val requestIdRaw = generateId("RequestId-")
        val requestId = sha256Hex(requestIdRaw)
        val requestIdMaced = hmac256("request_sk", requestId)
        val byteArrayOutputStream = ByteArrayOutputStream()
        HexEncoder().encode(requestIdMaced, 0, requestIdMaced.size, byteArrayOutputStream)
        return String(byteArrayOutputStream.toByteArray())
    }
    fun sha256Hex(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val d = md.digest(s.toByteArray())
        val byteArrayOutputStream = ByteArrayOutputStream()
        HexEncoder().encode(d, 0, d.size, byteArrayOutputStream)
        return byteArrayOutputStream.toString(Charset.defaultCharset()).lowercase(Locale.getDefault())
    }

/*    fun md5Hex(s: String): String {
        val md = MessageDigest.getInstance("md5")
        val d = md.digest(s.toByteArray())
        val byteArrayOutputStream = ByteArrayOutputStream()
        HexEncoder().encode(d, 0, d.size, byteArrayOutputStream)
        return byteArrayOutputStream.toString(Charset.defaultCharset()).lowercase(Locale.getDefault())
    }*/

    fun hmac256(key: ByteArray, msg: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key, mac.algorithm)
        mac.init(secretKeySpec)
        return mac.doFinal(msg.toByteArray())
    }
    fun hmac256(key: ByteArray, msg: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key, mac.algorithm)
        mac.init(secretKeySpec)
        return mac.doFinal(msg)
    }
    fun hmac256(key: String, msg: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), mac.algorithm)
        mac.init(secretKeySpec)
        return mac.doFinal(msg.toByteArray())
    }
    fun hmac256(key: String, msg: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), mac.algorithm)
        mac.init(secretKeySpec)
        return mac.doFinal(msg)
    }


}