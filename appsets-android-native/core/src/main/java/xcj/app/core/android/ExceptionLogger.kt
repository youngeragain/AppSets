package xcj.app.core.android

import xcj.app.core.foundation.DesignLogLevel
import xcj.app.core.foundation.DesignLogger

object ExceptionLogger: DesignLogger {
    var tag: String = "ExceptionLogger"
    fun log(any: Any?,  level: Int= DesignLogLevel.LEVEL_DEBUG) {

    }
}