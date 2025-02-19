package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

interface EventHandler {
    fun processEvent(event: DesignEvent): DesignEvent
}