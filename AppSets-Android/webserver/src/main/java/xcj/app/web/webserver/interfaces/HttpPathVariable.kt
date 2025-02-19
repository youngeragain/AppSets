package xcj.app.web.webserver.interfaces

/**
 * @sample https://localhost/example/{name}
 * name is HttpPathVariable
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpPathVariable(
    val name: String, val required: Boolean = false,
    val beginChar: Char = '{', val endChar: Char = '}'
)