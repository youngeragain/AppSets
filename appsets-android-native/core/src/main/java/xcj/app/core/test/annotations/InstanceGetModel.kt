package xcj.app.core.test.annotations


/**
 * 从AAContext中获取类的实例对象的方式
 * @see Singleton 该类只有一个对象
 * @see NewInstance 该类每次获取都产生一个新的对象
 */
sealed interface InstanceGetModel{
    annotation class Singleton
    annotation class NewInstance
}