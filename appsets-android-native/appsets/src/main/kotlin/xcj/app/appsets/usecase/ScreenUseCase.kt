package xcj.app.appsets.usecase

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ktx.request
import xcj.app.appsets.ktx.requestNotNull
import xcj.app.appsets.ktx.requestNotNullRaw
import xcj.app.appsets.ktx.requestRaw
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.ktx.toastSuspend
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.server.model.ScreenState
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.core.foundation.http.DesignResponse

class ScreenUseCase(private val coroutineScope: CoroutineScope) {


    class ScreensContainer {
        var uid: String? = null
        var page: Int = 1
        var pageSize: Int = 15
        var lastScreensSize: Int = -1
        val requestingState: MutableState<Boolean> = mutableStateOf(false)
        val screensState: MutableList<ScreenState> = mutableStateListOf()
    }

    private val screenRepository by lazy {
        ScreenRepository(URLApi.provide(UserApi::class.java))
    }

    val systemScreensContainer: ScreensContainer = ScreensContainer()
    var userScreensContainer: ScreensContainer? = null

    fun loadScreensByUid(uid: String) {
        if (userScreensContainer == null) {
            userScreensContainer = ScreensContainer()
        }
        if (userScreensContainer!!.uid != uid) {
            userScreensContainer!!.uid = uid
            userScreensContainer!!.screensState.clear()
        }
        if (userScreensContainer!!.requestingState.value)
            return
        userScreensContainer!!.page = 1
        userScreensContainer!!.requestingState.value = true
        requestScreens(userScreensContainer!!)
    }

    fun removeUserContainerIfNeeded() {
        userScreensContainer = null
    }

    fun loadIndexScreens(force: Boolean) {
        if (systemScreensContainer.requestingState.value)
            return
        if (!force) {
            if (systemScreensContainer.screensState.isNotEmpty())
                return
        }
        systemScreensContainer.page = 1
        systemScreensContainer.requestingState.value = true
        requestScreens(systemScreensContainer)
    }

    private fun requestScreens(container: ScreensContainer) {
        coroutineScope.requestNotNull({
            Log.e("ScreenUseCase", "ScreenUseCase:container uid:${container.uid}")
            if (!container.uid.isNullOrEmpty()) {
                screenRepository
                    .getScreensByUid(container.uid!!, container.page, container.pageSize)
            } else {
                screenRepository
                    .getIndexRecommendScreens(container.page, container.pageSize)
            }
        }, onSuccess = { userScreenInfoList ->
            container.lastScreensSize = userScreenInfoList.size
            if (container.screensState.isNotEmpty()) {
                val lastIndex = container.screensState.size - 1
                if (container.screensState[lastIndex] is ScreenState.NoMore) {
                    container.screensState.removeAt(lastIndex)
                }
            }
            if (userScreenInfoList.isNotEmpty()) {
                if (container.page == 1)
                    container.screensState.clear()
                userScreenInfoList.map { userScreen -> ScreenState.Screen(userScreen) }.also {
                    container.screensState.addAll(it)
                }
                if (userScreenInfoList.size < container.pageSize) {
                    container.screensState.add(ScreenState.NoMore)
                }
            } else {
                container.screensState.add(ScreenState.NoMore)
                container.page -= 1
            }
            delay(1000)
            container.requestingState.value = false
        }, onFailed = {
            container.requestingState.value = false
            container.page -= 1
        })
    }

    fun loadMore() {
        val container = userScreensContainer ?: systemScreensContainer
        if (container.lastScreensSize < systemScreensContainer.pageSize) {
            Log.e(
                "ScreenUseCase",
                "Load more, lastScreensSize not equals, break"
            )
            return
        }
        if (container.requestingState.value) {
            Log.e("ScreenUseCase", "Load more, requesting is true, break")
            return
        }
        Log.e("ScreenUseCase", "Load more")
        container.page += 1
        container.requestingState.value = true
        requestScreens(container)
    }

