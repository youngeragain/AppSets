package xcj.app.appsets.notification

import android.app.NotificationManager

data class NotificationChannel(
    val id: String,
    val name: Int,
    val description: Int,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
)