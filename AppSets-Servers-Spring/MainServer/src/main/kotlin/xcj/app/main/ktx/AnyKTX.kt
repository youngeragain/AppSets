package xcj.app.main.ktx

fun Any.jsonStr(gson: com.google.gson.Gson): String {
    return gson.toJson(this)
}