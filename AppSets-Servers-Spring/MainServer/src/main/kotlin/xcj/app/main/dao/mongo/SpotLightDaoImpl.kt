package xcj.app.main.dao.mongo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import xcj.app.main.model.table.mongo.SpotLight

@Component
class SpotLightDaoImpl(
    private val mongoTemplate: MongoTemplate
) : SpotLightDao {
    override fun getSpotLight(): SpotLight? {
        return mongoTemplate.findById(ObjectId("65b5cadfde53000009007328"), SpotLight::class.java)
    }
}