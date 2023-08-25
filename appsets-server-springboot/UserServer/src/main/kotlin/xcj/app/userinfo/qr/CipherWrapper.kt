package xcj.app.userinfo.qr

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CipherWrapper(private val base64WrappedSecretKey:String, val base64WrappedIv:String){
	private lateinit var cipher: Cipher
	private lateinit var base64Decoder: Base64.Decoder
	private lateinit var base64Encoder:Base64.Encoder
	private lateinit var secretKeySpec: SecretKeySpec
	private lateinit var ivParameterSpec: IvParameterSpec
	fun init(){
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
		base64Decoder = Base64.getDecoder()
		base64Encoder = Base64.getEncoder()
		secretKeySpec = SecretKeySpec(base64Decoder.decode(base64WrappedSecretKey), "AES")
		ivParameterSpec = IvParameterSpec(base64Decoder.decode(base64WrappedIv))
	}
	fun encode(input: String):String{
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
		val bytes = cipher.doFinal(input.toByteArray())
		return base64Encoder.encodeToString(bytes)
	}
	fun decode(input:String):String{
		val inputBytes = base64Decoder.decode(input)
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
		val bytes = cipher.doFinal(inputBytes)
		return String(bytes)
	}
}