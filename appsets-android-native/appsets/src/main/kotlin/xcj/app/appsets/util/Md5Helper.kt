package xcj.app.appsets.util

import okhttp3.internal.toHexString
import java.security.MessageDigest

object Md5Helper{
    fun encode(input:String):String{
        val messageDigest = MessageDigest.getInstance("MD5")
        val digest = messageDigest.digest(input.toByteArray())
        val sb = StringBuilder()
        for(b in digest){
            var s = b.toInt().toHexString()
            if(b<0x10)
                s = "0$s"
            sb.append(s.substring(s.length-2))
        }
        return sb.toString()
    }
}