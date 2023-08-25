package xcj.app.core.foundation

import java.text.SimpleDateFormat
import java.util.*

object InternalSingletonHolder{
    var dateTimePattern:String = "yyyy/MM/dd HH:mm:ss"
    val simpleDateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(dateTimePattern, Locale.getDefault())
    }
}