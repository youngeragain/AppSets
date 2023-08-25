package xcj.app.appsets.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import xcj.app.appsets.R
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.ui.compose.MainActivity
import xcj.app.appsets.usecase.UserRelationsCase

@UnstableApi
class NotificationPusher {
    private var notificationId: Int = 1
    private fun getNotificationId(): Int {
        val temp = notificationId
        notificationId += 1
        return temp
    }

    @SuppressLint("MissingPermission")
    fun pushConversionNotification(
        context: Context,
        notificationManagerCompat: NotificationManagerCompat,
        sessionId: String,
        imMessage: ImMessage
    ) {
        val iconUrl = if (imMessage.msgToInfo.isImgGroupMessage) {
            imMessage.msgToInfo.iconUrl
        } else {
            imMessage.msgFromInfo.avatarUrl
        }
        val name = if (imMessage.msgToInfo.isImgGroupMessage) {
            "${imMessage.msgToInfo.name}/${imMessage.msgFromInfo.name}"
        } else {
            if (!UserRelationsCase.getInstance().hasUserRelated(imMessage.msgFromInfo.uid)) {
                "${imMessage.msgFromInfo.name} (临时对话)"
            } else {
                imMessage.msgFromInfo.name
            }
        }
        imMessage.notificationId = getNotificationId()
        val notificationItselfIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("imMessageId", imMessage.id)
            putExtra("imMessageNotificationId", imMessage.notificationId)
        }
        val notificationItselfPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationItselfIntent,
            PendingIntent.FLAG_MUTABLE
        )
        //Glide.with(context).downloadOnly().load(iconUrl).submit(256, 256)
        // Key for the string that's delivered in the action's intent.
        val remoteInput: RemoteInput = RemoteInput.Builder("key_text_reply").run {
            setLabel("reply")
            build()
        }
        // Build a PendingIntent for the reply action to trigger.
        val replayIntent = Intent().apply {
            action = "xcj.app.conversation.session.im.session_reply"
            putExtra("imMessageId", imMessage.id)
            putExtra("imMessageNotificationId", imMessage.notificationId)
            putExtra("sessionId", sessionId)
        }
        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            replayIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction: NotificationCompat.Action =
            NotificationCompat.Action.Builder(
                R.drawable.ic_round_reply_24, "reply", replyPendingIntent
            )
                .addRemoteInput(remoteInput)
                .build()
        val builder = NotificationCompat.Builder(context, "Conversion_Channel_1")
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .setContentTitle(name)
            .setContentText(imMessage.contentByMyType())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(notificationItselfPendingIntent)
            .addAction(replyAction)
            .setAutoCancel(true)
        Glide.with(context)
            .asBitmap()
            .load(iconUrl)
            .centerCrop()
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    val notification = builder.build()
                    notificationManagerCompat.notify(imMessage.notificationId!!, notification)
                    return true
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    val notification = builder.setLargeIcon(resource).build()
                    notificationManagerCompat.notify(imMessage.notificationId!!, notification)
                    return true
                }
            })
            .submit(120, 120)

    }

}