package xcj.app.rtc

import com.google.gson.Gson

object DependencyProvider {
    val gson: Gson by lazy {
        Gson()
    }
}