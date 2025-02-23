package xcj.app.starter.test

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import xcj.app.starter.foundation.staticProvider

@JvmField
val LocalApplication = staticProvider<Application>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalActivities = staticProvider<Map<Activity, Lifecycle.State>>().apply {
    //Please provide this value in due course
}

val LocalTopActivity: Activity
    get() = LocalActivities.current.keys.last()

@JvmField
val LocalPurpleCoroutineScope = staticProvider<CoroutineScope>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalAndroidContextFileDir = staticProvider<AndroidContextFileDir>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalPurple = staticProvider<PurpleContext>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalPurpleEventPublisher = staticProvider<PurpleEventPublisher>().apply {
    //Please provide this value in due course
}

