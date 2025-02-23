package xcj.app.web.webserver.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler
import xcj.app.web.webserver.interfaces.ListenersProvider

class FileApiChannelChannelInitializer(
    private val apiPort: Int,
    private val handlerMappings: List<HandlerMapping>,
    private val listenersProvider: ListenersProvider?
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        val httpServerCodec = HttpServerCodec()
        val chunkedWriteHandler = ChunkedWriteHandler()
        val composedApiWebHandler =
            ComposedApiWebHandler(apiPort, handlerMappings, listenersProvider)
        //val idleStateHandler = IdleStateHandler(1, 1, 1, TimeUnit.DAYS)
        //ch.config().writeBufferWaterMark = WriteBufferWaterMark(32 * 1024, 64 * 1024)
        //LoggingHandler(LogLevel.TRACE)
        ch.pipeline()
            .addLast(httpServerCodec)
            .addLast(chunkedWriteHandler)
            .addLast(composedApiWebHandler)
    }
}