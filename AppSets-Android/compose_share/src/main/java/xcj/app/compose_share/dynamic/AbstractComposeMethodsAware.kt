package xcj.app.compose_share.dynamic

import xcj.app.starter.android.util.PurpleLogger

abstract class AbstractComposeMethodsAware : IComposeMethodsAware {

    companion object {
        private const val TAG = "AbstractComposeMethods"
    }

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

    override fun onComposeDispose(by: String?) {
        val statesHolder = getStatesHolder()
        PurpleLogger.current.d(TAG, "onComposeDispose by:${by}")
        if (statesHolder.reusable()) {
            statesHolder.onTempDestroy()
            return
        }
        statesHolder.onDestroy()
        loader = null
    }
}