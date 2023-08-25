package xcj.app.web.webserver.netty

import com.google.gson.Gson

class JavaScriptObjectNotationWrapper: JavaScriptObjectNotationWrapperInterface {
    private val gson:Gson = Gson()
    override fun <T> fromJson(jsonString:String, type:Class<T>):T{
        return gson.fromJson(jsonString, type)
    }

    override fun toJson(any: Any?): String {
        return gson.toJson(any)
    }
}