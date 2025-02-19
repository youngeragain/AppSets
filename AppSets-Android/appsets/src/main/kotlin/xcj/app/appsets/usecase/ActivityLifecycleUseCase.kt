package xcj.app.appsets.usecase

import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import xcj.app.starter.android.util.PurpleLogger

data class ActivityResumeInfo(
    val activityHash: Int = 0,
    val resumeCount: Int = 0
)

class ActivityLifecycleUseCase : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "ActivityLifecycleUseCase"
    }

    private val _activityResumeState: MutableState<ActivityResumeInfo> =
        mutableStateOf(ActivityResumeInfo())
    val activityResumeState: State<ActivityResumeInfo> = _activityResumeState

    fun setLifecycleOwner(activity: ComponentActivity) {
        val oldState = _activityResumeState.value
        _activityResumeState.value =
            oldState.copy(activityHash = activity.hashCode())
        activity.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        val oldState = _activityResumeState.value
        _activityResumeState.value = oldState.copy(resumeCount = oldState.resumeCount + 1)
        PurpleLogger.current.d(TAG, "onResume, update activityResumeState:${activityResumeState}")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }
}