package xcj.app.rtc

import xcj.app.rtc.ktx.jsonify
import xcj.app.rtc.model.LoginMessage
import xcj.app.rtc.model.RTMMessage
import xcj.app.rtc.model.WebRTCMessage


class RTCExchange {
    private val rtcSocket:RTCSocket by lazy {
        ScarletHelper.scarletInstance.create()
    }
    fun login(loginMessage: LoginMessage){
        rtcSocket.sendRTMMessage(RTMMessage("offer", loginMessage.jsonify()))
    }
    fun createOffer(rtcMessage: WebRTCMessage){
        rtcSocket.sendRTMMessage(RTMMessage("offer", rtcMessage.jsonify()))
    }

    fun createAnswer(rtcMessage:WebRTCMessage){
        rtcSocket.sendRTMMessage(RTMMessage("answer", rtcMessage.jsonify()))
    }

    fun candidate(rtcMessage:WebRTCMessage){
        rtcSocket.sendRTMMessage(RTMMessage("candidate", rtcMessage.jsonify()))
    }

    fun logout(){
        rtcSocket.sendRTMMessage(RTMMessage("bye", "bye"))
    }
}