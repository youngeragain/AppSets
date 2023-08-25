package xcj.app.rtc

import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import xcj.app.rtc.model.RTMMessage

interface RTCSocket{
    @Send
    fun sendRTMMessage(rtmMessage: RTMMessage)

    @Receive
    fun onRTMMessage(rtmMessage:RTMMessage)
}