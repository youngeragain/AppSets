package xcj.app.starter.android.service

import android.app.Service
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.TrafficStats
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlin.concurrent.thread
import kotlin.math.abs

class NetStateService : Service() {
    private lateinit var netStateBinder: NetStateBinder
    private var start:Boolean = false
    private var paused:Boolean = false
    private lateinit var netSpeedCheckRunnable: Runnable
    private var checkInterval:Long = 1000
    fun newNetSpeedCheckRunnable():Runnable = object :Runnable{
        private val lock = 0
        private val allBytes:Long = 0L
        private var lastBytes:Long = 0L
        override fun run() {
            do {
                if(netStateBinder.isListenersEmpty())
                    continue
                if(paused)
                    continue
                val uidRxBytes = TrafficStats.getUidTxBytes(applicationInfo.uid)
                val netSpeedMap = if(uidRxBytes==TrafficStats.UNSUPPORTED.toLong()){
                    mapOf(application.applicationInfo to "0KB/s")
                }else{
                    val l = abs((uidRxBytes - lastBytes) / checkInterval * 1024)
                    mapOf(application.applicationInfo to "${l}KB/s")
                }
                netStateBinder.onNetSpeed(netSpeedMap)
                lastBytes = uidRxBytes
                Thread.sleep(checkInterval)
            }while (start)
        }
    }
    override fun onBind(intent: Intent?): IBinder {
        return netStateBinder
    }
    override fun onCreate() {
        super.onCreate()
        netStateBinder = NetStateBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }
    fun start(){
        start = true
        paused = false
        thread(block = netSpeedCheckRunnable as Function0<Unit>)
    }
}

sealed class NetState(val isAvailable: Boolean) {
    class WIFI(available: Boolean) : NetState(available)
    class FourGen(available: Boolean) : NetState(available)
    class ForthGenPlus(available: Boolean) : NetState(available)
    class FifthGen(available: Boolean) : NetState(available)
}

interface OnNetStateCallback{
    fun onStateChanged():NetState
    fun onSpeedCalculated(netSpeedMap:Map<ApplicationInfo,String>)
    fun getLifecycleOwner():LifecycleOwner
}

class NetStateBinder: Binder() {
    private val netStateListeners:MutableList<OnNetStateCallback> = mutableListOf()
    fun isListenersEmpty(): Boolean = netStateListeners.isEmpty()
    fun addNetStateCallback(onNetStateCallback: OnNetStateCallback){
        netStateListeners.add(onNetStateCallback)
    }
    fun removeNetStateCallback(netStateCallback: OnNetStateCallback){
        netStateListeners.remove(netStateCallback)
    }
    fun onNetSpeed(netSpeedMap:Map<ApplicationInfo, String>){
        netStateListeners.forEach {
            val lifecycleOwner = it.getLifecycleOwner()
            if(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)&&
                    lifecycleOwner.lifecycle.currentState!=Lifecycle.State.DESTROYED){
                it.onSpeedCalculated(netSpeedMap)
            }
        }
    }
}