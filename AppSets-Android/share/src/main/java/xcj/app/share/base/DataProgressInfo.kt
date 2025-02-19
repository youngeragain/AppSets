package xcj.app.share.base

data class DataProgressInfo(
    var uuid: String,
    var name: String?,
    var total: Long = 0,
    var current: Long = 0,
    var percentage: Double = 0.00,
    var chunkCount: Int = 0,
    var chunkIndex: Int = 0
)