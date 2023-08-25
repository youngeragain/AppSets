package xcj.app.appsets.service

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer

@UnstableApi
class MediaPlayback101Service : MediaBrowserServiceCompat() {
    private val MY_MEDIA_ROOT_ID = "media_root_id"
    private val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"

    private var currentMediaItemIndex: Int = 0
    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private var mediaSession: MediaSessionCompat? = null
    private var mediaControllerCompat: MediaControllerCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private var playerListener: PlayerEventListener? = null
    private var exoPlayer: ExoPlayer? = null

    fun initExoPlayer() {
        if (exoPlayer != null)
            return
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            playWhenReady = true
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            val playerEventListener = PlayerEventListener()
            playerListener = playerEventListener
            addListener(playerEventListener)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        val onBind = super.onBind(intent)
        Log.e("MediaPlayback101Service", "onBind")
        return onBind
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        Log.e("MediaPlayback101Service", "onStartCommand")
        return onStartCommand
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("MediaPlayback101Service", "onCreate")
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MediaPlayback101Service").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(MySessionCallback())

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
            isActive = true
        }
        mediaControllerCompat = mediaSession!!.controller
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Log.e("MediaPlayback101Service", "onGetRoot")
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.e("MediaPlayback101Service", "onLoadChildren")
        //  Browsing not allowed
        if (MY_EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }

