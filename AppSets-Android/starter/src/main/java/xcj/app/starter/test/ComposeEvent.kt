package xcj.app.starter.test

import xcj.app.starter.foundation.DesignEvent

class ComposeEvent(
    val eventType: Int,
    val eventParams: ComposeEventParams
) : DesignEvent {
    companion object {
        const val EVENT_NAVI_HOST_FORMED = 0
    }
}

interface ComposeEventParams

/**
 * @param navController
 * @see androidx.navigation.NavHostController
 * @see androidx.navigation.NavController
 *
 * @param navGraphBuilder
 * @see androidx.navigation.NavGraphBuilder
 */
class NaviHostParams(
    val navController: Any,
    val navGraphBuilder: Any
) : ComposeEventParams