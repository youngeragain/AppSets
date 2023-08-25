package xcj.app.appsets.ktx

import com.google.gson.Gson

fun Any?.isHttpUrl():Boolean{
    if(this is Number)
        return false
    return if(this is String){
        this.startsWith("http://")||this.startsWith("https://")
    }else
        false
}

fun Any.json():String{
    return Gson().toJson(this)
}