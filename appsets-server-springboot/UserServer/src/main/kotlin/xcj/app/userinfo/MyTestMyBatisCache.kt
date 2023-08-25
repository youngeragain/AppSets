package xcj.app.userinfo

import org.apache.ibatis.builder.InitializingObject
import org.apache.ibatis.cache.Cache

/**
 * MyBatis 自定义缓存， 在mapper.xml的<cache> 节点设置type为org.apache.ibatis.cache.Cache的实现类
 *
 *
 * 从版本 3.4.2 开始，MyBatis 已经支持在所有属性设置完毕之后，调用一个初始化方法。 如果想要使用这个特性，请在你的自定义缓存类里实现
 *
 *
 * cache-ref
 * 回想一下上一节的内容，对某一命名空间的语句，只会使用该命名空间的缓存进行缓存或刷新。 但你可能会想要在多个命名空间中共享相同的缓存配置和实例。要实现这种需求，你可以使用 cache-ref 元素来引用另一个缓存。
 */
class MyTestMyBatisCache: Cache, InitializingObject {
    override fun getId(): String {
        return "id"
    }

    override fun putObject(key: Any?, value: Any?) {

    }

    override fun getObject(key: Any?): Any? {
        return null
    }

    override fun removeObject(key: Any?): Any? {
        return null
    }

    override fun clear() {

    }

    override fun getSize(): Int {
        return 0
    }

    override fun initialize() {

    }

}