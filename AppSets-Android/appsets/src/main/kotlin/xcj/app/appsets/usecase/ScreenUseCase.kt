package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.model.ScreenInfoForCard
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.server.requestNotNull
import xcj.app.starter.server.requestNotNullRaw

class ScreenUseCase(
    private val screenRepository: ScreenRepository
) : IComposeLifecycleAware {

    companion object {
        private const val TAG = "ScreenUseCase"
    }

    class ScreensContainer {
        var uid: String? = null
        var page: Int = 1
        var pageSize: Int = 15
        var lastScreensSize: Int = -1
        val isRequesting: MutableState<Boolean> = mutableStateOf(false)
        val screens: MutableList<ScreenInfo> = mutableStateListOf()
    }

    val systemScreensContainer: ScreensContainer = ScreensContainer()

    val userScreensContainer: ScreensContainer = ScreensContainer()

    val currentScreenInfoForCard: MutableState<ScreenInfoForCard> =
        mutableStateOf(ScreenInfoForCard())

    /**
     * 辅助信息
     */
    private var currentDestination: String? = null

    suspend fun loadOutSideScreens() {
        PurpleLogger.current.d(TAG, "loadOutSideScreens")
        loadMore(null, true)
    }

    private suspend fun requestScreens(container: ScreensContainer) {
        PurpleLogger.current.d(
            TAG,
            "requestScreens, container uid:${container.uid}"
        )

        if (container.isRequesting.value) {
            PurpleLogger.current.d(TAG, "requestScreens, requesting is true, break")
            return
        }
        if (container.page > 1 && container.lastScreensSize < systemScreensContainer.pageSize) {
            PurpleLogger.current.d(
                TAG,
                "requestScreens, lastScreensSize not equals, break"
            )
            return
        }

        container.isRequesting.value = true
        requestNotNull(
            action = {
                val uid = container.uid
                if (uid.isNullOrEmpty()) {
                    screenRepository
                        .getIndexRecommendScreens(container.page, container.pageSize)
                } else {
                    screenRepository
                        .getScreensByUid(uid, container.page, container.pageSize)
                }
            },
            onSuccess = { userScreenInfoList ->
                PurpleLogger.current.d(TAG, "requestScreens, onSuccess")
                container.lastScreensSize = userScreenInfoList.size
                if (userScreenInfoList.isNotEmpty()) {
                    if (container.page == 1) {
                        container.screens.clear()
                        container.screens.addAll(userScreenInfoList)
                    } else {
                        container.screens.addAll(userScreenInfoList)
                    }
                } else {
                    container.page -= 1
                }
                container.isRequesting.value = false
            },
            onFailed = {
                PurpleLogger.current.e(TAG, "requestScreens, onFailed:${it}")
                container.isRequesting.value = false
                container.page -= 1
            }
        )
    }

    suspend fun loadMore(uid: String? = null, force: Boolean = true) {
        PurpleLogger.current.d(TAG, "loadMore")
        val container = if (uid.isNullOrEmpty()) {
            systemScreensContainer
        } else {
            userScreensContainer.uid = uid
            userScreensContainer
        }
        if (force) {
            container.page = 1
        } else {
            container.page += 1
        }

        requestScreens(container)
    }

    suspend fun updateCurrentViewScreen(currentDestination: String?, screenInfo: ScreenInfo?) {
        this.currentDestination = currentDestination
        if (screenInfo == null) {
            currentScreenInfoForCard.value = ScreenInfoForCard()
            return
        }
        currentScreenInfoForCard.value = ScreenInfoForCard(
            screenInfo = screenInfo
        )

        val screenId = screenInfo.screenId ?: return

        requestNotNullRaw(
            action = {
                runCatching {
                    val response = screenRepository.getScreenReviews(screenId)
                    val reviews = response.data
                    PurpleLogger.current.d(
                        TAG,
                        "updateCurrentViewScreen, screenReviews:$reviews"
                    )
                    if (!reviews.isNullOrEmpty()) {
                        currentScreenInfoForCard.value =
                            currentScreenInfoForCard.value.copy(
                                reviews = reviews
                            )
                    }
                }
                runCatching {
                    screenRepository.screenViewedByUser(screenId)
                }
                runCatching {
                    val response = screenRepository.getScreenViewCount(screenId)
                    val viewCount = response.data ?: 0
                    PurpleLogger.current.d(
                        TAG,
                        "updateCurrentViewScreen, screenViewCount:$viewCount"
                    )
                    currentScreenInfoForCard.value = currentScreenInfoForCard.value.copy(
                        viewCount = viewCount
                    )
                }
                runCatching {
                    val response = screenRepository.getScreenLikedCount(screenId)
                    val likedCount = response.data ?: 0
                    PurpleLogger.current.d(
                        TAG,
                        "updateCurrentViewScreen, screenLikedCount:${likedCount}"
                    )

                    currentScreenInfoForCard.value = currentScreenInfoForCard.value.copy(
                        likedCount = likedCount
                    )
                }

                runCatching {
                    val response =
                        screenRepository.screenIsCollectByUser(screenId)
                    val isCollectedByUser = response.data == true
                    PurpleLogger.current.d(
                        TAG,
                        "screenIsCollectByUser, screen is collect by user:${isCollectedByUser}"
                    )
                    currentScreenInfoForCard.value = currentScreenInfoForCard.value.copy(
                        isCollectedByUser = isCollectedByUser
                    )
                }
            },
            onFailed = {
                PurpleLogger.current.d(
                    TAG,
                    "updateCurrentViewScreen, failed:${it}"
                )
            }
        )
    }

    suspend fun userClickLikeScreen(context: Context) {
        if (!LocalAccountManager.isLogged()) {
            context.getString(xcj.app.appsets.R.string.login_required).toast()
            return
        }
        val viewScreenInfo = currentScreenInfoForCard.value
        val screenId: String = viewScreenInfo.screenInfo?.screenId ?: return
        requestNotNull(
            action = {
                screenRepository.screenLikeItByUser(screenId)
            },
            onSuccess = {
                if (it) {
                    currentScreenInfoForCard.value = viewScreenInfo.copy(
                        likedCount = viewScreenInfo.likedCount + 1
                    )
                }
            }
        )
    }


    suspend fun userClickCollectScreen(context: Context, category: String?) {
        if (!LocalAccountManager.isLogged()) {
            context.getString(xcj.app.appsets.R.string.login_required).toast()
            return
        }
        val viewScreenInfo = currentScreenInfoForCard.value
        val screenId: String = viewScreenInfo.screenInfo?.screenId ?: return
        requestNotNullRaw(
            action = {
                if (viewScreenInfo.isCollectedByUser) {
                    screenRepository.removeCollectedScreen(screenId)
                } else {
                    screenRepository.screenCollectByUser(screenId, category)
                }

            },
            onSuccess = { response ->
                val isSuccess = response.data == true
                PurpleLogger.current.d(
                    TAG,
                    "userClickCollectScreen, isSuccess:$isSuccess"
                )
                if (response.data != true) {
                    response.info.toastSuspend()
                    return@requestNotNullRaw
                }
                currentScreenInfoForCard.value = viewScreenInfo.copy(
                    isCollectedByUser = !viewScreenInfo.isCollectedByUser
                )
            }
        )
    }

    suspend fun changeScreenPublicState(isPublic: Boolean) {
        val viewScreenInfo = currentScreenInfoForCard.value
        val screenId = viewScreenInfo.screenInfo?.screenId ?: return
        requestNotNull(
            action = {
                screenRepository.changeScreenPublicState(screenId, isPublic)
            },
            onSuccess = { isChangeSuccess ->
                PurpleLogger.current.d(
                    TAG,
                    "changeScreenPublicState, result:$isChangeSuccess"
                )
                viewScreenInfo.screenInfo.isPublic = if (isPublic) {
                    1
                } else {
                    0
                }
                currentScreenInfoForCard.value = viewScreenInfo.copy(
                    screenInfo = viewScreenInfo.screenInfo
                )
            }
        )
    }


    private suspend fun addScreenReview(context: Context, reviewString: String?) {
        if (!LocalAccountManager.isLogged()) {
            context.getString(xcj.app.appsets.R.string.login_required).toast()
            return
        }
        val viewScreenInfo = currentScreenInfoForCard.value
        val reviewString = reviewString
        if (reviewString.isNullOrEmpty()) {
            context.getString(xcj.app.appsets.R.string.reply_is_empty).toast()
            return
        }
        val screenInfo = viewScreenInfo.screenInfo ?: return
        requestNotNull(
            action = {
                val response =
                    screenRepository.addScreenReview(
                        screenInfo.screenId!!,
                        reviewString,
                        true,
                        null
                    )
                if (response.data != true) {
                    context.getString(xcj.app.appsets.R.string.reply_failed).toastSuspend()
                    DesignResponse()
                } else {
                    screenRepository.getScreenReviews(screenInfo.screenId)
                }
            },
            onSuccess = { screenReviews ->
                currentScreenInfoForCard.value = viewScreenInfo.copy(
                    userInputReview = null,
                    reviews = screenReviews
                )
            },
            onFailed = {
                context.getString(xcj.app.appsets.R.string.reply_failed).toastSuspend()
            }
        )
    }

    suspend fun onReviewConfirm(context: Context, reviewString: String?) {
        addScreenReview(context, reviewString)
    }

    suspend fun updatePageShowPrevious() {
        val currentScreenInfo = currentScreenInfoForCard.value.screenInfo
        if (currentDestination == PageRouteNames.OutSidePage) {
            if (systemScreensContainer.screens.size < 2) {
                return
            }
            val currentScreenInfoIndex =
                systemScreensContainer.screens.indexOfFirst { it == currentScreenInfo }
            val previousScreenInfoIndex = currentScreenInfoIndex - 1
            val previousScreenInfo =
                systemScreensContainer.screens[previousScreenInfoIndex]
            updateCurrentViewScreen(currentDestination, previousScreenInfo)
        } else {
            if (userScreensContainer.screens.size < 2) {
                return
            }
            val currentScreenInfoIndex =
                userScreensContainer.screens.indexOfFirst { it == currentScreenInfo }
            val nextScreenInfoIndex = currentScreenInfoIndex - 1
            val previousScreenInfo =
                userScreensContainer.screens[nextScreenInfoIndex]
            updateCurrentViewScreen(currentDestination, previousScreenInfo)
        }
    }

    fun updatePageShowNext() {

    }

    override fun onComposeDispose(by: String?) {

    }
}