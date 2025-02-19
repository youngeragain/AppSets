package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

interface PurpleContextEventListener : PurpleContextListener {
    fun onEvent(event: DesignEvent)
}