package xcj.app.appsets.im.model

import xcj.app.appsets.im.Bio

data class CommonURIJson(
    override var id: String,
    override var name: String,
    var uri: String,
    val isPlatformUri: Boolean = false
) : Bio {
    override var bioUrl: Any? = uri
}

