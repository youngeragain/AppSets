package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

interface EventPreHandler : EventHandler {
    override fun processEvent(event: DesignEvent): DesignEvent
}