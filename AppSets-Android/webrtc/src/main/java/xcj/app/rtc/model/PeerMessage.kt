package xcj.app.rtc.model

data class PeerMessage(val fromAccount: String, val toAccount: String, val message: String? = null)