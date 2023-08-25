package xcj.app.web.webserver.netty

interface JavaScriptObjectNotationWrapperInterface {
    fun <T> fromJson(jsonString:String, type:Class<T>):T
    fun toJson(any: Any?):String
}