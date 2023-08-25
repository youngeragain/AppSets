package xcj.app.annotation

import xcj.app.interf.Interceptor
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Interceptors(val interceptors:Array<KClass<Interceptor>>)
