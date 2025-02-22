package xcj.app.web.webserver.netty

object DefaultExceptionProvider {
    @JvmStatic
    fun provideException(message: String? = null): Exception {
        return Exception(message)
    }
}