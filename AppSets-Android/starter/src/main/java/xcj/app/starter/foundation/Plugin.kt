package xcj.app.starter.foundation

/**
 * @param H 是谁的插件
 */
interface Plugin<H> {
    fun isEnable(): Boolean
}