package xcj.app.compose_share.compose.dynamic

import android.util.Log

abstract class AbsComposeMethods : IComposeMethods {
    private val TAG = "AbsComposeMethods"
    private var loader: ComposeDynamicLoader? = null

    /**
     * 包名
     */
    private var aarName: String? = null

    override fun setAARName(aarName: String?) {
        this.aarName = aarName
    }

    override fun getAARName(): String? {
        return aarName
    }

    override fun getVersionMetadata(): VersionMetadata {
        return VersionMetadata.defaultVersionMetadata()
    }

    override fun setLoader(loader: ComposeDynamicLoader) {
        this.loader = loader
    }


    override fun onComposeDispose(by: String) {
        val statesHolder = getStatesHolder()
        Log.e(TAG, "onComposeDispose by:${by}, this:${this}")
        if (!statesHolder.reusable()) {
            statesHolder.onDestroy()
            loader = null
        } else {
            statesHolder.onTempDestroy()
        }
    }
}