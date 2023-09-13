package xcj.app.userinfo.service.mongo

import com.google.gson.Gson
import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mongo.ApplicationDao
import xcj.app.userinfo.model.req.AddAppParams
import xcj.app.userinfo.model.req.UpdateAppParams
import xcj.app.userinfo.model.res.AppsWithCategory
import xcj.app.userinfo.model.table.mongo.Application

@Service
class AppServiceImpl(
    private val mongoApplicationDaoImpl: ApplicationDao,
    private val tokenHelper: TokenHelper
    ):AppService{
    override fun getAllApplications(): DesignResponse<List<Application>> {
        val allApps = mongoApplicationDaoImpl.getAllApplications()
        return DesignResponse(data = allApps)
    }

    override fun getAllApplicationsPaged(page: Int, pageSize: Int): DesignResponse<List<Application>> {
        val allAppsPaged = mongoApplicationDaoImpl.getAllApplicationsPaged(page, pageSize)
        return DesignResponse(data = allAppsPaged)
    }

    override fun addApplicationByDeveloper(token: String, addAppParams: AddAppParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        return DesignResponse(data = false)
    }

    override fun updateApplicationByDeveloper(token: String, updateAppParams: UpdateAppParams): DesignResponse<Boolean> {
        return DesignResponse(data = false)
    }

    override fun getIndexApplications(): DesignResponse<List<AppsWithCategory>?>{
        val categories = ApplicationCategory.allAsList()
        val indexApplications = mutableListOf<AppsWithCategory>()
        categories.forEach { applicationCategory->
            val indexAppsByCategory = mongoApplicationDaoImpl.getApplicationsByCategoryPaged(applicationCategory, 1, 5)
            if(!indexAppsByCategory.isNullOrEmpty())
                indexApplications.add(AppsWithCategory(applicationCategory.name, applicationCategory.nameZh, indexAppsByCategory))
        }
        return DesignResponse(data = indexApplications)
    }

    override fun createApplicationByUser(token: String, createAppParams: Map<String, Any?>): DesignResponse<Boolean> {
        val toJson = Gson().toJson(createAppParams)
        println("createAppByUser:\n${toJson}")
        val addResult = mongoApplicationDaoImpl.addApplicationObject(createAppParams)
        return DesignResponse(data = addResult)
    }

    override fun createApplicationPreCheckByUser(token: String, appName: String): DesignResponse<Boolean> {
        val hasAppNameExist = mongoApplicationDaoImpl.hasAppNameExist(appName)
        return DesignResponse(data = !hasAppNameExist)
    }

    override fun searchApplicationsByKeywords(keywords: String, page: Int?, pageSize:Int?): DesignResponse<List<Application>?> {
        val limit = pageSize?:20
        val offset = ((page?:1)-1)*limit
        val applicationList = mongoApplicationDaoImpl.searchApplicationsByKeywords(keywords, limit, offset)
        return DesignResponse(data = applicationList)
    }

    override fun getUsersApplications(uid: String): DesignResponse<List<Application>?> {
        val applicationList = mongoApplicationDaoImpl.getApplicationByUserId(uid)
        return DesignResponse(data = applicationList)
    }
}

sealed class ApplicationCategory(val name:String, val nameZh:String){
    object Social:ApplicationCategory("social", "社交")
    object Tool:ApplicationCategory("tool", "工具")
    object Game:ApplicationCategory("game", "游戏")
    object Media:ApplicationCategory("media", "媒体")
    object Music:ApplicationCategory("music", "音乐")
    object Video:ApplicationCategory("video", "视频")
    object Money:ApplicationCategory("money", "金融")
    object Camera:ApplicationCategory("camera", "相机")
    object Live:ApplicationCategory("live", "直播")
    object Shopping:ApplicationCategory("shopping", "购物")
    object Health:ApplicationCategory("health", "健康")
    object Others:ApplicationCategory("others", "其它")

    companion object{
        fun allAsList(): List<ApplicationCategory> {
            return listOf(Social, Tool, Game, Media, Music, Video,
                Money, Camera, Live, Shopping, Health, Others)
        }
    }
}