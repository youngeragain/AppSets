package xcj.app.web.webserver.netty

object DefaultExceptionProvider {
    fun provideException(message: String? = null): Exception {
        return Exception(message)
    }
}