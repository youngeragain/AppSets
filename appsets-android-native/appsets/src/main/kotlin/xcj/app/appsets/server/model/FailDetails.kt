package xcj.app.appsets.server.model

data class FailDetails(
    var code:Int,
    var info:String,
    var e:Throwable?=null,
    var url:String?=null,
    var methodName:String?=null
)