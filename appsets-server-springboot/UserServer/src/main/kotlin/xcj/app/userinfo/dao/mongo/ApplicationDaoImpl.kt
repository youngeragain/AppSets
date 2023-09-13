package xcj.app.userinfo.dao.mongo

import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import xcj.app.userinfo.model.table.mongo.Application
import xcj.app.userinfo.service.mongo.ApplicationCategory

@Component
class ApplicationDaoImpl(private val mongoTemplate: MongoTemplate): ApplicationDao {
    override fun getAllApplications(): List<Application> {
        return mongoTemplate.findAll<Application>()
    }

    override fun getAllApplicationsPaged(page: Int, pageSize: Int): List<Application> {
        val tempPage = if(page>0){ page-1 }else{ page }
        val pageAble = PageRequest.of(tempPage, pageSize)
        return mongoTemplate.find(
            Query.query(Criteria()).with(pageAble),
            Application::class.java
        )
    }

    override fun addApplication(app: Application): Boolean {
        return try {
            mongoTemplate.insert(app)
            true
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }

    override fun addApplicationObject(app: Map<String, Any?>): Boolean {
        return try {
            mongoTemplate.insert(app, "Application")
            true
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }

    override fun updateApplication(app: Application): Boolean {
        mongoTemplate.update(Application::class.java)
            .matching(Query.query(Criteria.where("app_id").`is`(app.appId)))
            .replaceWith(app).findAndReplaceValue()
        return false
    }

    override fun getApplicationsByCategoryPaged(
        category: ApplicationCategory,
        page: Int,
        pageSize: Int
    ): List<Application>? {
        return try {
            val tempPage = if(page>0){ page-1 }else{ page }
            val pageRequest = PageRequest.of(tempPage, pageSize)
            val operator = Criteria().orOperator(
                Criteria.where("category").`is`(category.name),
                Criteria.where("category").`is`(category.nameZh)
            )
            val query = Query.query(operator).with(pageRequest)
            mongoTemplate.find(
                query,
                Application::class.java,
                "Application"
            )
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

    override fun hasAppNameExist(appName: String): Boolean {
        return try {
            mongoTemplate.exists(Query.query(Criteria.where("name").`is`(appName)),
                null, "Application")
        }catch (e:Exception){
            e.printStackTrace()
            true
        }
    }

    override fun searchApplicationsByKeywords(keywords: String, limit: Int, offset: Int): List<Application>? {
        return try {
            val query = Query.query(Criteria.where("name").regex(keywords))
            val applications = mongoTemplate.find(
                query,
                Application::class.java,
                "Application"
            )
            applications
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

    override fun getApplicationByUserId(uid: String): List<Application>? {
        return try {
            val query = Query.query(Criteria.where("create_uid").`is`(uid))
            val applications = mongoTemplate.find(
                query,
                Application::class.java,
                "Application"
            )
            applications
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }
}
