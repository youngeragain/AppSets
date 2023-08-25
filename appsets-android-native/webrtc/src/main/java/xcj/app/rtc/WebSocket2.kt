package xcj.app.rtc

import com.tinder.scarlet.Message
import com.tinder.scarlet.ShutdownReason
import com.tinder.scarlet.Stream
import com.tinder.scarlet.WebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocketListener
import okio.ByteString
import org.reactivestreams.Subscriber

class WebSocket2: WebSocket, Stream<WebSocket.Event> {
    private val okHttpClient = OkHttpClient.Builder().build()
    private val okhttpWebSocket = okHttpClient.newWebSocket(Request.Builder().url("ws://ws-feed.gdax.com").build(), object :
        WebSocketListener(){
        override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {

        }

        override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
        }

        override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            super.onMessage(webSocket, text)
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
        }

        override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
            super.onOpen(webSocket, response)
        }
    })
    override fun cancel() {
        okhttpWebSocket.cancel()
    }

    override fun close(shutdownReason: ShutdownReason): Boolean {
        return okhttpWebSocket.close(shutdownReason.code, shutdownReason.reason)
    }
    private val disposable = object : Stream.Disposable {
        private var mDisposed = false
        override fun dispose() {
            mDisposed = true
        }

        override fun isDisposed(): Boolean {
            return mDisposed
        }
    }
    override fun open(): Stream<WebSocket.Event> {
        return this
    }

    override fun start(observer: Stream.Observer<WebSocket.Event>): Stream.Disposable {
        return disposable
    }

    override fun subscribe(s: Subscriber<in WebSocket.Event>?) {

    }

    override fun send(message: Message): Boolean {
        if(message is Message.Text){
            return okhttpWebSocket.send(message.value)
        }else if(message is Message.Bytes){
            return okhttpWebSocket.send(ByteString.of(*message.value))
        }
        return false
    }
}