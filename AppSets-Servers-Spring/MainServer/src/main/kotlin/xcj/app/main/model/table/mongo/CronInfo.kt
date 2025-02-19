package xcj.app.main.model.table.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("CronInfo")
data class CronInfo(
    @Id val id: ObjectId?,
    val cronText: String,
    val name: String
)
