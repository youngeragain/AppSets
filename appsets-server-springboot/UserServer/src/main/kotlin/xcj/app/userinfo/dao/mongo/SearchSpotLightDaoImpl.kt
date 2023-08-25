package xcj.app.userinfo.dao.mongo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import xcj.app.userinfo.model.table.mongo.SpotLight

@Component
class SearchSpotLightDaoImpl(
    private val mongoTemplate: MongoTemplate
) :SearchSpotLightDao{
    override fun getSpotLightInfo(): SpotLight? {
        return mongoTemplate.findById(ObjectId("62cbee50f7f2517adf34bdbb"), SpotLight::class.java)
    }
}