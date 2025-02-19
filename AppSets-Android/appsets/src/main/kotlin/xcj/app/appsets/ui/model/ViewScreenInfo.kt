package xcj.app.appsets.ui.model

import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.server.model.ScreenInfo

data class ViewScreenInfo(
    val screenInfo: ScreenInfo? = null,
    val reviews: List<ScreenReview>? = null,
    val userInputReview: String? = null,
    val viewCount: Int = 0,
    val likedCount: Int = 0,
    val isCollectedByUser: Boolean = false
)