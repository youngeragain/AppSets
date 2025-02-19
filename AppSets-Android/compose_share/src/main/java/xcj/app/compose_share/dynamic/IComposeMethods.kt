package xcj.app.compose_share.dynamic

import android.content.Context
import androidx.compose.ui.platform.ComposeView

/**
 * ComposeDynamicLoader加载的Class需要实现接口IComposeMethods以便找到content方法返回的ComposeView
 * @see ComposeDynamicLoader
 * @see ComposeView
 */
interface IComposeMethods : IComposeDispose {

    fun setAARName(aarName: String?)

    fun getAARName(): String?

    fun getStatesHolder(): StatesHolder

    fun getVersionMetadata(): VersionMetadata

    fun setLoader(loader: ComposeDynamicLoader)

    fun content(context: Context): ComposeView

}

