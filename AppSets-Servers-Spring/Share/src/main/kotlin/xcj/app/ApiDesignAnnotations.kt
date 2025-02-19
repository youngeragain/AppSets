package xcj.app

/**
 * 暂时是处理http请求中的header中的逻辑
 */
sealed interface ApiDesignPermission {
    /**
     * 所有需要登录后才可调用的API接口添加此注解
     * 在Spring拦截器里面实现具体判断
     * @param tokenState token状态
     * @see TOKEN_STATE_VALID_BUT_NOT_EXPIRED 有效未过期
     * @see TOKEN_STATE_VALID_HAS_EXPIRED 有效已过期
     */
    annotation class LoginRequired(
        val tokenState: Int = TOKEN_STATE_VALID_BUT_NOT_EXPIRED
    ) {
        companion object {
            const val TOKEN_STATE_VALID_BUT_NOT_EXPIRED = 0
            const val TOKEN_STATE_VALID_HAS_EXPIRED = 1
        }
    }

    /**
     * 必须为管理员或者有权限的才可以调用API
     * 等价于UserRoleRequired(andRoles=[ROLE_ADMIN])
     */
    annotation class AdministratorRequired


    /**
     * 必须为指定role的权限的才可以调用API
     */
    annotation class UserRoleRequired(val andRoles: Array<String>, val orRoles: Array<String>) {
        companion object {
            const val ROLE_ADMIN = "admin"
            const val ROLE_DEVELOPER = "developer"
        }
    }

    /**
     * @param time 前可调用API，毫秒时间戳
     */
    annotation class BeforeTimeRequired(val time: Long)

    /**
     * @param time 后可调用API，毫秒时间戳
     */
    annotation class AfterTimeRequired(val time: Long)

    /**
     * @param leastVersionCode 调用者版本的代码，至低需要调用者版本才可调用API
     */
    annotation class VersionRequired(val leastVersionCode: Int)

    /**
     * API已经被弃用
     * @param message 提示信息
     * @param alternative 代替方案
     */
    annotation class Deprecated(val message: String, val alternative: String)


    /**
     * 特定请求者才可调用
     * @param keyCode 服务端提供给调用者的加密字符串， 调用者在http请求的header里面带上合规的keyCode才能进行API调用
     */
    @Repeatable
    annotation class SpecialRequired(val keyCode: String)

    annotation class CombineRequired(
        val login: Boolean = false,
        val administrator: Boolean = false,
        val beforeTime: Long = 0L,
        val afterTime: Long = 0L,
        val versionCodeLeast: Int = 0,
        val deprecatedMessage: String = "",
        val deprecatedAlternative: String = ""
    )
}