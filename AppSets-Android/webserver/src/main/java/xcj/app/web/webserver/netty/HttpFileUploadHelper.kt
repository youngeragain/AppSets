package xcj.app.web.webserver.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import xcj.app.web.webserver.interfaces.ComponentsProvider
import xcj.app.web.webserver.interfaces.ListenersProvider

object HttpFileUploadHelper {

    private const val TAG = "HttpFileUploadHelper"

    fun handleHttpContent(
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        componentsProvider: ComponentsProvider?,
        listenersProvider: ListenersProvider?
    ) {
        val httpPostRequestDecoder = httpRequestWrapper.httpPostRequestDecoder
        if (httpPostRequestDecoder == null) {
            return
        }
        val httpContent = httpRequestWrapper.httpContent
        if (httpContent != null) {
            try {
                httpPostRequestDecoder.offer(httpContent)
            } catch (e: HttpPostRequestDecoder.ErrorDataDecoderException) {
                ComposedApiWebHandler.sendBadRequest(ctx)
                e.printStackTrace()
            }
        }
        val fileUploadN = httpRequestWrapper.contentUploadN
        if (httpContent != null && fileUploadN != null) {
            fileUploadN.addHttpContent(
                httpContent,
                httpPostRequestDecoder,
                componentsProvider,
                listenersProvider
            )
        }
    }
}