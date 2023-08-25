package xcj.app.userinfo.model.table.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("user")
data class User(
    @Id val id: ObjectId?,
    val name:String,
    val age:Int,
    val sex:String)