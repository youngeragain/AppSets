package xcj.app.web.webserver.base

data class DataProgressInfo(
    var uuid: String,
    var name: String?,
    var total: Long = 0,
    var current: Long = 0,
    var chunkCount: Int = 0,
    var chunkIndex: Int = 0
) {
    var percentage: Float = percentageInternal

    val percentageInternal: Float
        get() = if (total <= 0) {
            0.0f
        } else {
            (current / total.toFloat()) * 100
        }

}