package xcj.app.main.qr

import org.springframework.stereotype.Component

@Component
class MyCipherComponent {
    companion object {
        private const val base64WrappedSecretKey = "zTXu5jfOEbUWLtlI53X3xrLjBfL70dVXQjYuNlwEQX0="
        private const val base64WrappedIv = "NBBfyzxe4qiWDvZvSAEtmw=="
    }

    private val sCipherWrapper: ThreadLocal<CipherWrapper?> = ThreadLocal()

    fun getCipherWrapper(): CipherWrapper {
        if (sCipherWrapper.get() == null) {
            val cipherWrapper = CipherWrapper(base64WrappedSecretKey, base64WrappedIv)
            cipherWrapper.init()
            sCipherWrapper.set(cipherWrapper)
            return cipherWrapper
        }
        return sCipherWrapper.get()!!
    }

    fun encode(input: String): String {
        return getCipherWrapper().encode(input)
    }

    fun decode(input: String): String {
        return getCipherWrapper().decode(input)
    }
}