package xcj.app

object CoreLogger {
    fun d(tag:String="CoreLogger", message:String?, throwable: Throwable? = null){
        println("$tag: ${message?:"[Empty message by CoreLogger]"}")
    }
}