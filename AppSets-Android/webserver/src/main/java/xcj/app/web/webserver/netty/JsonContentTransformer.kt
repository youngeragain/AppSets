package xcj.app.web.webserver.netty

import com.google.gson.Gson

class JsonContentTransformer : ContentTransformer {
    private val gson: Gson = Gson()
    override fun <T> fromString(jsonString: String, type: Class<T>): T {
        return gson.fromJson(jsonString, type)
    }

    override fun toString(any: Any?): String {
        return gson.toJson(any)
    }
}