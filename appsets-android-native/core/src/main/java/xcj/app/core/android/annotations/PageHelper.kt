package xcj.app.core.android.annotations

import kotlin.reflect.KClass

annotation class PageHelper(
    val viewModelKClass: KClass<*>,
    val viewModelFactoryKClass:KClass<*>,
    val viewDataBindingClass:KClass<*>,
    val pageRouteName:String)