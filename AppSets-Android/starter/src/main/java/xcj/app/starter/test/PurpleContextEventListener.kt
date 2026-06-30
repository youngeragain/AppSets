package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

interface PurpleContextEventListener : PurpleContextListener {
    suspend fun onEvent(event: DesignEvent)
}