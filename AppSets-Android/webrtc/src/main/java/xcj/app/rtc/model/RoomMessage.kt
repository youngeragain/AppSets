package xcj.app.rtc.model

data class RoomMessage(
    val fromAccount: String,
    val room: String,
    val accountsInRoom: List<String>? = null,
    val message: String? = null
)