package xcj.app.main.model.table.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("MediaContent")
data class MediaContent(
    @Id
    val id: ObjectId?,
    @Field("type")
    val type: Int,
    @Field("uri")
    val uri: String,
    @Field("companionUri")
    val companionUri: String? = null,
    @Field("relateUserUid")
    val relateUserUid: String? = null,
    @Field("relateUserScreenId")
    val relateUserScreenId: String? = null,
    @Field("extraInfo")
    val extraInfo: String? = null
)