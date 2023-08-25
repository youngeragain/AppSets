package xcj.app.appsets.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.util.UnstableApi
import coil.imageLoader
import coil.request.ImageRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.BuildConfig
import xcj.app.appsets.R
import xcj.app.appsets.im.CommonURLJson
import xcj.app.appsets.ktx.request
import xcj.app.appsets.ktx.requestNotNullRaw
import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.compose.ExoPlayerActivity
import xcj.app.appsets.ui.compose.win11Snapshot.SpotLightState
import xcj.app.appsets.usecase.models.Application
import xcj.app.appsets.util.ApplicationCategory
import xcj.app.core.android.ApplicationHelper
import xcj.app.core.foundation.usecase.NoConfigUseCase
import java.util.Calendar

@UnstableApi
class AppSetsUseCase(private val coroutineScope: CoroutineScope): NoConfigUseCase() {
    val indexApplications: MutableList<AppsWithCategory> = mutableStateListOf()
    val spotLightsState: MutableList<SpotLightState> = mutableStateListOf()
    var fastFindApplicationState: MutableState<Application?> = mutableStateOf(null)
    private var packageInfo: PackageInfo? = null
    val applicationDetailsBlurDrawableState: MutableState<BitmapDrawable?> = mutableStateOf(null)

    init {
        initPackageInfo()
        initAppToken()
    }

    fun loadApplicationBlur(context: Context, application: Application) {
        val request = ImageRequest.Builder(context)
            .data("https://example.com/image.jpg")
            .build()
        val imageLoader = context.imageLoader
        coroutineScope.launch(Dispatchers.IO) {
            val drawable = imageLoader.execute(request).drawable
        }
    }

    private fun initPackageInfo() {
        val context = ApplicationHelper.application.applicationContext
        packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    }

    val appTokenInitialized: MutableLiveData<Boolean> = MutableLiveData()

    /**
     * 部分数据可以直接公开，不过也需要有访问权限
     */
    private fun initAppToken() {
        coroutineScope.request({
            Log.i("AppSetsUseCase", "getAppToken")
            AppSetsRepository.getInstance().getAppToken()
        }, onSuccess = {
            appTokenInitialized.postValue(true)
        }, onFailed = {
            Log.e("AppSetsUseCase", "getAppToken failed:${it.info}")
        })
    }

    fun loadIndexApps() {
        if (fastFindApplicationState.value != null)
            return
        coroutineScope.request({
            Log.i("blue", "AppSetsUseCase:getIndexRecommendApps")
            AppSetsRepository.getInstance().getIndexApplications()
        }, onSuccess = {
            if (it.isNullOrEmpty())
                return@request
            val applications = mutableListOf<Application>()
            it.forEach { applicationWithCategory ->
                ApplicationCategory.mapCategoryToLocale(applicationWithCategory)
                applications.addAll(applicationWithCategory.applications)
            }
            if (it.isNotEmpty())
                AppSetsRepository.mapIconUrl(applications)
            if (applications.isNotEmpty()) {
                val randomApplication = applications.random()
                fastFindApplicationState.value = randomApplication
            }
            indexApplications.addAll(it)
        })
    }


