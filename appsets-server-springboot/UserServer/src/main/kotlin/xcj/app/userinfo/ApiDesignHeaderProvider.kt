package xcj.app.userinfo


/**
 * 背景:filter和interceptor都没法用入参的ServletRequest或HttpServletRequest添加header
 * 此接口让一个继承HttpServletRequestWrapper的类可以添加和获取自定义header
 */
interface ApiDesignHeaderProvider {
    fun getDesignHeader(name:String):String?

    fun getDesignHeaderInt(name:String):Int?

    fun addDesignHeader(name:String, value:Any)

    fun getDesignHeaders():Map<String, Any?>
}
