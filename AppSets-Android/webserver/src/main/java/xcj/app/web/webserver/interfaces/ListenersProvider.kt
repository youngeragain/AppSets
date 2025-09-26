package xcj.app.web.webserver.interfaces

interface ListenersProvider {
    fun provideSendProgressListener(): ProgressListener?
    fun provideReceiveProgressListener(): ProgressListener?
    fun provideContentReceivedListener(): ContentReceivedListener?
}