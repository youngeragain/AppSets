package xcj.app.web.webserver.netty

import android.util.Log
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import xcj.app.web.webserver.Dog

class WebHandler(
    private val handlerMappingCache:List<HandlerMapping>,
): SimpleChannelInboundHandler<FullHttpRequest>(){

    private var defaultFullHttpRequest:DefaultFullHttpRequest? = null

    private var defaultHttpRequest:DefaultHttpRequest? = null

    private fun requestUriNotFount(ctx: ChannelHandlerContext, why:String?) {
        Log.e("WebHandler", "why:${why}")
        ctx.channel().write(notFoundUriResponse)
    }


    private fun getHandlerMapping(ctx: ChannelHandlerContext, fullHttpRequest: FullHttpRequest): HandlerMapping? {
        if(fullHttpRequest.uri().isNullOrEmpty())
            return null
        for(handlerMapping in handlerMappingCache){
            if(handlerMapping.isSupport(fullHttpRequest))
                return handlerMapping
        }
        return null
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        super.exceptionCaught(ctx, cause)
    }

    override fun channelRegistered(ctx: ChannelHandlerContext?) {
        super.channelRegistered(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        super.channelUnregistered(ctx)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        super.channelReadComplete(ctx)
        ctx?.writeAndFlush(Unpooled.EMPTY_BUFFER)
            ?.addListener(ChannelFutureListener.CLOSE)
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        super.userEventTriggered(ctx, evt)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
        super.channelWritabilityChanged(ctx)
    }

  /*  override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if(msg is DefaultHttpRequest)
            defaultHttpRequest = msg
        if(msg is DefaultLastHttpContent){
            if(defaultHttpRequest!=null){
                defaultFullHttpRequest = DefaultFullHttpRequest(
                    defaultHttpRequest!!.protocolVersion(),
                    defaultHttpRequest!!.method(),
                    defaultHttpRequest!!.uri(),
                    msg.content(),
                    defaultHttpRequest!!.headers(),
                    msg.trailingHeaders()
                )
            }
            defaultFullHttpRequest?.let {
                parse(ctx, it)
            }
        }
    }*/

    private fun parse(ctx: ChannelHandlerContext, fullHttpRequest: FullHttpRequest) {
        if(fullHttpRequest.toString()=="EmptyLastHttpContent")
            return
        val uri = fullHttpRequest.uri()
        if(uri == "/favicon.ico")
            return
        val queryStringDecoder = QueryStringDecoder(uri)
        queryDecoderLocal.set(queryStringDecoder)
        val handlerMapping = getHandlerMapping(ctx, fullHttpRequest)
        if(handlerMapping==null){
            requestUriNotFount(ctx, "handlerMapping not found!")
            return
        }
        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(byteArrayOf())
        )

        val handle = handlerMapping.handle(fullHttpRequest, response)

        try {
            if(handle== HandleResult.EMPTY){
                Log.e("blue", "handlerMethod hand result is empty!")
            }else{
                val handleResult = handle.getResult()
                ctx.channel().write(handleResult)
            }
        }catch (e:Exception){
            Dog.e("exception:${e}")
            ctx.channel().write(serverInternalErrorResponse)
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {
        parse(ctx!!, msg!!)
    }

    companion object{
        private val serverInternalErrorResponse = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.INTERNAL_SERVER_ERROR,
            Unpooled.wrappedBuffer("server internal error".toByteArray())
        ).apply {
            headers().set("Content-Type", "application/text")
        }
        private val notFoundUriResponse = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer("not found".toByteArray())
        ).apply {
            headers().set("Content-Type", "application/text")
        }
        val queryDecoderLocal:ThreadLocal<QueryStringDecoder?> = ThreadLocal()
    }
}