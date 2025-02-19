package xcj.app.appsets.settings

data class AppConfiguration(
    val canSignUp: Boolean,
    val apiSchema: String,
    val apiHost: String,
    val apiPort: Int,
    val apiUrl: String,
    val appsetsAppId: String,
    val imBrokerProperties: String
)

object AppConfig {
    private var updateTime: Long = 0
    private var updating = false

    var isTest: Boolean = true

    var appConfiguration: AppConfiguration = createAppConfiguration()

    private fun createAppConfiguration(): AppConfiguration {
        return AppConfiguration(
            canSignUp = true,
            apiSchema = "https",
            apiHost = "8.137.93.144",
            apiPort = 3401,
            apiUrl = "",
            appsetsAppId = "APPSETS2023071579019880338529",
            imBrokerProperties = ""
        )
        return if (isTest) {
            AppConfiguration(
                canSignUp = true,
                apiSchema = "https",
                apiHost = "127.0.0.1",
                apiPort = 8084,
                apiUrl = "",
                appsetsAppId = "APPSETS2023071579019880338529",
                imBrokerProperties = ""
            )
        } else {
            AppConfiguration(
                canSignUp = true,
                apiSchema = "https",
                apiHost = "8.137.93.144",
                apiPort = 3401,
                apiUrl = "",
                appsetsAppId = "APPSETS2023071579019880338529",
                imBrokerProperties = ""
            )

        }
    }

    fun isNeedUpdateImBrokerProperties(): Boolean {
        return appConfiguration.imBrokerProperties.isEmpty()
    }

    fun updateImBrokerProperties(properties: String) {
        appConfiguration = appConfiguration.copy(imBrokerProperties = properties)
    }

    fun update() {
        if (updating) {
            return
        }
    }
}