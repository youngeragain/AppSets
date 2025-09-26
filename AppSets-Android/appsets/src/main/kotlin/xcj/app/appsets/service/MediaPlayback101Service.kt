package xcj.app.appsets.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import xcj.app.appsets.ui.compose.main.MainActivity
import xcj.app.starter.android.util.PurpleLogger

@SuppressLint("UnsafeOptInUsageError")
class MediaPlayback101Service : MediaLibraryService() {
    private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback {
        private val TAG = "CustomMediaLibrarySessionCallback"

        private var customCommands: List<CommandButton>

        init {
            val playbackItemCommand = CommandButton
                .Builder(CommandButton.ICON_UNDEFINED)
                .setSessionCommand(SessionCommand("set_playback_item", Bundle.EMPTY))
                .build()
            customCommands =
                listOf(
                    playbackItemCommand
                    /*   getShuffleCommandButton(
                           SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON, Bundle.EMPTY)
                       ),
                       getShuffleCommandButton(
                           SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF, Bundle.EMPTY)
                       )*/
                )
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            PurpleLogger.current.d(TAG, "onConnect")
            val availableSessionCommands =
                MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
            customCommands.forEach { commandButton ->
                // Add custom command to available session commands.
                commandButton.sessionCommand?.let(availableSessionCommands::add)
            }
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableSessionCommands.build())
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            PurpleLogger.current.d(TAG, "onCustomCommand, ${customCommand.customAction}")
            /*if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON == customCommand.customAction) {
                // Enable shuffling.
                player.shuffleModeEnabled = true
                // Change the custom layout to contain the `Disable shuffling` command.
                session.setCustomLayout(ImmutableList.of(customCommands[1]))
            } else if (CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF == customCommand.customAction) {
                // Disable shuffling.
                player.shuffleModeEnabled = false
                // Change the custom layout to contain the `Enable shuffling` command.
                session.setCustomLayout(ImmutableList.of(customCommands[0]))
            }else */
            if (customCommand.customAction == "set_playback_item") {
                val itemUrl = customCommand.customExtras.getString("url")
                PurpleLogger.current.d(TAG, "url:${itemUrl}")
                itemUrl?.let {
                    val mediaItem = MediaItem.fromUri(it)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            PurpleLogger.current.d(TAG, "onGetLibraryRoot")
            return super.onGetLibraryRoot(session, browser, params)
            /*if (params != null && params.isRecent) {
                // The service currently does not support playback resumption. Tell System UI by returning
                // an error of type 'RESULT_ERROR_NOT_SUPPORTED' for a `params.isRecent` request. See
                // https://github.com/androidx/media/issues/355
                return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
            }
            return Futures.immediateFuture(LibraryResult.ofItem(MediaItemTree.getRootItem(), params))
          */
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            PurpleLogger.current.d(TAG, "onGetItem")
            /* val item =
                 MediaItemTree.getItem(mediaId)
                     ?: return Futures.immediateFuture(
                         LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                     )*/
            //return Futures.immediateFuture(LibraryResult.ofItem(item, /* params= */ null))
            return super.onGetItem(session, browser, mediaId)
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<Void>> {
            PurpleLogger.current.d(TAG, "onSubscribe")
            /*val children =
                MediaItemTree.getChildren(parentId)
                    ?: return Futures.immediateFuture(
                        LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                    )
            session.notifyChildrenChanged(browser, parentId, children.size, params)*/
            //return Futures.immediateFuture(LibraryResult.ofVoid())
            return super.onSubscribe(session, browser, parentId, params)
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            PurpleLogger.current.d(TAG, "onGetChildren")
            /*val children =
                MediaItemTree.getChildren(parentId)
                    ?: return Futures.immediateFuture(
                        LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                    )*/

            //return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
            return super.onGetChildren(session, browser, parentId, page, pageSize, params)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
        ): ListenableFuture<List<MediaItem>> {
            /*val updatedMediaItems: List<MediaItem> =
                mediaItems.mapNotNull { mediaItem ->
                    *//*if (mediaItem.requestMetadata.searchQuery != null)
                        getMediaItemFromSearchQuery(mediaItem.requestMetadata.searchQuery!!)
                    else MediaItemTree.getItem(mediaItem.mediaId) ?: mediaItem*//*
                    null
                }
            return Futures.immediateFuture(updatedMediaItems)*/
            PurpleLogger.current.d(TAG, "onAddMediaItems")
            return super.onAddMediaItems(mediaSession, controller, mediaItems)
        }

        private fun getMediaItemFromSearchQuery(query: String): MediaItem? {
            // Only accept query with pattern "play [Title]" or "[Title]"
            // Where [Title]: must be exactly matched
            // If no media with exact name found, play a random media instead
            val mediaTitle =
                if (query.startsWith("play ", ignoreCase = true)) {
                    query.drop(5)
                } else {
                    query
                }
            return null
            // return MediaItemTree.getItemFromTitle(mediaTitle) ?: MediaItemTree.getRandomItem()
        }
    }

    private inner class MediaSessionServiceListener : Listener {

        /**
         * This method is only required to be implemented on Android 12 or above when an attempt is made
         * by a media controller to resume playback when the {@link MediaSessionService} is in the
         * background.
         */
        @SuppressLint("MissingPermission") // TODO: b/280766358 - Request this permission at runtime.
        override fun onForegroundServiceStartNotAllowedException() {
            val pendingIntent =
                TaskStackBuilder.create(this@MediaPlayback101Service).run {
                    addNextIntent(Intent(this@MediaPlayback101Service, MainActivity::class.java))
                    getPendingIntent(
                        0,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            val builder =
                NotificationCompat.Builder(this@MediaPlayback101Service, CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(xcj.app.compose_share.R.drawable.ic_appsets_44)
                    .setContentTitle(getString(xcj.app.appsets.R.string.notification_content_title))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(getString(xcj.app.appsets.R.string.notification_content_text))
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
            val notificationManagerCompat =
                NotificationManagerCompat.from(this@MediaPlayback101Service)
            notificationManagerCompat.notify(FOREGROUND_NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val TAG = "MediaPlayback101Service"

        const val CHANNEL_ID = "TAG"
        const val FOREGROUND_NOTIFICATION_ID = 3

        private const val SEARCH_QUERY_PREFIX_COMPAT = "androidx://media3-session/playFromSearch"
        private const val SEARCH_QUERY_PREFIX = "androidx://media3-session/setMediaUri"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON =
            "android.media3.session.demo.SHUFFLE_ON"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF =
            "android.media3.session.demo.SHUFFLE_OFF"
    }

    private lateinit var player: ExoPlayer

    private lateinit var mediaLibrarySession: MediaLibrarySession


    override fun onBind(intent: Intent?): IBinder? {
        val onBind = super.onBind(intent)
        PurpleLogger.current.d(TAG, "onBind")
        return onBind
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        PurpleLogger.current.d(TAG, "onStartCommand")
        return onStartCommand
    }

    override fun onCreate() {
        super.onCreate()
        createPlayer()
        val sessionServiceListener = MediaSessionServiceListener()
        setListener(sessionServiceListener)
    }

    private fun createPlayer() {
        PurpleLogger.current.d(TAG, "createPlayer")
        player =
            ExoPlayer.Builder(this).build().apply {
                playWhenReady = true
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build()
                setAudioAttributes(audioAttributes, true)
                setHandleAudioBecomingNoisy(true)
            }
        //MediaItemTree.initialize(assets)
    }

    private fun getSingleTopActivity(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getBackStackedActivity(): PendingIntent {
        return TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@MediaPlayback101Service, MainActivity::class.java))
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private fun getShuffleCommandButton(sessionCommand: SessionCommand): CommandButton? {
        /*val isOn = sessionCommand.customAction == CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON
        return CommandButton.Builder()
            .setDisplayName(
                getString(
                    if (isOn) R.string.exo_controls_shuffle_on_description
                    else R.string.exo_controls_shuffle_off_description
                )
            )
            .setSessionCommand(sessionCommand)
            .setIconResId(if (isOn) R.drawable.exo_icon_shuffle_off else R.drawable.exo_icon_shuffle_on)
            .build()*/
        return null
    }


    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        player.stop()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        PurpleLogger.current.d(TAG, "onGetSession")
        if (!::mediaLibrarySession.isInitialized) {
            val librarySessionCallback = CustomMediaLibrarySessionCallback()
            mediaLibrarySession =
                MediaLibrarySession.Builder(this, player, librarySessionCallback)
                    .setSessionActivity(getSingleTopActivity())
                    //.setCustomLayout(ImmutableList.of(customCommands[0]))
                    .setBitmapLoader(CacheBitmapLoader(DataSourceBitmapLoader(/* context= */ this)))
                    .build()
        }
        return mediaLibrarySession
    }


    override fun onDestroy() {
        mediaLibrarySession.setSessionActivity(getBackStackedActivity())
        mediaLibrarySession.release()
        player.release()
        clearListener()
        super.onDestroy()
    }

}