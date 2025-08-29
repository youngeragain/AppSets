package xcj.app.starter.foundation

import androidx.lifecycle.Lifecycle

interface LifecycleStateAware : Aware {
    fun onLifecycleState(state: Lifecycle.State)
}