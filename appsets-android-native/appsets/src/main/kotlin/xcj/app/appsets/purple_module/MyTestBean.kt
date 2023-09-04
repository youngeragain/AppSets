package xcj.app.appsets.purple_module

import xcj.app.core.android.TestLogger
import xcj.app.core.test.annotations.InstanceGetModel
import java.util.Calendar

@InstanceGetModel.Singleton
class MyTestBean {

    fun showTodayDate(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val week = calendar.get(Calendar.WEEK_OF_MONTH)
        val weekOfDay = calendar.get(Calendar.DAY_OF_WEEK)
        TestLogger.log("$year 年${month+1}月 ${day}日 $week} $weekOfDay")
    }
}