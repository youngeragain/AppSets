package xcj.app.main.model.table.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("AppToken")
data class AppToken(
    @Id val id: ObjectId?,
    val appToken: String,
    val appSetsAppId: String
)
