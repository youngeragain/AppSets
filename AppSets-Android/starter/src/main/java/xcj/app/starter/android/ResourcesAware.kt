package xcj.app.starter.android

import android.content.res.Resources

interface ResourcesAware {
    fun setResources(resources: Resources)
    fun replaceResources(resources: Resources)
    fun getResources():Resources
}