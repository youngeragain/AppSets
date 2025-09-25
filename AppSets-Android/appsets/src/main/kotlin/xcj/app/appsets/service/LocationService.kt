package xcj.app.appsets.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ExecutorService

class LocationService : Service() {
    private var binder: LocationBinder? = null

    private var locationManager: LocationManager? = null


    private var executors: ExecutorService? = null

    private var locationUpdateListener: LocationListener? = null

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        val locationManager = locationManager ?: return
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE//高精度
        criteria.isAltitudeRequired = false//不要求海拔
        criteria.isBearingRequired = false//不要求方位
        criteria.isCostAllowed = true//允许有花费
        criteria.powerRequirement = Criteria.POWER_LOW//低功耗
        val provider =
            locationManager.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER
        var location: Location? = null
        var times = 0
        do {
            if (times > 4) {
                break
            }
            // 调用getLastKnownLocation()方法获取当前的位置信息
            location = locationManager.getLastKnownLocation(provider)
            times++
        } while (location == null)

        if (location == null) {
            val locationUpdateListener = LocationListener {
                binder?.geoLocation?.value = it
            }
            this.locationUpdateListener = locationUpdateListener
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                0.5F,
                locationUpdateListener
            )
        } else {
            binder?.geoLocation?.value = location
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        val locationManager = locationManager
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                executors?.execute(::getLastLocation)
            }
        }
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        binder = LocationBinder(this)
        initLocationManager()
    }

    fun removeLocationUpdateListener() {
        val locationUpdateListener = locationUpdateListener
        if (locationUpdateListener != null) {
            locationManager?.removeUpdates(locationUpdateListener)
        }
    }

    @SuppressLint("MissingPermission")
    fun initLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as? LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    class LocationBinder(
        private val locationService: LocationService
    ) : Binder() {
        val geoLocation: MutableStateFlow<Location?> = MutableStateFlow(null)
    }
}