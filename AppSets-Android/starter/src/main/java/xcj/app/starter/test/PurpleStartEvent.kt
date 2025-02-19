package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

data class PurpleStartEvent(
    val startDateTime: String,
    val contextInformation: String
) : DesignEvent