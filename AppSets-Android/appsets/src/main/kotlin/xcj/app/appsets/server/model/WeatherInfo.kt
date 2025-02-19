package xcj.app.appsets.server.model

data class TemperatureInfo(
    val time: Long,
    val temperature: Float,
    val description: String? = null
)

data class WeatherInfo(
    val temperatureInfo: List<TemperatureInfo>
)