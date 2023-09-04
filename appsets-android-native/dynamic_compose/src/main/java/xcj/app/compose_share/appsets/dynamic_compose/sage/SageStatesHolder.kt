package xcj.app.compose_share.appsets.dynamic_compose.sage

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import xcj.app.compose_share.appsets.purple_module.MySharedPreferences
import xcj.app.compose_share.compose.dynamic.AbsStateHolder
import java.util.Calendar

class SageStatesHolder : AbsStateHolder() {
    private val TAG = "SageStatesHolder"
    val isCheckToday: MutableState<Boolean> = mutableStateOf(false)
    val checkedDays: MutableState<Int> = mutableStateOf(0)
    val maxContinuousInterval: MutableState<Int> = mutableStateOf(0)

    override fun onUnLoad() {
        kotlin.runCatching {
            MySharedPreferences.remove("Sage_Times")
        }
    }

    override fun onInit() {
        Log.e(TAG, "onInit")
        kotlin.runCatching {
            val sageTimesJson = MySharedPreferences.getString("Sage_Times")
            if (sageTimesJson.isNullOrEmpty())
                return
            val sageTimeWrapper = Gson().fromJson(sageTimesJson, SageTimeWrapper::class.java)
            val calendar = Calendar.getInstance()
            val todayHours = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            val seconds = calendar.get(Calendar.SECOND)
            val todayMills = minutes * 60000 + seconds * 1000 + todayHours * 3600000
            val todayStartTimeMills = calendar.timeInMillis - todayMills
            val todayEndTimeMills = calendar.timeInMillis + 86_400_000
            var tempInterval = 1
            var max = tempInterval
            for ((index, dateTime) in sageTimeWrapper.dateTimes.withIndex()) {
                if (index > 0) {
                    val difference = dateTime - sageTimeWrapper.dateTimes[index - 1]
                    if (difference > 86_400_000 && difference < 86_400_000 * 2) {
                        tempInterval += 1
                    } else {
                        tempInterval = 1
                    }
                    max = tempInterval.coerceAtLeast(max)
                }
                if (dateTime > todayStartTimeMills && dateTime < todayEndTimeMills) {
                    isCheckToday.value = true
                }
            }
            maxContinuousInterval.value = max
            checkedDays.value = sageTimeWrapper.dateTimes.size
        }.onFailure {
            Log.e(TAG, "onInit fail:" + it.printStackTrace())
        }
    }

    fun setChecked() {
        kotlin.runCatching {
            val sageTimesJson = MySharedPreferences.getString("Sage_Times")
            val gson = Gson()
            val sageTimeWrapper: SageTimeWrapper = if (sageTimesJson.isNullOrEmpty()) {
                SageTimeWrapper(mutableListOf())
            } else {
                gson.fromJson(sageTimesJson, SageTimeWrapper::class.java)
            }
            sageTimeWrapper.dateTimes.add(System.currentTimeMillis())
            MySharedPreferences.putString("Sage_Times", gson.toJson(sageTimeWrapper))
        }.onSuccess {
            isCheckToday.value = true
            checkedDays.value = checkedDays.value + 1
            maxContinuousInterval.value = maxContinuousInterval.value + 1
        }
    }
}