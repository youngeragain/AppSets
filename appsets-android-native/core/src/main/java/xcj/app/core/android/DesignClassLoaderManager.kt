package xcj.app.core.android

class DesignClassLoaderManager{

    //所有ClassLoader,一个ClassLoader可以加载一组或一个类，一个类是一组类的子集或特例
    private val designClassLoaders:MutableMap<DesignClassLoader, MutableList<String>> = mutableMapOf()
    //所有类名对应的类加载器
    private val designClassLoadersOfName:MutableMap<String, DesignClassLoader> = mutableMapOf()

    /**
     * @param flag 用于标记类名所属的类组，flag为类标记类的组
     */
    fun loadClass(name: String, flag:String?=null):Class<*>{
        var findClassLoader: DesignClassLoader? = null
        if(designClassLoadersOfName.containsKey(name)){
            findClassLoader = designClassLoadersOfName[name]
        }
        if(findClassLoader!=null){
            return findClassLoader.loadClass(name)
        }
        val designClassLoader = DesignClassLoader(flag)
        designClassLoaders[designClassLoader] = mutableListOf()
        designClassLoadersOfName[name] = designClassLoader
        return designClassLoader.loadClass(name)
    }

    //当某个类A没有对象实例存在内存中时，将加载这个类对象A的ClassLoader回收后，
    //类A就被卸载了
    fun unloadClass(name:String){
        makeSureAllClassInstanceGc(name)
        val designClassLoader = designClassLoadersOfName[name]
        designClassLoaders.remove(designClassLoader)
        designClassLoadersOfName.remove(name)
        gcIfNeeded()
    }

    private fun gcIfNeeded() {
        System.gc()
    }

    private fun makeSureAllClassInstanceGc(name: String) {

    }
}