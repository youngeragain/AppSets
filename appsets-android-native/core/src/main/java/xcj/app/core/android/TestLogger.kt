package xcj.app.core.android

import xcj.app.core.foundation.DesignLogLevel
import xcj.app.core.foundation.DesignLogger
import java.text.SimpleDateFormat
import java.util.*

object TestLogger: DesignLogger {
    var tag:String = "TestLogger"
    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd HH:mm:ss", Locale.CHINA)
    fun log(any: Any?, level: Int= DesignLogLevel.LEVEL_DEBUG) {
        println("$tag || $any")
    }
}
