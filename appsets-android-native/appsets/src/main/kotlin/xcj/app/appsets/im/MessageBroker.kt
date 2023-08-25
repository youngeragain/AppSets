package xcj.app.appsets.im

interface MessageBroker<C : MessageBrokerConfig> {
    fun bootstrap(messageBrokerConfig: C)

    fun retry()

    fun close()

    suspend fun sendMessage(imObj: ImObj, imMessage: ImMessage)
}
