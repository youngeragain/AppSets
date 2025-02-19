package xcj.app.appsets.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.request.transformations
import coil3.toBitmap
import coil3.transform.CircleCropTransformation
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.ui.compose.conversation.ImSessionBubbleActivity
import xcj.app.appsets.ui.compose.main.MainActivity
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalPurple
import xcj.app.starter.test.SimplePurpleForAndroidContext
import kotlin.math.absoluteValue

class NotificationPusher() {

    companion object {
        private const val TAG = "NotificationPusher"

        const val REMOTE_BUILDER_KEY_IM_INPUT = "key_text_reply"
        const val ACTION_RECEIVER_IM_SESSION_REPLY = "xcj.app.conversation.session.im.session_reply"
        const val ACTION_SHORTCUT_IM_SESSION = "xcj.app.conversation.session.im.session_shortcut"
        const val NOTIFICATION_ID_START_INDEX = 10000
    }

    private var notificationId: Int = NOTIFICATION_ID_START_INDEX

    private val shortcutInfoMap: MutableMap<String, ShortcutInfoCompat> = mutableMapOf()

    private val personMap: MutableMap<String, Person> = mutableMapOf()

    private var mNotificationManagerCompat: NotificationManagerCompat? = null

    private fun ensureNotificationManager(context: Context): NotificationManagerCompat {
        if (mNotificationManagerCompat == null) {
            mNotificationManagerCompat = NotificationManagerCompat.from(context.applicationContext)
        }
        return mNotificationManagerCompat!!
    }

    private suspend fun requestImSessionIcon(
        context: Context,
        session: Session,
        imMessage: ImMessage
    ): Bitmap? {
        PurpleLogger.current.d(TAG, "requestSessionIcon")
        val iconUrl = if (imMessage.toInfo.toType == ImMessage.TYPE_O2M) {
            imMessage.toInfo.bioUrl
        } else {
            imMessage.fromInfo.bioUrl
        }
        val imageRequest = ImageRequest.Builder(context)
            .data(iconUrl)
            .size(120, 120)
            .transformations(CircleCropTransformation())
            .build()

        //avoid Lifecycle change!!!
        //context.imageLoader.enqueue()

        val imageResult: ImageResult = context.imageLoader.execute(imageRequest)
        val iconBitmap = when (imageResult) {
            is ErrorResult -> {
                PurpleLogger.current.d(
                    TAG,
                    "pushConversionNotification, request avatar, failed! error drawable:${imageResult.image}!"
                )
                imageResult.image?.toBitmap()
            }

            is SuccessResult -> {
                PurpleLogger.current.d(
                    TAG,
                    "pushConversionNotification, request avatar, success"
                )
                imageResult.image.toBitmap()
            }
        }
        PurpleLogger.current.d(
            TAG,
            "pushConversionNotification, iconBitmap:$iconBitmap"
        )
        return iconBitmap
    }

    @SuppressLint("MissingPermission")
    suspend fun pushConversionNotification(
        context: Context,
        session: Session,
        imMessage: ImMessage
    ) {
        if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            PurpleLogger.current.d(
                TAG,
                "pushConversionNotification, no post notification permission! return!"
            )
            return
        }
        val simplePurpleForAndroidContext =
            LocalPurple.current as SimplePurpleForAndroidContext
        if (!simplePurpleForAndroidContext.isApplicationInBackground()) {
            PurpleLogger.current.d(
                TAG,
                "pushConversionNotification, App not in background! return!"
            )
            return
        }
        PurpleLogger.current.d(TAG, "pushConversionNotification start")

        val notificationId = session.id.hashCode().absoluteValue

        val sessionIconBitmap = requestImSessionIcon(context, session, imMessage)

        val notificationBuilder =
            makeNotificationBuilderForImSession(
                context,
                session,
                imMessage,
                notificationId,
                sessionIconBitmap
            )

        val shouldShownAsSystemConversationStyleNotification =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

