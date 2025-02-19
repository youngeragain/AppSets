package xcj.app.appsets.im.model

import xcj.app.appsets.im.Bio

data class CommonURLJson(
    override var id: String,
    override var name: String,
    var url: String,
) : Bio {
    override var bioUrl: Any? = url
}

