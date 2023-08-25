package xcj.app.appsets.im

data class Session(val imObj: ImObj, val conversionState: ConversationUiState?) {
    val lastMsg: ImMessage?
        get() {
            return conversionState?.messages?.firstOrNull()
        }
    val id: String = imObj.id
    val isO2O: Boolean
        get() = imObj is ImObj.ImSingle
    val isTitle: Boolean
        get() = imObj is ImObj.ImTitle

}