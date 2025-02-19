package xcj.app.starter.android.ktx

import java.io.File

const val SCHEMA_FIlE = "file"
const val SCHEMA_HTTPS = "https"
const val SCHEMA_HTTP = "http"

fun String?.asFileOrNull(): File? {
    if (this.isNullOrEmpty()) {
        return null
    }
    return File(this)
}

fun String?.startWithHttpSchema(): Boolean {
    if (this.isNullOrEmpty()) {
        return false
    }
    return startsWith(SCHEMA_HTTPS) || this.startsWith(SCHEMA_HTTP)
}