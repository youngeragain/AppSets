package xcj.app.proxy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.ParcelFileDescriptor
import android.util.Pair
import android.widget.Toast
import xcj.app.proxy.ui.compose.vpn.AppSetsVpnActivity
import xcj.app.starter.android.util.PurpleLogger
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class AppSetsVpnService : VpnService(), Handler.Callback {
    companion object {
        private const val TAG: String = "AppSetsVpnService"

        const val ACTION_CONNECT = "com.example.android.toyvpn.START"

        const val ACTION_DISCONNECT = "com.example.android.toyvpn.STOP"

        const val NOTIFICATION_CHANNEL_ID = "AppSets VPN"
    }

    private var mHandler: Handler? = null

    private class Connection(thread: Thread, pfd: ParcelFileDescriptor) :
        Pair<Thread?, ParcelFileDescriptor?>(thread, pfd)

    private val mConnectingThread = AtomicReference<Thread?>()

    private val mConnection: AtomicReference<Connection?> = AtomicReference<Connection?>()

    private val mNextConnectionId = AtomicInteger(1)

    private var mConfigureIntent: PendingIntent? = null

    override fun onCreate() {
        // The handler is only used to show messages.
        if (mHandler == null) {
            mHandler = Handler(Looper.getMainLooper(), this)
        }

        // Create the intent to "configure" the connection (just start ToyVpnClient).
        mConfigureIntent = PendingIntent.getActivity(
            this, 0, Intent(
                this,
                AppSetsVpnActivity::class.java
            ),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && ACTION_DISCONNECT == intent.action) {
            disconnect()
            return Service.START_NOT_STICKY
        } else {
            connect()
            return Service.START_STICKY
        }
    }

    override fun onDestroy() {
        disconnect()
    }

    override fun handleMessage(message: Message): Boolean {
        Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show()
        if (message.what != xcj.app.proxy.R.string.disconnected) {
            updateForegroundNotification(message.what)
        }
        return true
    }

    private fun connect() {
        // Become a foreground service. Background services can be VPN services too, but they can
        // be killed by background check before getting a chance to receive onRevoke().
        updateForegroundNotification(xcj.app.proxy.R.string.connecting)
        mHandler?.sendEmptyMessage(xcj.app.proxy.R.string.connecting)

        // Extract information from the shared preferences.
        val prefs: SharedPreferences =
            getSharedPreferences(AppSetsVpnActivity.Prefs.NAME, Context.MODE_PRIVATE)
        val server = prefs.getString(AppSetsVpnActivity.Prefs.SERVER_ADDRESS, "") ?: ""
        val secret = prefs.getString(AppSetsVpnActivity.Prefs.SHARED_SECRET, "")?.toByteArray()
            ?: byteArrayOf()
        val allow = prefs.getBoolean(AppSetsVpnActivity.Prefs.ALLOW, true)
        val packages =
            prefs.getStringSet(AppSetsVpnActivity.Prefs.PACKAGES, emptySet<String>()) ?: emptySet()
        val port = prefs.getInt(AppSetsVpnActivity.Prefs.SERVER_PORT, 0)
        val proxyHost = prefs.getString(AppSetsVpnActivity.Prefs.PROXY_HOSTNAME, "")
        val proxyPort = prefs.getInt(AppSetsVpnActivity.Prefs.PROXY_PORT, 0)
        startConnection(
            AppSetsVpnConnection(
                this, mNextConnectionId.getAndIncrement(), server, port, secret,
                proxyHost, proxyPort, allow, packages
            )
        )
    }

    private fun startConnection(connection: AppSetsVpnConnection) {
        // Replace any existing connecting thread with the  new one.
        val thread: Thread = Thread(connection, "ToyVpnThread")
        setConnectingThread(thread)

        // Handler to mark as connected once onEstablish is called.
        connection.setConfigureIntent(mConfigureIntent)
        connection.setOnEstablishListener { tunInterface ->
            mHandler?.sendEmptyMessage(xcj.app.proxy.R.string.connected)
            mConnectingThread.compareAndSet(thread, null)
            setConnection(Connection(thread, tunInterface))
        }
        thread.start()
    }

    private fun setConnectingThread(thread: Thread?) {
        val oldThread = mConnectingThread.getAndSet(thread)
        oldThread?.interrupt()
    }

    private fun setConnection(connection: Connection?) {
        val oldConnection: Connection? = mConnection.getAndSet(connection)
        if (oldConnection != null) {
            try {
                oldConnection.first?.interrupt()
                oldConnection.second?.close()
            } catch (e: IOException) {
                PurpleLogger.current.e(TAG, "Closing VPN interface", e)
            }
        }
    }

    private fun disconnect() {
        mHandler?.sendEmptyMessage(xcj.app.proxy.R.string.disconnected)
        setConnectingThread(null)
        setConnection(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updateForegroundNotification(message: Int) {
        val mNotificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
            startForeground(
                1, Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(xcj.app.compose_share.R.drawable.ic_vpn)
                    .setContentText(getString(message))
                    .setContentIntent(mConfigureIntent)
                    .build()
            )
        }
    }
}