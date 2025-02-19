package xcj.app.starter.android

import android.content.res.Resources

/**
 * 加载特定类，以实现插件化, 要卸载某个类，那么必须最后将该类加载到内存中的ClassLoader回收后才能被卸载
 * 最直接的方法就是一个类一个ClassLoader
 * 优化1:一组类用一个ClassLoader
 */
class DesignClassLoader(val flag: String? = null) : ClassLoader(), ResourcesAware {

    lateinit var mResources: Resources

    override fun setResources(resources: Resources) {
        mResources = resources
    }

    override fun replaceResources(resources: Resources) {
        setResources(resources)
    }

    override fun getResources(): Resources {
        return mResources
    }


}