    fun loadSpotLight() {
        if (spotLightsState.isNotEmpty())
            return
        coroutineScope.requestNotNullRaw({
            Log.i("AppSetsUseCase", "getWin11SearchSpotLightInfo")
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val headerTitle =
                SpotLightState.HeaderTitle("今天 • ${month}月${dayOfMonth}日")
            spotLightsState.add(headerTitle)
            val spotLightInfoRes =
                AppSetsRepository.getInstance().getSpotLight()
            if (!spotLightInfoRes.success || spotLightInfoRes.data == null) {
                return@requestNotNullRaw
            }

            val spotLightInfo = spotLightInfoRes.data!!
            if (spotLightInfo.holiday != null) {
                val holiday = SpotLightState.Holiday(
                    spotLightInfo.holiday.picUrl,
                    spotLightInfo.holiday.name ?: "", spotLightInfo.holiday.infoUrl,
                    spotLightInfo.holiday.moreUrl
                )
                spotLightsState.add(holiday)
            }
            kotlin.runCatching {
                val image = spotLightInfo.bingWallpaperJson?.images?.getOrNull(0)
                val bingWallpaperUrl: Any? = image?.let {
                    "https://www.bing.com/${it.url?.replace("/", "")}"
                }
                val where = image?.copyright ?: "一个美好的地方"
                val whereBlowText = image?.title ?: "每日一题"
                val questionOfTheDay =
                    SpotLightState.QuestionOfTheDay(bingWallpaperUrl, where, whereBlowText)
                questionOfTheDay.onClick = ::win11SnapShotPageStateClick
                spotLightsState.add(questionOfTheDay)
            }

            if (spotLightInfo.wordOfTheDay != null && spotLightInfo.todayInHistory != null) {
                val wordOfTheDay: SpotLightState.WordOfTheDay =
                    SpotLightState.WordOfTheDay(
                        null,
                        spotLightInfo.wordOfTheDay.picUrl,
                        spotLightInfo.wordOfTheDay.word ?: "",
                        spotLightInfo.wordOfTheDay.author ?: ""
                    )
                val todayInHistory: SpotLightState.TodayInHistory =
                    SpotLightState.TodayInHistory(
                        null,
                        spotLightInfo.todayInHistory.picUrl,
                        spotLightInfo.todayInHistory.title,
                        spotLightInfo.todayInHistory.event ?: ""
                    )
                val wordOfTheDayAndTodayInHistory =
                    SpotLightState.WordOfTheDayAndTodayInHistory(
                        wordOfTheDay,
                        todayInHistory
                    )
                wordOfTheDay.onClick = ::win11SnapShotPageStateClick
                todayInHistory.onClick = ::win11SnapShotPageStateClick
                spotLightsState.add(wordOfTheDayAndTodayInHistory)
            }

            if (!spotLightInfo.popularSearches?.url.isNullOrEmpty() &&
                !spotLightInfo.popularSearches?.keywords.isNullOrEmpty()
            ) {
                val popularSearches = SpotLightState.PopularSearches(
                    R.drawable.ic_baseline_call_missed_outgoing_24,
                    "热门搜索",
                    spotLightInfo.popularSearches?.keywords?.sorted()
                        ?: emptyList()
                )
                spotLightsState.add(popularSearches)
            }
            val hotWords = spotLightInfo.baiduHotData
            if (!hotWords?.hotsearch.isNullOrEmpty()) {
                val hotWordsWrapper =
                    SpotLightState.HotWordsWrapper("百度热搜", hotWords!!.hotsearch!!)
                spotLightsState.add(hotWordsWrapper)
            }

        }, onFailed = {
            Log.e("AppSetsUseCase", "getWin11SearchSpotLightInfo, failed!${it.info}")
        })
    }


    @UnstableApi
    fun win11SnapShotPageStateClick(
        spotLightState: SpotLightState,
        context: Context,
        payload: Any?
    ) {
        context.startActivity(Intent(context, ExoPlayerActivity::class.java).apply {
            val url =
                "http://${BuildConfig.ApiHostAddress}:8000/files/Wiz Khalifa-Charlie Puth-See You Again.mp4"
            val videoJson = Gson().toJson(
                CommonURLJson.VideoURLJson(
                    url,
                    "See you again"
                )
            )
            putExtra("video_json", videoJson)
        })
    }


    val newVersionState: MutableState<UpdateCheckResult?> = mutableStateOf(null)

    private var newVersionStatePendingDismissJob: Job? = null

    fun dismissNewVersionTips() {
        newVersionState.value = null
        newVersionStatePendingDismissJob?.cancel()
        newVersionStatePendingDismissJob = null
    }

    fun checkUpdate(context: Context) {
        if (packageInfo == null)
            return
        coroutineScope.request({
            AppSetsRepository.getInstance().checkUpdate(packageInfo!!.versionCode)
        }, onSuccess = {
            if (it == null)
                return@request
            if (it.canUpdate) {
                delay(1200)
                it.versionFromTo = "${packageInfo!!.versionName} → ${it.newestVersion}"
                newVersionState.value = it
                newVersionStatePendingDismissJob = launch {
                    delay(1000 * 300)
                    newVersionState.value = null
                }
            }
        })
    }

    /**
     * 更新历史
     */
    val updateHistory: MutableList<UpdateCheckResult> = mutableStateListOf()
    fun getUpdateHistory() {
        if (updateHistory.isNotEmpty())
            return
        coroutineScope.request({
            AppSetsRepository.getInstance().getUpdateHistory()
        }, onSuccess = { updateCheckResultList ->
            if (updateCheckResultList.isNullOrEmpty())
                return@request
            delay(300)
            updateHistory.addAll(updateCheckResultList.sortedByDescending { it.publishDateTime })
        })
    }

    fun getAppSetsPackageVersionName(): String? {
        return packageInfo?.versionName
    }

    fun cleanUpdateHistory() {
        updateHistory.clear()
    }
}