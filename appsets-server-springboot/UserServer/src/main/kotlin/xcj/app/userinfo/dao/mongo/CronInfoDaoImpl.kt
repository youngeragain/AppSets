package xcj.app.userinfo.dao.mongo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import xcj.app.userinfo.model.table.mongo.CronInfo

@Component
class CronInfoDaoImpl(
    private val mongoTemplate: MongoTemplate
): CronInfoDao {


    override fun getRedisTokenScanCronInfo(): CronInfo? {
        val query = Query.query(Criteria.where("name").`is`("redis_token_scan"))

        return mongoTemplate.findOne(query, CronInfo::class.java, "CronInfo")
    }

    override fun getBaiduHotDataFetchCronInfo(): CronInfo? {
        val query = Query.query(Criteria.where("name").`is`("baidu_hot_data_fetch"))
        return mongoTemplate.findOne(query,
            CronInfo::class.java, "CronInfo")
    }

    override fun getBingWallpaperFetchCronInfo(): CronInfo? {
        val query = Query.query(Criteria.where("name").`is`("bing_wallpaper_fetch"))
        return mongoTemplate.findOne(query,
            CronInfo::class.java, "CronInfo")
    }
}