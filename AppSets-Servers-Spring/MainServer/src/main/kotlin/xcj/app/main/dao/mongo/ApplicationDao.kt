package xcj.app.main.dao.mongo

import xcj.app.main.model.table.mongo.Application
import xcj.app.main.service.mongo.ApplicationCategory

interface ApplicationDao {

    fun getAllApplications():List<Application>

    fun getAllApplicationsPaged(page: Int, pageSize: Int):List<Application>

    fun addApplication(app: Application):Boolean

    fun addApplicationObject(app: Map<String, Any?>):Boolean

    fun updateApplication(app: Application):Boolean

    fun getApplicationsByCategoryPaged(category:ApplicationCategory, page: Int, pageSize: Int):List<Application>?

    fun hasAppNameExist(appName: String): Boolean

    fun searchApplicationsByKeywords(keywords: String, limit:Int, offset:Int):List<Application>?

    fun getApplicationByUserId(uid: String): List<Application>?
    
}