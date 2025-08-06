package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.starter.server.requestNotNull
import xcj.app.starter.server.requestNotNullRaw
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.model.ScreenState
import xcj.app.appsets.ui.model.ViewScreenInfo
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse

class ScreenUseCase(
    private val coroutineScope: CoroutineScope,
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
        val screens: MutableList<ScreenState> = mutableStateListOf()
    }

    val systemScreensContainer: ScreensContainer = ScreensContainer()

    val userScreensContainer: ScreensContainer = ScreensContainer()

    val currentViewScreenInfo: MutableState<ViewScreenInfo> = mutableStateOf(ViewScreenInfo())

    /**
     * 辅助信息
     */
    private var currentDestination: String? = null

    fun loadOutSideScreens() {
        PurpleLogger.current.d(TAG, "loadOutSideScreens")
        loadMore(null, true)
    }

    private fun requestScreens(container: ScreensContainer) {
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
        coroutineScope.launch {
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
                        val screenStateList =
                            userScreenInfoList.map { userScreen -> ScreenState.Screen(userScreen) }
                        if (container.page == 1) {
                            container.screens.clear()
                            container.screens.addAll(screenStateList)
                        } else {
                            container.screens.addAll(screenStateList)
                        }
                    } else {
                        container.page -= 1
                    }
                    if (userScreenInfoList.size < container.pageSize) {
                        container.screens.add(ScreenState.NoMore)
                    }
                    delay(1000)
                    container.isRequesting.value = false
                },
                onFailed = {
                    PurpleLogger.current.e(TAG, "requestScreens, onFailed:${it}")
                    container.isRequesting.value = false
                    container.page -= 1
                }
            )
        }
    }

    fun loadMore(uid: String? = null, force: Boolean = true) {
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

    fun updateCurrentViewScreen(currentDestination: String?, screenInfo: ScreenInfo?) {
        this.currentDestination = currentDestination
        if (screenInfo == null) {
            currentViewScreenInfo.value = ViewScreenInfo()
            return
        }
        currentViewScreenInfo.value = ViewScreenInfo(
            screenInfo = screenInfo
        )

        val screenId = screenInfo.screenId ?: return

        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    val e = async {
                        runCatching {
                            val response = screenRepository.getScreenReviews(screenId)
                            val reviews = response.data
                            PurpleLogger.current.d(
                                TAG,
                                "updateCurrentViewScreen, screenReviews:$reviews"
                            )
                            if (!reviews.isNullOrEmpty()) {
                                currentViewScreenInfo.value = currentViewScreenInfo.value.copy(
                                    reviews = reviews
                                )
                            }
                        }
                    }
                    val a = async {
                        runCatching {
                            screenRepository.screenViewedByUser(screenId)
                        }
                    }
                    val b = async {
                        runCatching {
                            val response = screenRepository.getScreenViewCount(screenId)
                            val viewCount = response.data ?: 0
                            PurpleLogger.current.d(
                                TAG,
                                "updateCurrentViewScreen, screenViewCount:$viewCount"
                            )
                            currentViewScreenInfo.value = currentViewScreenInfo.value.copy(
                                viewCount = viewCount
                            )
                        }
                    }
                    val c = async {
                        runCatching {
                            val response = screenRepository.getScreenLikedCount(screenId)
                            val likedCount = response.data ?: 0
                            PurpleLogger.current.d(
                                TAG,
                                "updateCurrentViewScreen, screenLikedCount:${likedCount}"
                            )

                            currentViewScreenInfo.value = currentViewScreenInfo.value.copy(
                                likedCount = likedCount
                            )
                        }
                    }

                    val d = async {
                        runCatching {
                            val response =
                                screenRepository.screenIsCollectByUser(screenId)
                            val isCollectedByUser = response.data == true
                            PurpleLogger.current.d(
                                TAG,
                                "screenIsCollectByUser, screen is collect by user:${isCollectedByUser}"
                            )
                            currentViewScreenInfo.value = currentViewScreenInfo.value.copy(
                                isCollectedByUser = isCollectedByUser
                            )
                        }
                    }
                    e.await()
                    a.await()
                    b.await()
                    c.await()
                    d.await()
                },
                onFailed = {
                    PurpleLogger.current.d(
                        TAG,
                        "updateCurrentViewScreen, failed:${it}"
                    )
                }
            )
        }
    }

    fun userClickLikeScreen(context: Context) {
        if (!LocalAccountManager.isLogged()) {
            context.getString(xcj.app.appsets.R.string.login_required).toast()
            return
        }
        val viewScreenInfo = currentViewScreenInfo.value
        val screenId: String = viewScreenInfo.screenInfo?.screenId ?: return
        coroutineScope.launch {
            requestNotNull(
                action = {
                    screenRepository.screenLikeItByUser(screenId)
                },
                onSuccess = {
                    if (it) {
                        currentViewScreenInfo.value = viewScreenInfo.copy(
                            likedCount = viewScreenInfo.likedCount + 1
                        )
                    }
                }
            )
        }
    }


    fun userClickCollectScreen(context: Context, category: String?) {
        if (!LocalAccountManager.isLogged()) {
            context.getString(xcj.app.appsets.R.string.login_required).toast()
            return
        }
        val viewScreenInfo = currentViewScreenInfo.value
        val screenId: String = viewScreenInfo.screenInfo?.screenId ?: return
        coroutineScope.launch {
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
                    currentViewScreenInfo.value = viewScreenInfo.copy(
                        isCollectedByUser = !viewScreenInfo.isCollectedByUser
                    )
                }
            )
        }
    }

    fun changeScreenPublicState(isPublic: Boolean) {
        val viewScreenInfo = currentViewScreenInfo.value
        val screenId = viewScreenInfo.screenInfo?.screenId ?: return
        coroutineScope.launch {
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
                    currentViewScreenInfo.value = viewScreenInfo.copy(
                        screenInfo = viewScreenInfo.screenInfo
                    )
                }
            )
        }
    }


    private fun addScreenReview(context: Context) {
        if (!LocalAccountManager.isLogged()) {
            context.getString(xcj.app.appsets.R.string.login_required).toast()
            return
        }
        val viewScreenInfo = currentViewScreenInfo.value
        val reviewString = viewScreenInfo.userInputReview
        if (reviewString.isNullOrEmpty()) {
            context.getString(xcj.app.appsets.R.string.reply_is_empty).toast()
            return
        }
        val screenInfo = viewScreenInfo.screenInfo ?: return
        coroutineScope.launch {
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
                    currentViewScreenInfo.value = viewScreenInfo.copy(
                        userInputReview = null,
                        reviews = screenReviews
                    )
                },
                onFailed = {
                    context.getString(xcj.app.appsets.R.string.reply_failed).toastSuspend()
                }
            )
        }
    }

    fun onInputReview(review: String) {
        currentViewScreenInfo.value =
            currentViewScreenInfo.value.copy(
                userInputReview = review
            )
    }

    fun onReviewConfirm(context: Context) {
        addScreenReview(context)
    }

    fun onChangeScreenStyleClick(
        screenInfo: ScreenInfo,
        currentDestinationRoute: String
    ) {
        val replace: (ScreenState) -> ScreenState = { screenState ->
            if (screenState is ScreenState.Screen && screenState.screenInfo.screenId == screenInfo.screenId) {
                screenState.copy(screenInfo = screenInfo)
            } else {
                screenState
            }
        }
        if (currentDestinationRoute != PageRouteNames.UserProfilePage) {
            systemScreensContainer.screens.replaceAll(replace)
        } else {

            userScreensContainer.screens.replaceAll(replace)
        }
    }

    fun updatePageShowPrevious() {
        val currentScreenInfo = currentViewScreenInfo.value.screenInfo
        if (currentDestination == PageRouteNames.OutSidePage) {
            if (systemScreensContainer.screens.size < 2) {
                return
            }
            val currentScreenInfoIndex =
                systemScreensContainer.screens.indexOfFirst { it is ScreenState.Screen && it.screenInfo == currentScreenInfo }
            val previousScreenInfoIndex = currentScreenInfoIndex - 1
            val previousScreenInfo =
                (systemScreensContainer.screens[previousScreenInfoIndex] as? ScreenState.Screen)?.screenInfo
            updateCurrentViewScreen(currentDestination, previousScreenInfo)
        } else {
            if (userScreensContainer.screens.size < 2) {
                return
            }
            val currentScreenInfoIndex =
                userScreensContainer.screens.indexOfFirst { it is ScreenState.Screen && it.screenInfo == currentScreenInfo }
            val nextScreenInfoIndex = currentScreenInfoIndex - 1
            val previousScreenInfo =
                (userScreensContainer.screens[nextScreenInfoIndex] as? ScreenState.Screen)?.screenInfo
            updateCurrentViewScreen(currentDestination, previousScreenInfo)
        }
    }

    fun updatePageShowNext() {

    }

    override fun onComposeDispose(by: String?) {

    }
}