        // Assume for example that the music catalog is already loaded/cached.

        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID == parentId) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            isActive = false
            release()
        }

        // Cancel coroutines when the service is going away.
        //serviceJob.cancel()

        // Free ExoPlayer resources.
        if (playerListener != null)
            exoPlayer?.removeListener(playerListener!!)
        exoPlayer?.release()
    }

    //UI操作的回调
    private inner class MySessionCallback : MediaSessionCompat.Callback() {

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            /*  Log.e("MySessionCallback", "onCommand:\n" +
                      "command:${command}\n" +
                      "extras:${extras}")*/
            when (command) {
                "set_playback_item" -> {
                    if (exoPlayer == null)
                        return
                    val itemUrl = extras?.getString("url") ?: return
                    Log.e("MySessionCallback", "url:${itemUrl}")
                    val state = getMediaSessionPlaybackState(
                        exoPlayer!!.playbackState,
                        exoPlayer!!.playWhenReady
                    )
                    if (state != PlaybackStateCompat.STATE_NONE) {
                        //exoPlayer.pause()
                        exoPlayer!!.stop()
                    }
                    exoPlayer!!.setMediaItem(MediaItem.fromUri(itemUrl))
                    exoPlayer!!.prepare()
                }

                "init_exoplayer" -> {
                    initExoPlayer()
                }
            }
        }

        override fun onPlay() {
            if (exoPlayer == null)
                return
            Log.e(
                "MySessionCallback",
                "onPlay, exoPlayer.playbackState:${exoPlayer!!.playbackState}"
            )
            val state =
                getMediaSessionPlaybackState(exoPlayer!!.playbackState, exoPlayer!!.playWhenReady)
            if (state == PlaybackStateCompat.STATE_PAUSED) {
                exoPlayer!!.play()
                return
            }
            if (state == PlaybackStateCompat.STATE_NONE || state == PlaybackStateCompat.STATE_STOPPED) {
                exoPlayer!!.prepare()
                return
            }
            Log.e("MySessionCallback", "onPlay when exoPlayer on other state!")
        }


        override fun onPause() {
            if (exoPlayer == null)
                return
            Log.e("MySessionCallback", "onPause")
            exoPlayer!!.pause()
        }

        override fun onStop() {
            if (exoPlayer == null)
                return
            Log.e("MySessionCallback", "onStop")
            exoPlayer!!.stop()
        }

        override fun onRewind() {
            if (exoPlayer == null)
                return
            Log.e("MySessionCallback", "onRewind")
            exoPlayer!!.seekForward()
        }

        override fun onSkipToNext() {
            if (exoPlayer == null)
                return
            Log.e("MySessionCallback", "onSkipToNext")
        }

        override fun onSkipToPrevious() {
            if (exoPlayer == null)
                return
            Log.e("MySessionCallback", "onSkipToPrevious")
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            Log.e("MySessionCallback", "onCustomAction")
        }

        override fun onAddQueueItem(description: MediaDescriptionCompat?) {
            super.onAddQueueItem(description)
        }
    }

    private inner class PlayerEventListener : Player.Listener {
        private var currentWindowCount: Int = 0
        private var currentMediaItemIndex: Int = 0
        private val EMPTY_METADATA_COMPACT by lazy {
            MediaMetadataCompat.Builder().build()
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.e("PlayerEventListener", "onPlayerStateChanged, playerState:${playbackState}")
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {
                    //notificationManager.showNotificationForPlayer(currentPlayer)
                    if (playbackState == Player.STATE_READY) {

                        // When playing/paused save the current media item in persistent
                        // storage so that playback can be resumed between device reboots.
                        // Search for "media resumption" for more information.
                        //saveRecentSongToStorage()

                        if (!playWhenReady) {
                            // If playback is paused we remove the foreground state which allows the
                            // notification to be dismissed. An alternative would be to provide a
                            // "close" button in the notification which stops playback and clears
                            // the notification.
                            stopForeground(false)
                            //isForegroundService = false
                        }
                    }
                }

                else -> {
                    // notificationManager.hideNotification()
                }
            }

        }


        override fun onEvents(player: Player, events: Player.Events) {

            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)
                || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)
            ) {
                currentMediaItemIndex = if (currentPlaylistItems.isNotEmpty()) {
                    Util.constrainValue(
                        player.currentMediaItemIndex,
                        0,
                        currentPlaylistItems.size - 1
                    )
                } else 0
            }
            var invalidateMetadata = false
            var invalidatePlaybackState = false
            if (events.contains(Player.EVENT_POSITION_DISCONTINUITY)) {
                if (currentMediaItemIndex != player.currentMediaItemIndex) {
                    invalidateMetadata = true
                }
                invalidatePlaybackState = true
            }

            if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
                val windowCount = player.currentTimeline.windowCount
                val mediaItemIndex = player.currentMediaItemIndex
                /*if (queueNavigator != null) {
                    queueNavigator.onTimelineChanged(player)
                    invalidatePlaybackState = true
                } else */if (currentWindowCount != windowCount || currentMediaItemIndex != mediaItemIndex) {
                    // active queue item and queue navigation actions may need to be updated
                    invalidatePlaybackState = true
                }
                currentWindowCount = windowCount
                invalidateMetadata = true
            }

            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_REPEAT_MODE_CHANGED,
                    Player.EVENT_PLAYBACK_PARAMETERS_CHANGED
                )
            ) {
                invalidatePlaybackState = true
            }

            Log.e(
                "PlayerEventListener",
                "onEvents, invalidatePlaybackState:${invalidatePlaybackState}, invalidateMetadata:${invalidateMetadata}"
            )

            if (invalidatePlaybackState) {
                invalidateMediaSessionPlaybackState()
            }

            if (invalidateMetadata) {
                invalidateMediaSessionMetadata()
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            invalidateMediaSessionMetadata()
        }

        override fun onMetadata(metadata: Metadata) {
            invalidateMediaSessionMetadata()
        }

        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onPlaylistMetadataChanged(mediaMetadata)
            invalidateMediaSessionMetadata()
        }

        private fun invalidateMediaSessionMetadata() {
            val player = exoPlayer ?: return
            val metadata = getMetadata(player)
            mediaSession?.setMetadata(metadata)
        }

        private fun getMetadata(player: Player): MediaMetadataCompat {
            if (player.currentTimeline.isEmpty) {
                return EMPTY_METADATA_COMPACT
            }
            val builder = MediaMetadataCompat.Builder()
            if (player.isPlayingAd) {
                builder.putLong(MediaMetadataCompat.METADATA_KEY_ADVERTISEMENT, 1)
            }
            builder.putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                if (player.isCurrentMediaItemDynamic || player.duration == C.TIME_UNSET) -1 else player.duration
            )
            val activeQueueItemId: Long =
                mediaControllerCompat?.playbackState?.activeQueueItemId ?: -1
            if (activeQueueItemId != MediaSessionCompat.QueueItem.UNKNOWN_ID.toLong()) {
                val queue: List<MediaSessionCompat.QueueItem>? = mediaControllerCompat?.queue
                var i = 0
                while (queue != null && i < queue.size) {
                    val queueItem = queue[i]
                    if (queueItem.queueId == activeQueueItemId) {
                        val description = queueItem.description
                        val extras = description.extras
                        if (extras != null) {
                            for (key in extras.keySet()) {
                                when (val value = extras.get(key)) {
                                    is String -> {
                                        builder.putString("" + key, value)
                                    }

                                    is CharSequence -> {
                                        builder.putText("" + key, value as CharSequence?)
                                    }

                                    is Long -> {
                                        builder.putLong("" + key, value)
                                    }

                                    is Int -> {
                                        builder.putLong("" + key, value.toLong())
                                    }

                                    is Bitmap -> {
                                        builder.putBitmap("" + key, value)
                                    }

                                    is RatingCompat -> {
                                        builder.putRating("" + key, value)
                                    }
                                }
                            }
                        }
                        val title = description.title
                        if (title != null) {
                            val titleString = title.toString()
                            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, titleString)
                            builder.putString(
                                MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
                                titleString
                            )
                        }
                        val subtitle = description.subtitle
                        if (subtitle != null) {
                            builder.putString(
                                MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
                                subtitle.toString()
                            )
                        }
                        val displayDescription = description.description
                        if (displayDescription != null) {
                            builder.putString(
                                MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                                displayDescription.toString()
                            )
                        }
                        val iconBitmap = description.iconBitmap
                        if (iconBitmap != null) {
                            builder.putBitmap(
                                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
                                iconBitmap
                            )
                        }
                        val iconUri = description.iconUri
                        if (iconUri != null) {
                            builder.putString(
                                MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                                iconUri.toString()
                            )
                        }
                        val mediaId = description.mediaId
                        if (mediaId != null) {
                            builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                        }
                        val mediaUri = description.mediaUri
                        if (mediaUri != null) {
                            builder.putString(
                                MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri.toString()
                            )
                        }
                        break
                    }
                    i++
                }
            }
            return builder.build()
        }

        /**
         * TODO
         * 添加播放器返回该mediaSession的状态回调，以便mediaSession回调给ui
         */
        fun invalidateMediaSessionPlaybackState() {
            val player = exoPlayer ?: return
            val sessionPlaybackState =
                if (player.playerError != null)
                    PlaybackStateCompat.STATE_ERROR
                else
                    getMediaSessionPlaybackState(player.playbackState, player.playWhenReady)
            val playbackSpeed =
                if (player.isPlaying) {
                    player.playbackParameters.speed
                } else {
                    0f
                }
            val state = PlaybackStateCompat.Builder()
                .setState(
                    sessionPlaybackState,
                    player.currentPosition,
                    playbackSpeed,
                    SystemClock.elapsedRealtime()
                ).build()
            mediaSession?.setPlaybackState(state)
        }


        override fun onPlayerError(error: PlaybackException) {
            Log.e("PlayerEventListener", "onPlayerError")
            var message = "一般错误"
            //Log.e(TAG, "Player error: " + error.errorCodeName + " (" + error.errorCode + ")");
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
                || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            ) {
                message = "内容未找到"
            }
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getMediaSessionPlaybackState(
        @Player.State exoPlayerPlaybackState: Int,
        playWhenReady: Boolean
    ): Int {
        return when (exoPlayerPlaybackState) {
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackStateCompat.STATE_BUFFERING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackStateCompat.STATE_NONE
            else -> PlaybackStateCompat.STATE_NONE
        }
    }
}