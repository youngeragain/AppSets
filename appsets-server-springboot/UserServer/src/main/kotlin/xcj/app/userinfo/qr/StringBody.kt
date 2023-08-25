package xcj.app.userinfo.qr

data class StringBody(val text:String){
    /*init {
        instanceMap.put(text, this)
    }

    companion object{
        private val instanceMap:MutableMap<String, StringBody> = mutableMapOf()
        fun get(text: String): StringBody?{
            return instanceMap.get(text)
        }
        fun remove(text: String){
            instanceMap.remove(text)
        }
        fun clear(){
            instanceMap.clear()
        }
    }*/
}