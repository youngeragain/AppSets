package xcj.app.appsets.util

object LocalDataProvider {

    fun <D : Any> save(key: String, any: D?): Boolean {
        return false
    }

    fun <D : Any> get(key: String): D? {
        return null
    }

}