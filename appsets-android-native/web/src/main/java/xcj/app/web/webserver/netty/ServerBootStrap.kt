package xcj.app.web.webserver.netty

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import xcj.app.web.webserver.DefaultController
import kotlin.concurrent.thread

class ServerBootStrap(private val port:Int) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun boot(context: Context, handler:Handler?=null) {
        thread {
            val mainLoopGroup = NioEventLoopGroup()
            val workerLoopGroup = NioEventLoopGroup()
            kotlin.runCatching {
                val handlerMethodList = DefaultController.collect(context, (context.applicationContext as Application)::class.java.`package`?.name, handler)
                Log.e("blue", "handlerMethodList:${handlerMethodList}")
                val fixedUriHandlerMethod = handlerMethodList?.filter { it.uriSplitResults == null }
                val dynamicUriHandlerMethod = handlerMethodList?.filter { it.uriSplitResults != null }
                val requestPathHandlerMapping = RequestPathHandlerMapping(fixedUriHandlerMethod, dynamicUriHandlerMethod)
                val handlerMappings = listOf(requestPathHandlerMapping)
                val serverBootstrap = ServerBootstrap()

                serverBootstrap
                    .group(mainLoopGroup, workerLoopGroup)
                    .channel(NioServerSocketChannel::class.java)
                    .localAddress(port)
                    .childHandler(object : ChannelInitializer<SocketChannel>(){
                        override fun initChannel(ch: SocketChannel) {
                            val webHandler = WebHandler(handlerMappings)
                            val httpRequestDecoder = HttpRequestDecoder()
                            val httpResponseEncoder = HttpResponseEncoder()
                            val httpObjectAggregator = HttpObjectAggregator(1024 * 1024)
                            ch.pipeline()
                                .addLast(httpRequestDecoder)
                                .addLast(httpResponseEncoder)
                                .addLast(httpObjectAggregator)
                                .addLast(webHandler)

                        }
                    })
                serverBootstrap.bind().sync().channel().closeFuture().sync()
            }.onSuccess {
                Log.e("blue", "doNetty success")
            }.onFailure {
                Log.e("blue", "doNetty failure")
            }
        }
    }
}