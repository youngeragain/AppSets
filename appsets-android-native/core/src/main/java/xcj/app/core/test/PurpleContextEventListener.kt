package xcj.app.core.test

import xcj.app.core.foundation.DesignEvent

interface PurpleContextEventListener:PurpleContextListener {
    fun onEvent(event: DesignEvent)
}