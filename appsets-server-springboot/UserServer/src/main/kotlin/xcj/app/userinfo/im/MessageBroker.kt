package xcj.app.userinfo.im

interface MessageBroker{
    fun sendMessage(imMessage: ImMessage)
}