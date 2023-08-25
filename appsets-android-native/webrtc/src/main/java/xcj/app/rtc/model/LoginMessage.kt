package xcj.app.rtc.model

data class LoginMessage(val room:String, val account:String, val name:String?, val password:String? = null)

