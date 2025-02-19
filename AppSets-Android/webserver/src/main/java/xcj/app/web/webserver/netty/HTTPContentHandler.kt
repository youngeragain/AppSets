package xcj.app.web.webserver.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpMessage
import xcj.app.starter.android.util.PurpleLogger

class HTTPContentHandler() : SimpleChannelInboundHandler<HttpMessage>() {
    companion object {
        private const val TAG = "HTTPContentHandler"
    }

    //HttpMessage
    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpMessage?) {
        PurpleLogger.current.d(TAG, "channelRead0, msg:${msg}")
        ctx.fireChannelRead(msg)
    }
}