package xcj.app.appsets.settings

data class ModuleConfiguration(
    val canSignUp: Boolean,
    val apiSchema: String,
    val apiHost: String,
    val apiPort: Int,
    val apiUrl: String,
    val appsetsAppId: String,
    val imBrokerProperties: String
)

object ModuleConfig {
    private var updateTime: Long = 0
    private var updating = false

    var isTest: Boolean = false

    var moduleConfiguration: ModuleConfiguration = createConfiguration()

    private fun createConfiguration(): ModuleConfiguration {
        return if (isTest) {
            ModuleConfiguration(
                canSignUp = true,
                apiSchema = "https",
                apiHost = "127.0.0.1",
                apiPort = 8084,
                apiUrl = "",
                appsetsAppId = "APPSETS2023071579019880338529",
                imBrokerProperties = ""
            )
        } else {
            ModuleConfiguration(
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
        return moduleConfiguration.imBrokerProperties.isEmpty()
    }

    fun updateImBrokerProperties(properties: String) {
        moduleConfiguration = moduleConfiguration.copy(imBrokerProperties = properties)
    }

    fun update() {
        if (updating) {
            return
        }
    }
}