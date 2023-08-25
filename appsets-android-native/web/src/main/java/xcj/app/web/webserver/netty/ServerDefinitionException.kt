package xcj.app.web.webserver.netty

import io.netty.handler.codec.http.FullHttpResponse


abstract class ServerDefinitionException(message:String):Exception(message) {

    open fun getResponse():FullHttpResponse?{
        return null
    }
    open fun canAsResponse():Boolean{
        return false
    }
}