    val currentViewScreenState: MutableState<UserScreenInfo?> = mutableStateOf(null)
    val currentViewScreenReviews: MutableList<ScreenReview> = mutableStateListOf()
    val currentViewScreenUserReviewState: MutableState<String?> = mutableStateOf(null)
    val currentViewScreenViewCount: MutableState<Int> = mutableStateOf(0)
    val currentViewScreenLikedCount: MutableState<Int> = mutableStateOf(0)
    val currentViewScreenIsCollectByUser: MutableState<Boolean> = mutableStateOf(false)
    fun updateCurrentViewScreen(userScreenInfo: UserScreenInfo?) {
        if (userScreenInfo == null) {
            currentViewScreenState.value = null
            currentViewScreenReviews.clear()
            currentViewScreenUserReviewState.value = null
            return
        }

        currentViewScreenState.value = userScreenInfo
        if (!LocalAccountManager.isLogged()) {
            return
        }
        if (currentViewScreenReviews.isNotEmpty())
            currentViewScreenReviews.clear()
        if (!currentViewScreenUserReviewState.value.isNullOrEmpty())
            currentViewScreenUserReviewState.value = null
        currentViewScreenViewCount.value = 0
        currentViewScreenLikedCount.value = 0
        currentViewScreenIsCollectByUser.value = false
        val screenId = currentViewScreenState.value?.screenId ?: return
        coroutineScope.requestNotNullRaw(
            requestAction = {
                kotlin.runCatching {
                    val screenReviewsRes = screenRepository.getScreenReviews(screenId)
                    Log.e(
                        "ScreenUseCase",
                        "updateCurrentViewScreen, screenReviews:${screenReviewsRes.data}"
                    )
                    if (!screenReviewsRes.data.isNullOrEmpty())
                        currentViewScreenReviews.addAll(screenReviewsRes.data!!)
                }
                async {
                    kotlin.runCatching {
                        screenRepository.screenViewedByUser(screenId)
                    }
                }
                async {
                    kotlin.runCatching {
                        val screenViewCountRes = screenRepository.getScreenViewCount(screenId)
                        Log.e(
                            "ScreenUseCase",
                            "updateCurrentViewScreen, screenViewCount:${screenViewCountRes.data}"
                        )
                        currentViewScreenViewCount.value = screenViewCountRes.data ?: 0
                    }
                }
                async {
                    kotlin.runCatching {
                        val screenLikedCountRes = screenRepository.getScreenLikedCount(screenId)
                        Log.e(
                            "ScreenUseCase",
                            "updateCurrentViewScreen, screenLikedCount:${screenLikedCountRes.data}"
                        )
                        currentViewScreenLikedCount.value = screenLikedCountRes.data ?: 0
                    }
                }

                async {
                    kotlin.runCatching {
                        val screenIsCollectByUserRes = screenRepository.screenIsCollectByUser(screenId)
                        Log.e(
                            "ScreenUseCase",
                            "screenIsCollectByUser, screen is collect by user:${screenIsCollectByUserRes.data}"
                        )
                        currentViewScreenIsCollectByUser.value = screenIsCollectByUserRes.data == true
                    }
                }

            },
            onFailed = {
                Log.e("ScreenUseCase", "updateCurrentViewScreen, failed:${it}")
            })
    }

    fun userClickLikeScreen() {
        if (!LocalAccountManager.isLogged()) {
            "需要登录".toast()
            return
        }
        val screenInfo: UserScreenInfo = currentViewScreenState.value ?: return
        coroutineScope.requestNotNull({
            screenRepository.screenLikeItByUser(screenInfo.screenId!!)
        }, onSuccess = {
            if (it)
                currentViewScreenLikedCount.value = currentViewScreenLikedCount.value + 1
        })
    }


    fun userClickCollectScreen(category: String?) {
        if (!LocalAccountManager.isLogged()) {
            "需要登录".toast()
            return
        }
        val screenInfo: UserScreenInfo = currentViewScreenState.value ?: return
        if (currentViewScreenIsCollectByUser.value) {
            coroutineScope.requestNotNullRaw({
                screenRepository.removeCollectedScreen(screenInfo.screenId!!)
            }, onSuccess = {
                if (it.data == true) {
                    currentViewScreenIsCollectByUser.value = true
                } else {
                    it.info.toastSuspend()
                }
            })
        } else {
            coroutineScope.requestRaw({
                val submitCategory = if (category.isNullOrEmpty()) {
                    null
                } else {
                    category
                }
                screenRepository.screenCollectByUser(screenInfo.screenId!!, submitCategory)
            }, onSuccess = {
                Log.e(
                    "ScreenUseCase",
                    "userClickCollectScreen, screen is collected by user:${it?.data}"
                )
                if (it?.data == true)
                    currentViewScreenIsCollectByUser.value = it.data == true
                else
                    it?.info?.toastSuspend()
            })
        }
    }

    fun changeScreenPublicState(isPublic: Boolean) {
        val screenInfo = currentViewScreenState.value ?: return
        coroutineScope.requestRaw({
            screenRepository.changeScreenPublicState(screenInfo.screenId!!, isPublic)
        }, onSuccess = {
            Log.e("ScreenUseCase", "changeScreenPublicState, result:${it?.data}")
            if (it?.data == true) {
                currentViewScreenState.value?.isPublic = if (isPublic) {
                    1
                } else {
                    0
                }
            } else {
                it?.info?.toastSuspend()
            }
        })
    }


    fun addScreenReview() {
        if (!LocalAccountManager.isLogged()) {
            "需要登录".toast()
            return
        }
        val reviewString = currentViewScreenUserReviewState.value
        if (reviewString.isNullOrEmpty()) {
            "回复为空".toast()
            return
        }
        val screenInfo = currentViewScreenState.value ?: return
        coroutineScope.request({
            val addResultRes =
                screenRepository.addScreenReview(screenInfo.screenId!!, reviewString, true, null)
            if (addResultRes.data != true) {
                "回复失败".toastSuspend()
                DesignResponse()
            } else {
                currentViewScreenUserReviewState.value = ""
                screenRepository.getScreenReviews(screenInfo.screenId)
            }
        }, onSuccess = { screenReviews ->
            if (currentViewScreenReviews.isNotEmpty())
                currentViewScreenReviews.clear()
            if (!screenReviews.isNullOrEmpty())
                currentViewScreenReviews.addAll(screenReviews)
        }, onFailed = {
            "回复失败".toastSuspend()
        })
    }
}