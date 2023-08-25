package xcj.app.web.webserver

import android.util.Log

object Dog {
    fun e(msg:String, throwable: Throwable? = null){
        Log.e("blue", msg, throwable)
    }
}