package xcj.app.appsets.ui.compose.custom_component

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import xcj.app.starter.android.util.PurpleLogger

@Navigator.Name("navigation")
class ComposeNavGraphNavigator(
    navigatorProvider: NavigatorProvider
) : NavGraphNavigator(navigatorProvider) {

    override fun createDestination(): NavGraph {
        return ComposeNavGraph(this)
    }

    internal class ComposeNavGraph(
        navGraphNavigator: Navigator<out NavGraph>
    ) : NavGraph(navGraphNavigator) {
        var enterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null

        var exitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null

        var popEnterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null

        var popExitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null
    }

}

internal class DesignNaviHostControllerWrapper(
    context: Context,
    val popBackStackCallBack: (() -> Boolean)?
) : NavHostController(context) {

    companion object {
        private const val TAG = "DesignNaviHostControllerWrapper"
    }

    override fun popBackStack(): Boolean {
        PurpleLogger.current.d(TAG, "popBackStack")
        val callback = popBackStackCallBack
        if (callback == null) {
            return super.popBackStack()
        }
        return callback() && super.popBackStack()
    }

}

@Composable
public fun rememberNavControllerX(
    popBackStackCallBack: (() -> Boolean)? = null,
    vararg navigators: Navigator<out NavDestination>
): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(
        inputs = navigators,
        saver = NavControllerSaver(context, popBackStackCallBack)
    ) {
        createNavController(context, popBackStackCallBack)
    }.apply {
        for (navigator in navigators) {
            navigatorProvider.addNavigator(navigator)
        }
    }
}

private fun createNavController(context: Context, popBackStackCallBack: (() -> Boolean)?) =
    DesignNaviHostControllerWrapper(context, popBackStackCallBack).apply {
        navigatorProvider.addNavigator(ComposeNavGraphNavigator(navigatorProvider))
        navigatorProvider.addNavigator(ComposeNavigator())
        navigatorProvider.addNavigator(DialogNavigator())
    }

/**
 * Saver to save and restore the NavController across config change and process death.
 */
private fun NavControllerSaver(
    context: Context,
    popBackStackCallBack: (() -> Boolean)?,
): Saver<NavHostController, *> = Saver<NavHostController, Bundle>(
    save = { it.saveState() },
    restore = { createNavController(context, popBackStackCallBack).apply { restoreState(it) } }
)
