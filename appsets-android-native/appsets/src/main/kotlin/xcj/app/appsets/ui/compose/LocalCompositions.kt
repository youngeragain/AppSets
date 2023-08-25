package xcj.app.appsets.ui.compose

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.staticCompositionLocalOf

val LocalBackPressedDispatcher =
    staticCompositionLocalOf<OnBackPressedDispatcher> { error("No Back Dispatcher provided") }


//提供所有需要登录拦截页面名
val LocalPageRouteNameNeedLoggedProvider = staticCompositionLocalOf<MutableList<String>> {
    mutableListOf(
        PageRouteNameProvider.ConversationOverviewPage,
        PageRouteNameProvider.ConversationDetailsPage,
        PageRouteNameProvider.GroupConversationPage,
        PageRouteNameProvider.UserProfilePage,
        PageRouteNameProvider.AddScreenPostPage,
        PageRouteNameProvider.CreateAppPage,
    )
}