package xcj.app.main.model.table.mysql

import java.io.Serializable

data class UserScreen(
    var screenId: String?,
    var screenContent: String?,
    var uid: String?,
    var associateTopics: String?,
    var associateUsers: String?,
    var isPublic: Int?,
    var systemReviewResult: Int?
) : Serializable {
    constructor() : this(
        null, null, null,
        null, null, null, null
    )
}
