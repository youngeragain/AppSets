package xcj.app.appsets.purple_module

object ModuleConstant {

    //模块名称
    const val MODULE_NAME = "MODULE_APPSETS"
    //模块数据库名称
    const val MODULE_DATABASE_NAME = "AppSetMainDatabase"

    //模块SharedPreferences默认名称
    const val MODULE_SHARED_PREFERENCES_DEFAULT = "xcj.app.sp.appsets.default"

    //当用户登录后
    const val MSG_DELIVERY_KEY_USER_LOGIN_ACTION = "USER_LOGIN_ACTION"

    //当token错误时
    const val MSG_DELIVERY_KEY_TOKEN_ERROR = "TOKEN_ERROR"

    //当选择器选择后
    const val MSG_DELIVERY_KEY_SELECTOR_ITEM_SELECTED = "SELECTOR_ITEM_SELECTED"

    //选择时的上下文环境
    var KEY_SELECTOR_ITEM_SELECTED_CONTEXT = "?"

    //当用户退出登录后
    const val MSG_DELIVERY_KEY_USER_LOGOUT_ACTION = "USER_LOGOUT_ACTION"
}