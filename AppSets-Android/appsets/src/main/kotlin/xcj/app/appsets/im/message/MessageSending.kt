package xcj.app.appsets.im.message

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class MessageSending {
    val sendInfoState: MutableState<MessageSendInfo?> = mutableStateOf(null)

    fun updateSendInfo(messageSendInfo: MessageSendInfo) {
        sendInfoState.value = messageSendInfo
    }
}