package xcj.app.userinfo.service.mongo


import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.AddAppParams
import xcj.app.userinfo.model.req.UpdateAppParams
import xcj.app.userinfo.model.res.AppsWithCategory
import xcj.app.userinfo.model.table.mongo.Application


interface AppService {

    fun getAllApplications():DesignResponse<List<Application>>
    fun getAllApplicationsPaged(page:Int, pageSize:Int):DesignResponse<List<Application>>
    fun addApplicationByDeveloper(token: String, addAppParams: AddAppParams): DesignResponse<Boolean>
    fun updateApplicationByDeveloper(token: String, updateAppParams: UpdateAppParams): DesignResponse<Boolean>

    fun getIndexApplications():DesignResponse<List<AppsWithCategory>?>
    fun createApplicationByUser(token: String, createAppParams: Map<String, Any?>): DesignResponse<Boolean>
    fun createApplicationPreCheckByUser(token: String, appName: String): DesignResponse<Boolean>

    fun searchApplicationsByKeywords(keywords: String, page: Int?, pageSize:Int?): DesignResponse<List<Application>?>
}



