package xcj.app.starter.test

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import xcj.app.starter.foundation.lazyStaticProvider

@JvmField
val LocalApplication = lazyStaticProvider<Application>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalActivities = lazyStaticProvider<Map<Activity, Lifecycle.State>>().apply {
    //Please provide this value in due course
}

val LocalTopActivity: Activity
    get() = LocalActivities.current.keys.last()

@JvmField
val LocalPurpleCoroutineScope = lazyStaticProvider<CoroutineScope>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalAndroidContextFileDir = lazyStaticProvider<AndroidContextFileDir>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalPurple = lazyStaticProvider<PurpleContext>().apply {
    //Please provide this value in due course
}

@JvmField
val LocalPurpleEventPublisher = lazyStaticProvider<PurpleEventPublisher>().apply {
    //Please provide this value in due course
}

