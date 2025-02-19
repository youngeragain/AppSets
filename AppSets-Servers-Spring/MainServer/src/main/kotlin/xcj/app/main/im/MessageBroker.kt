package xcj.app.main.im

interface MessageBroker {
    fun sendMessage(imMessage: ImMessage)

    fun onReceivedMessage(imMessage: ImMessage)
}