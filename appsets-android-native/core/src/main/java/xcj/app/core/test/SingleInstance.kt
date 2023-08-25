package xcj.app.core.test

interface SingleInstance {
    companion object {
        private var namesOfInstance: MutableList<SingleInstance>? = null
        fun addInstance(singleInstance: SingleInstance) {
            if (namesOfInstance == null)
                namesOfInstance = mutableListOf()
            namesOfInstance!!.add(singleInstance)
        }

        fun removeInstanceByClass(instanceClass: Class<SingleInstance>) {
            namesOfInstance?.removeIf {
                instanceClass == it::class.java
            }
        }
    }
}

abstract class AbsSingleInstance : SingleInstance {
    init {
        //SingleInstance.addInstance(this)
    }
}