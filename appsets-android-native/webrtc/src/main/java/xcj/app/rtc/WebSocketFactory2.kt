package xcj.app.rtc

import com.tinder.scarlet.WebSocket

class WebSocketFactory2: WebSocket.Factory{
    override fun create(): WebSocket {
        return WebSocket2()
    }
}