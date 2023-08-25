package xcj.app.core.android

import android.content.res.Resources

interface ResourcesAware {
    fun setResources(resources: Resources)
    fun replaceResources(resources: Resources)
    fun getResources():Resources
}