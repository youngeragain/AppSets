package xcj.app.web.webserver.interfaces

interface ComponentsProvider {
    fun provideFileCreator(): FileCreator

    fun provideShareDirPath(): String
}