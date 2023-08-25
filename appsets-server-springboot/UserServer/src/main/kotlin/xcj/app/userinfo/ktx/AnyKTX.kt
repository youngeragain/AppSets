package xcj.app.userinfo.ktx

fun Any.jsonStr(gson: com.google.gson.Gson):String{
    return gson.toJson(this)
}