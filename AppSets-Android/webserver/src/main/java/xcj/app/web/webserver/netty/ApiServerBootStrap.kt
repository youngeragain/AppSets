package xcj.app.web.webserver.netty

import android.os.Build
import androidx.annotation.RequiresApi
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import xcj.app.starter.android.util.PurpleLogger

class ApiServerBootStrap(
    private val port: Int,
    private val channelInitializer: ChannelInitializer<*>,
) {
    companion object {
        private const val TAG = "ApiServerBootStrap"
    }

    private var mainEventGroup: EventLoopGroup? = null
    private var workerEventGroup: EventLoopGroup? = null

    fun close() {
        workerEventGroup?.shutdownGracefully()
        mainEventGroup?.shutdownGracefully()
        workerEventGroup = null
        mainEventGroup = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun main() {
        PurpleLogger.current.d(TAG, "main, create serverBootstrap for port:$port")
        val mainLoopGroup = NioEventLoopGroup(1)
        val workerLoopGroup = NioEventLoopGroup()

        mainEventGroup = mainLoopGroup
        workerEventGroup = workerLoopGroup

        val serverBootstrap = ServerBootstrap()
        serverBootstrap
            .group(mainLoopGroup, workerLoopGroup)
            .channel(NioServerSocketChannel::class.java)
            .localAddress(port)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(channelInitializer)


        PurpleLogger.current.d(
            TAG,
            "main, serverBootstrap do bind().sync().channel() for port:$port"
        )
        serverBootstrap.bind().sync().channel().closeFuture()
        PurpleLogger.current.d(TAG, "main, start success for port:$port")
    }
}