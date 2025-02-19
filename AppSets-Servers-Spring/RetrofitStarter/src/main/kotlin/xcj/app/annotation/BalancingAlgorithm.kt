package xcj.app.annotation

import xcj.app.interf.BalancedAble
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BalancingAlgorithm(val AlgorithmClass:KClass<BalancedAble<*, *>>)
