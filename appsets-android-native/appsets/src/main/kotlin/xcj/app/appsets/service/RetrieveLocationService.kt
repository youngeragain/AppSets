package xcj.app.appsets.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RetrieveLocationService: Service(){
    private var binder: MyBinder? = null
    private val runnable:Runnable by lazy {
        Runnable(::getLastLocation)
    }

    private var locationManager:LocationManager? = null

    // 调用getSystemService()方法来获取LocationManager对象
    var lastLocation:Location?=null
    private val providerGPS = LocationManager.GPS_PROVIDER  // 指定LocationManager的定位方法
    private val providerNetwork = LocationManager.NETWORK_PROVIDER // 指定LocationManager的定位方法
    @SuppressLint("MissingPermission")
    fun getLastLocation(){
        locationManager?.let { manager->
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE//高精度
            criteria.isAltitudeRequired = false//不要求海拔
            criteria.isBearingRequired = false//不要求方位
            criteria.isCostAllowed = true//允许有花费
            criteria.powerRequirement = Criteria.POWER_LOW//低功耗
            var provider = manager.getBestProvider(criteria, true)?:providerGPS
            val timeStart=System.currentTimeMillis()
            do {
                if(System.currentTimeMillis()-timeStart>250)
                    break
                lastLocation = manager.getLastKnownLocation(providerNetwork)
            }while (lastLocation==null)
            // 调用getLastKnownLocation()方法获取当前的位置信息
            lastLocation?.let {
                binder?.lati_longi?.postValue(Pair(it.latitude.toFloat(), it.longitude.toFloat()))
            }
            if(lastLocation==null){
                Looper.prepare()
                locationManager?.requestLocationUpdates(providerNetwork, 1000L, 0.5F, locationUpdateListener)
                Looper.loop()
            }
        }

    }
    private val executors: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    override fun onBind(intent: Intent?): IBinder? {
        binder = binder?: MyBinder(this)
        createObserve()
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
        if(locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) != true){
            binder?.lati_longi?.value = Pair(0.0f, 0.0f)
        }else{
            //getLastLocation and if null, register a new LocationUpdateListener to LocationManager
            executors.execute(runnable)
        }
        return binder
    }
    fun createObserve(){
        //lati_longi?.observe()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        binder = null
        return super.onUnbind(intent)
    }
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        initLocationManager()
    }
    private val locationUpdateListener:LocationListener by lazy { LocationListener {
            lastLocation = it
            binder?.lati_longi?.postValue(Pair(it.latitude.toFloat(), it.longitude.toFloat()))
        }
    }
    fun removeLocationUpdateListener(){
        locationManager?.removeUpdates(locationUpdateListener)
    }
    @SuppressLint("MissingPermission")
    fun initLocationManager(){
        locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    fun getLatiAndLongi():Pair<Float, Float>?{
        return binder?.lati_longi?.value
    }
    class MyBinder(
        val retrieveLocationService: RetrieveLocationService?
    ) :Binder(){
        val lati_longi:MutableLiveData<Pair<Float, Float>?>? by lazy { MutableLiveData() }
    }
}