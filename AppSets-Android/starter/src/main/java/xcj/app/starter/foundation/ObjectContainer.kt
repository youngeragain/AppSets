package xcj.app.starter.foundation

interface ObjectContainer {
    fun <T : Any> removeObject(key: String): T?
    fun <T : Any> putObject(key: String, obj: T)
    fun <T : Any> getObjectOrNull(key: String): T?

    companion object {
        fun <T : Any> removeObject(objectContainer: ObjectContainer, key: String): T? {
            return objectContainer.removeObject<T>(key)
        }

        fun <T : Any> putObject(objectContainer: ObjectContainer, key: String, obj: T) {
            objectContainer.putObject(key, obj)
        }

        fun <T : Any> getObjectOrNull(objectContainer: ObjectContainer, key: String): T? {
            return objectContainer.getObjectOrNull<T>(key)
        }
    }
}

class DefaultObjectContainerImpl : ObjectContainer {
    private val container: MutableMap<String, Any> = mutableMapOf()

    override fun <T : Any> removeObject(key: String): T? {
        if (container.contains(key)) {
            return container.remove(key) as? T
        }
        return null
    }

    override fun <T : Any> putObject(key: String, obj: T) {
        container.put(key, obj)
    }

    override fun <T : Any> getObjectOrNull(key: String): T? {
        return container[key] as? T
    }

}