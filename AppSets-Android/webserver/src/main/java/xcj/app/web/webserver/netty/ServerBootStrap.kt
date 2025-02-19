package xcj.app.web.webserver.netty

import android.app.Application
import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.stream.ChunkedWriteHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.DefaultControllerCollector

class ServerBootStrap(private val port: Int) {

    interface ActionLister {
        fun onSuccess()

        fun onFailure(reason: String? = null)
    }

    companion object {
        private const val TAG = "ServerBootStrap"
    }

    private var mainEventGroup: EventLoopGroup? = null
    private var workerEventGroup: EventLoopGroup? = null
    private var serverChannel: Channel? = null

    suspend fun close(actionLister: ActionLister?) {
        withContext(Dispatchers.IO) {
            runCatching {
                serverChannel?.close() // 关闭 ServerSocketChannel
                workerEventGroup?.shutdownGracefully()
                mainEventGroup?.shutdownGracefully()
            }.onSuccess {
                actionLister?.onSuccess()
                workerEventGroup = null
                mainEventGroup = null
                serverChannel = null
                PurpleLogger.current.d(TAG, "close success")
            }.onFailure {
                PurpleLogger.current.d(TAG, "close failure, ${it.message}")
                actionLister?.onFailure(it.message)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun main(application: Application, actionLister: ActionLister?) {
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "main")
            val handler: Handler? = null
            val handlerMethodList = DefaultControllerCollector.collect(
                application,
                application::class.java.`package`?.name?.removeSuffix(".container"),
                handler
            )
            PurpleLogger.current.d(TAG, "main, handlerMethodList: $handlerMethodList")
            if (handlerMethodList.isNullOrEmpty()) {
                actionLister?.onFailure()
                return@withContext
            }
            PurpleLogger.current.d(TAG, "main, create serverBootstrap pre step 0")
            val fixedUriHandlerMethod = handlerMethodList.filter { it.uriSplitResults == null }
            PurpleLogger.current.d(
                TAG,
                "main, create serverBootstrap pre step 1, fixedUriHandlerMethod$fixedUriHandlerMethod"
            )
            val dynamicUriHandlerMethod =
                handlerMethodList.filter { it.uriSplitResults != null }
            PurpleLogger.current.d(
                TAG,
                "main, create serverBootstrap pre step 2, dynamicUriHandlerMethod:$dynamicUriHandlerMethod"
            )
            val fixedUriHandlerMethodMap = fixedUriHandlerMethod.associateBy { it.uri }
            val dynamicUriHandlerMethodMap = dynamicUriHandlerMethod.associateBy { it.uri }
            val requestPathHandlerMapping =
                RequestPathHandlerMapping(fixedUriHandlerMethodMap, dynamicUriHandlerMethodMap)
            PurpleLogger.current.d(TAG, "main, create serverBootstrap pre step 3")
            val handlerMappings = listOf(requestPathHandlerMapping)

            val mainLoopGroup = NioEventLoopGroup()
            val workerLoopGroup = NioEventLoopGroup()
            mainEventGroup = mainLoopGroup
            workerEventGroup = workerLoopGroup
            val serverBootstrap = ServerBootstrap()
            PurpleLogger.current.d(TAG, "main, create serverBootstrap")
            serverBootstrap
                .group(mainLoopGroup, workerLoopGroup)
                .channel(NioServerSocketChannel::class.java)
                .localAddress(port)
                .handler(LoggingHandler(LogLevel.TRACE))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val webHandler = WebHandler(port, handlerMappings)
                        val httpContentHandler = HTTPContentHandler()
                        val httpServerCodec = HttpServerCodec()
                        val httpObjectAggregator = HttpObjectAggregator(Int.MAX_VALUE)
                        val chunkedWriteHandler = ChunkedWriteHandler()
                        ch.pipeline()
                            .addLast(httpServerCodec)
                            .addLast(httpContentHandler)
                            .addLast(chunkedWriteHandler)
                            .addLast(httpObjectAggregator)
                            .addLast(webHandler)
                    }
                })
            PurpleLogger.current.d(TAG, "main, serverBootstrap do bind().sync().channel()")
            runCatching {
                val channel = serverBootstrap.bind().sync().channel()
                serverChannel = channel
                PurpleLogger.current.d(TAG, "main, start success")
                actionLister?.onSuccess()
                channel.closeFuture().sync()
            }.onFailure {
                it.printStackTrace()
                actionLister?.onFailure(it.message)
                PurpleLogger.current.d(TAG, "main, start failure, ${it.message}")
                close(null)
            }
        }
    }
}


