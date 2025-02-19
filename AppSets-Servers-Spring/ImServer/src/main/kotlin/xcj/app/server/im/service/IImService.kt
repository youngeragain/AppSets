package xcj.app.server.im.service

import xcj.app.server.im.controller.Message

interface IImService {
    fun saveImMessage()
    fun saveImMessages()
    fun getImMessages(): List<String>
    fun sendMessage(message: Message)
}