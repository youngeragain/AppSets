package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

fun interface PurpleEventPublisher {
    suspend fun publishEvent(event: DesignEvent)
}