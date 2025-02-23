package xcj.app.web.webserver.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import xcj.app.web.webserver.interfaces.ListenersProvider

class ApiChannelChannelInitializer(
    private val apiPort: Int,
    private val handlerMappings: List<HandlerMapping>,
    private val listenersProvider: ListenersProvider?
) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val httpServerCodec = HttpServerCodec()
        val httpObjectAggregator = HttpObjectAggregator(Int.MAX_VALUE)
        val composedApiWebHandler =
            ComposedApiWebHandler(apiPort, handlerMappings, listenersProvider)
        ch.pipeline()
            .addLast(httpServerCodec)
            .addLast(httpObjectAggregator)
            .addLast(composedApiWebHandler)
    }
}