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
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocationService : Service() {
    private lateinit var binder: PBinder

    private var locationManager: LocationManager? = null

    private var lastLocation: Location? = null

    private val executors: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val locationUpdateListener: LocationListener by lazy {
        LocationListener {
            lastLocation = it
            binder.geoPair.postValue(Pair(it.latitude.toFloat(), it.longitude.toFloat()))
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        val locationManager = locationManager ?: return
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE//高精度
        criteria.isAltitudeRequired = false//不要求海拔
        criteria.isBearingRequired = false//不要求方位
        criteria.isCostAllowed = true//允许有花费
        criteria.powerRequirement = Criteria.POWER_LOW//低功耗
        var provider =
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


        lastLocation = location

        if (location == null) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                0.5F,
                locationUpdateListener
            )
        } else {
            binder.geoPair.postValue(
                Pair(
                    location.latitude.toFloat(),
                    location.longitude.toFloat()
                )
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) != true) {
            binder.geoPair.value = Pair(0.0f, 0.0f)
        } else {
            //getLastLocation and if null, register a new LocationUpdateListener to LocationManager
            executors.execute(::getLastLocation)
        }
        return binder
    }

    fun createObserve() {
        /*contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED),
            false,
            object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    val enabled: Boolean = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)?:false
                    if(enabled){
                        Logger.e("", "gps on")
                        executors.execute(runnable)
                    }else{
                        Logger.e("", "gps off")
                    }
                }
            }
        )*/
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
        binder = PBinder(this)
        createObserve()
        initLocationManager()
    }

    fun removeLocationUpdateListener() {
        locationManager?.removeUpdates(locationUpdateListener)
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

    class PBinder(
        val locationService: LocationService?
    ) : Binder() {
        val geoPair: MutableLiveData<Pair<Float, Float>> by lazy { MutableLiveData() }
    }
}