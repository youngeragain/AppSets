package xcj.app.main.dao.mongo

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import xcj.app.main.model.table.mongo.AppToken

@Component
class AppTokenDaoImpl(
    private val mongoTemplate: MongoTemplate
) : AppTokenDao {
    override fun getAppTokenByKeySecret(appSetsAppId: String): AppToken? {
        return mongoTemplate.findOne(
            Query.query(Criteria.where("appSetsAppId").`is`(appSetsAppId)),//.and("secret").`is`(secret)
            AppToken::class.java, "AppToken"
        )
    }

    override fun addToken(appToken: AppToken): AppToken? {
        return mongoTemplate.insert(appToken)
    }
}

