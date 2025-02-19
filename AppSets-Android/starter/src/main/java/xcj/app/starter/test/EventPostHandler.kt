package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

interface EventPostHandler : EventHandler {
    override fun processEvent(event: DesignEvent): DesignEvent
}