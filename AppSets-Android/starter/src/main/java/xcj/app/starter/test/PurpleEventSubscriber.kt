package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

fun interface PurpleEventSubscriber {
    fun onEvent(event: DesignEvent)
}