        if (shouldShownAsSystemConversationStyleNotification) {
            val isExistPerson = personMap.contains(session.id)
            makeSessionShortcutInfo(context, session, imMessage, sessionIconBitmap, isExistPerson)
            makeBubbleForNotificationIfNeeded(
                context,
                session,
                imMessage,
                notificationBuilder,
                notificationId,
                isExistPerson
            )
        }

        val notification = notificationBuilder.build()
        val notificationManager = ensureNotificationManager(context)
        notificationManager.notify(notificationId, notification)
        PurpleLogger.current.d(TAG, "pushConversionNotification final")
    }

    private fun makeBubbleForNotificationIfNeeded(
        context: Context,
        session: Session,
        imMessage: ImMessage,
        notificationBuilder: NotificationCompat.Builder,
        notificationId: Int,
        isExistPerson: Boolean,
    ) {
        PurpleLogger.current.d(TAG, "makeBubbleForNotificationIfNeeded")
        val person = personMap[session.id]
        if (person == null) {
            PurpleLogger.current.d(
                TAG,
                "makeBubbleForNotificationIfNeeded, person is null, return!"
            )
            return
        }
        PurpleLogger.current.d(TAG, "makeBubbleForNotificationIfNeeded, person:$person!")
        val contentText = if (imMessage.toInfo.toType == ImMessage.TYPE_O2M) {
            "${imMessage.fromInfo.name}\n${
                ImMessage.readableContent(
                    context,
                    imMessage
                ) ?: ""
            }"
        } else {
            ImMessage.readableContent(context, imMessage) ?: ""
        }
        val message =
            NotificationCompat.MessagingStyle.Message(
                contentText,
                imMessage.timestamp.time,
                person
            )
        val messagingStyle = NotificationCompat.MessagingStyle(person)
        messagingStyle.addMessage(message)

        notificationBuilder
            .setStyle(messagingStyle)
            .setShortcutId(session.id)

        val bubbleIntent = Intent(context, ImSessionBubbleActivity::class.java).apply {
            putExtra(ImMessage.KEY_IM_MESSAGE_NOTIFICATION_ID, notificationId)
            putExtra(ImMessage.KEY_IM_MESSAGE_ID, imMessage.id)
            putExtra(ImMessage.KEY_SESSION_ID, session.id)
        }
        val bubblePendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                bubbleIntent,
                PendingIntent.FLAG_MUTABLE
            )

        val bubble =
            NotificationCompat.BubbleMetadata.Builder(bubblePendingIntent, person.icon!!)
                .setDesiredHeight(600)
                .build()

        notificationBuilder.setBubbleMetadata(bubble)
        notificationBuilder.addPerson(person)
    }

    private fun makeSessionShortcutInfo(
        context: Context,
        session: Session,
        imMessage: ImMessage,
        iconBitmap: Bitmap?,
        isExistPerson: Boolean
    ) {
        PurpleLogger.current.d(TAG, "makeSessionShortcutInfo")
        if (!personMap.contains(session.id)) {
            val icon = if (iconBitmap != null) {
                IconCompat.createWithBitmap(iconBitmap)
            } else {
                IconCompat.createWithResource(
                    context,
                    xcj.app.appsets.R.drawable.ai_model_logo_google_gemini
                )
            }
            val person = Person.Builder()
                .setName(session.imObj.name)
                .setIcon(icon)
                .build()
            personMap[session.id] = person
        }
        val person = personMap[session.id]
        if (person == null) {
            return
        }
        if (!shortcutInfoMap.contains(session.id)) {
            val shortcutInfoIntent = Intent(context, ImSessionBubbleActivity::class.java).apply {
                action = ACTION_SHORTCUT_IM_SESSION
                putExtra(ImMessage.KEY_IM_MESSAGE_NOTIFICATION_ID, notificationId)
                putExtra(ImMessage.KEY_IM_MESSAGE_ID, imMessage.id)
                putExtra(ImMessage.KEY_SESSION_ID, session.id)
            }

            val shortcutInfo = ShortcutInfoCompat.Builder(context, session.id)
                .setLongLived(true)
                .setIntent(shortcutInfoIntent)
                .setShortLabel(session.imObj.name)
                .setIcon(person.icon)
                .setPerson(person)
                .build()
            shortcutInfoMap[session.id] = shortcutInfo
        }
        if (!isExistPerson) {
            val allShortcuts = shortcutInfoMap.values.toList()
            ShortcutManagerCompat.setDynamicShortcuts(context, allShortcuts)
        }
    }

    @SuppressLint("MutableImplicitPendingIntent")
    private fun makeNotificationBuilderForImSession(
        context: Context,
        session: Session,
        imMessage: ImMessage,
        notificationId: Int,
        sessionIconBitmap: Bitmap?
    ): NotificationCompat.Builder {
        PurpleLogger.current.d(
            TAG,
            "makeNotificationBuilderForImMessage, (imMessageId:${imMessage.id}, sessionId:${session.id}," +
                    " imMessageNotificationId:$notificationId)"
        )
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(ImMessage.KEY_IM_MESSAGE_NOTIFICATION_ID, notificationId)
            putExtra(ImMessage.KEY_IM_MESSAGE_ID, imMessage.id)
            putExtra(ImMessage.KEY_SESSION_ID, session.id)
        }
        val notificationPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle = if (imMessage.toInfo.toType == ImMessage.TYPE_O2M) {
            "${imMessage.toInfo.name}"
        } else {
            imMessage.fromInfo.name
        }
        val contentText = if (imMessage.toInfo.toType == ImMessage.TYPE_O2M) {
            "${imMessage.fromInfo.name}\n${ImMessage.readableContent(context, imMessage) ?: ""}"
        } else {
            ImMessage.readableContent(context, imMessage) ?: ""
        }
        val notificationBuilder =
            NotificationCompat
                .Builder(context, NotificationChannels.CHANNEL_ID_CONVERSATION_1)
                .setSmallIcon(xcj.app.appsets.R.drawable.ic_appsets_44)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(notificationPendingIntent)
                .setGroup(session.id)
                .setGroupSummary(true)
                .setAutoCancel(true)

        val isReplyNotification = false

        if (isReplyNotification) {
            val replyText = context.getString(xcj.app.appsets.R.string.reply)
            // Key for the string that's delivered in the action's intent.
            val remoteInput: RemoteInput =
                RemoteInput.Builder(REMOTE_BUILDER_KEY_IM_INPUT)
                    .setLabel(replyText)
                    .build()
            // Build a PendingIntent for the reply action to trigger.
            val replayIntent = Intent().apply {
                action = ACTION_RECEIVER_IM_SESSION_REPLY
                putExtra(ImMessage.KEY_IM_MESSAGE_NOTIFICATION_ID, notificationId)
                putExtra(ImMessage.KEY_IM_MESSAGE_ID, imMessage.id)
                putExtra(ImMessage.KEY_SESSION_ID, session.id)
            }
            val replyPendingIntent: PendingIntent? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        replayIntent,
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
                    )
                } else {
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        replayIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                }

            val replyAction: NotificationCompat.Action =
                NotificationCompat.Action
                    .Builder(
                        xcj.app.compose_share.R.drawable.ic_round_reply_24,
                        replyText,
                        replyPendingIntent
                    )
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build()
            notificationBuilder.addAction(replyAction)
        }
        if (sessionIconBitmap != null) {
            notificationBuilder.setLargeIcon(sessionIconBitmap)
        }
        return notificationBuilder
    }


    fun cancelNotification(context: Context, notificationId: Int) {
        PurpleLogger.current.d(
            TAG,
            "cancelNotification, notificationId:$notificationId"
        )
        val notificationManager = ensureNotificationManager(context)
        notificationManager.cancel(notificationId)
    }

}