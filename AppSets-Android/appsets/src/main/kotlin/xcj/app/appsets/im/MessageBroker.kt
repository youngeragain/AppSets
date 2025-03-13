package xcj.app.appsets.im

import xcj.app.appsets.im.message.ImMessage

interface MessageBroker<C : MessageBrokerConfig> {

    suspend fun bootstrap(config: C)

    suspend fun retry()

    suspend fun close()

    suspend fun sendMessage(imObj: ImObj, imMessage: ImMessage)

}
