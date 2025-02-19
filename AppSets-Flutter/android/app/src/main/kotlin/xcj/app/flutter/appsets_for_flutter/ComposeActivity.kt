package xcj.app.flutter.appsets_for_flutter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.StringBuilder


fun String.times(what:Int):String {
    if(what==1)
        return this
    val result = StringBuilder()
    repeat(what){
        result.append(this)
    }
    return result.toString()
}

fun String.MD5Encryption():String{
    return this
}

fun String.Base64Encryption():String{
    return this
}
fun String.Base64Decryption():String{
    return this
}

fun String.AESEncryption():String{
    return this
}
fun String.AESDecryption():String{
    return this
}
fun String.RSAEncryption():String{
    return this
}
fun String.RSADecryption():String{
    return this
}

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeMain()
        }
        doEncryption()
    }


    private fun doEncryption() {
        val rsaKeyGenerator = KeyGenerator.getInstance("AES")
        rsaKeyGenerator.init(256)
        val secretKey = rsaKeyGenerator.generateKey()
        val iv:ByteArray = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        val base64WrappedSecretKey = "zTXu5jfOEbUWLtlI53X3xrLjBfL70dVXQjYuNlwEQX0="
        val base64WrappedIv = "NBBfyzxe4qiWDvZvSAEtmw=="
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        fun en_decrypt(input: ByteArray, key:SecretKey, iv:ByteArray, base64WrappedSecretKey:String, base64WrappedIv:String, cipher: Cipher, mode:Int=Cipher.ENCRYPT_MODE):ByteArray{
           /* val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")*/
            val secretKeySpec = SecretKeySpec(Base64.decode(base64WrappedSecretKey, Base64.DEFAULT), "AES")
            val ivParameterSpec = IvParameterSpec(Base64.decode(base64WrappedIv, Base64.DEFAULT))
            cipher.init(mode, secretKeySpec, ivParameterSpec)
            val bytes = cipher.doFinal(input)
            return bytes
        }

        val rawText = "are you ok?".times(100)
        Log.e("blue", "raw text:$rawText")
        val encryptByteArray = en_decrypt(rawText.toByteArray(), secretKey, iv, base64WrappedSecretKey, base64WrappedIv, cipher)
        val encodeBase64 = Base64.encodeToString(encryptByteArray, Base64.DEFAULT)
        val charArrayString = encryptByteArray.map { it.toInt().toChar() }.joinToString("")
        Log.e("blue", "encode base64 text:${encodeBase64}")
        val decodeByteArray = Base64.decode(encodeBase64, Base64.DEFAULT)
        val encryptByteArray1 = charArrayString.toCharArray().map { it.code.toByte() }
         val decryptByteArray = en_decrypt(decodeByteArray, secretKey, iv, base64WrappedSecretKey, base64WrappedIv, cipher, Cipher.DECRYPT_MODE)
         Log.e("blue", "encrypt text:${String(decryptByteArray)}")
    }
}

@Composable
fun ComposeMain(){
    val activity = LocalContext.current as AppCompatActivity
    Column() {
        val size = remember {
            mutableStateOf(Size(100f, 100f))
        }
        Text(text = "hello compose", Modifier.clickable {
            val random = (0..100).random().toFloat()
            val random1 = (0..100).random().toFloat()
            size.value = Size(random, random1)
        }, lineHeight = TextUnit.Unspecified)
        val testSize = animateSizeAsState(targetValue = size.value)
        Canvas(modifier = Modifier
            .background(Color.Gray)
            .size(testSize.value.width.dp, testSize.value.height.dp), onDraw = {
            drawRect(Color.Red, Offset.Zero, this.size)
        })

    }
}