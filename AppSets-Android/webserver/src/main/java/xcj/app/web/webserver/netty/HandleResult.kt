package xcj.app.web.webserver.netty

import io.netty.handler.codec.http.HttpResponse

interface HandleResult {

    fun getContentType(): String?

    fun getResult(): HttpResponse?

    companion object {
        val EMPTY = object : HandleResult {
            override fun getContentType(): String? = null

            override fun getResult(): HttpResponse? = null
        }
    }
}