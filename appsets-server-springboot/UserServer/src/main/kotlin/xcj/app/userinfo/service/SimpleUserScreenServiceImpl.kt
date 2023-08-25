package xcj.app.userinfo.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sun.misc.Unsafe
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.userinfo.Helpers
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mysql.*
import xcj.app.userinfo.model.req.AddScreenReviewParams
import xcj.app.userinfo.model.req.AddUserScreenParams
import xcj.app.userinfo.model.req.AdminReviewScreenParams
import xcj.app.userinfo.model.req.DeleteUserScreenParams
import xcj.app.userinfo.model.res.UserScreenInfoRes
import xcj.app.userinfo.model.table.mysql.ScreenReview
import xcj.app.userinfo.model.table.mysql.UserScreen
import xcj.app.userinfo.util.ContentCheckChainHolder

@Service
class SimpleUserScreenServiceImpl(
    private val tokenHelper: TokenHelper,
    private val userScreenDao: UserScreenDao,
    private val screenReviewDao: ScreenReviewDao,
    private val screenMediaFileUrlDao: ScreenMediaFileUrlDao,
    private val reviewDao: UserScreenSystemReviewDao,
    private val redisTemplate: StringRedisTemplate,
    private val userScreenCollectDao: UserScreenCollectDao,
    private val contentCheckChainHolder: ContentCheckChainHolder
):UserScreenService {

    private val stringIntHashOps = redisTemplate.opsForHash<String, String>()
    init {
       /* redisTemplate.hashValueSerializer = object : RedisSerializer<Int>{
            override fun serialize(t: Int?): ByteArray? {
                return t.toString().toByteArray()
            }

            override fun deserialize(bytes: ByteArray?): Int? {
                return bytes?.let { String(it).toInt() } ?: 0
            }
        }*/
    }

    fun getUserScreenListByUid(
        uid: String,
        checkReviewResult:Boolean,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<List<UserScreenInfoRes>> {
        val limit = (pageSize?:10)
        val offset = ((page?:1)-1)*limit
        val screenPageListByUidPaged = userScreenDao.getScreenPageListByUidPaged(uid, checkReviewResult, true, limit, offset)
        return DesignResponse(data = screenPageListByUidPaged)
    }

    override fun getUserScreenListByUid(
        uid: String,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<List<UserScreenInfoRes>> {
        return getUserScreenListByUid(uid, true, page, pageSize)
    }

    override fun getUserScreenListByToken(
        token: String,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<List<UserScreenInfoRes>> {
        val uid = tokenHelper.getUidByToken(token)
        return getUserScreenListByUid(uid, false, page, pageSize)
    }

    override fun getIndexUserScreens(page: Int?, pageSize: Int?): DesignResponse<List<UserScreenInfoRes>> {
        val limit = (pageSize?:10)
        val offset = ((page?:1)-1)*limit
        val indexRandomUserScreen = userScreenDao.getIndexRandomUserScreen(orderByTime = true, checkReviewResult = false, limit, offset)
        return DesignResponse(data = indexRandomUserScreen)
    }

    @Transactional
    override fun addUserScreen(token: String, addUseScreenParams: AddUserScreenParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        val screenId = Helpers.generateScreenId()
        val contentCheckChain = contentCheckChainHolder.getContentCheckChain()
        val tempSystemReviewResult = if(addUseScreenParams.screenContent.isNullOrEmpty()){
            1
        }else{
            val proceedResult = contentCheckChain.proceed<String, String>(addUseScreenParams.screenContent)
            if(proceedResult.isNullOrEmpty()||proceedResult!=addUseScreenParams.screenContent)
                0
            else{
                1
            }
        }
        val tempAssociateTopics = if(addUseScreenParams.associateTopics.isNullOrEmpty()){
            null
        }else{
            "#"+addUseScreenParams.associateTopics?.replace(",", "#,#")+"#"
        }
        val tempAssociateUsers = if(addUseScreenParams.associateUsers.isNullOrEmpty()){
            null
        }else{
            "^"+addUseScreenParams.associateUsers?.replace(",", "^,^")+"^"
        }
        val addScreenResult = userScreenDao.addScreen(
            UserScreen(
                screenId,
                addUseScreenParams.screenContent,
                uid,
                tempAssociateTopics,
                tempAssociateUsers,
                if(addUseScreenParams.isPublic==true){1}else{0},
                tempSystemReviewResult
            )
        )
        return if(addScreenResult==1){
            if(addUseScreenParams.mediaFileUrls.isNullOrEmpty()){
                DesignResponse(data = true)
            }else{
                addUseScreenParams.mediaFileUrls.forEach {
                    it.x18Content = if(it.mediaDescription.isNullOrEmpty()){
                        0
                    }else{
                        val proceedResult = contentCheckChain.proceed<String, String>(it.mediaDescription)
                        if(proceedResult.isNullOrEmpty()||proceedResult!=it.mediaDescription){
                            1
                        }else{
                            0
                        }
                    }
                }
                val addScreenMediaFileUrlsResult =
                    screenMediaFileUrlDao.addScreenMediaFileUrls(screenId, addUseScreenParams.mediaFileUrls)
                if(addScreenMediaFileUrlsResult==addUseScreenParams.mediaFileUrls.size)
                    DesignResponse(data = true)
                else{
                    DesignResponse(data = false)
                }
            }
        }else{
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, data = false)
        }
    }

    override fun deleteUserScreen(
        token: String,
        deleteUserScreenParams: DeleteUserScreenParams
    ): DesignResponse<Boolean> {
        val deleteScreenResult = userScreenDao.deleteScreen(deleteUserScreenParams.screenId)
        return if(deleteScreenResult==1)
            DesignResponse(info ="Delete screen successful!", data = true)
        else
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, info ="Delete screen failed!", data = false)
    }

    override fun deleteUserScreenLogic(
        token: String,
        deleteUserScreenParams: DeleteUserScreenParams
    ): DesignResponse<Boolean> {
        return DesignResponse.somethingWentWrong()
    }

    override fun reviewScreenByAdmin(token: String, adminReviewScreenParams: AdminReviewScreenParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        if(adminReviewScreenParams.reviewResult==1){
            val updateAdminReviewResultResult = userScreenDao.updateAdminReviewResult(
                adminReviewScreenParams.screenId,
                adminReviewScreenParams.reviewResult
            )
            if(updateAdminReviewResultResult!=1){
                return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Your admin review failed!", false)
            }
        }
        val addAdminReviewResult = reviewDao.addAdminReview(
            uid,
            adminReviewScreenParams.screenId,
            adminReviewScreenParams.reviewResult,
            adminReviewScreenParams.reviewMessage
        )
        return if(addAdminReviewResult==1)
            DesignResponse(info = "Your admin review successful!", data = true)
        else{
            DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, info = "Your admin review failed!", data = false)
        }
    }

    /**
     * 更改Screen的可见性
     * screen必须是token对应用户创建的
     */
    override fun flipScreenVisibility(token: String, screenId: String): DesignResponse<Boolean> {
        val uidByToken = tokenHelper.getUidByToken(token)
        val screen = userScreenDao.getScreenByScreenId(screenId)
            ?: throw Exception("Screen not found when flip screen visibility!")
        if(screen.uid!=uidByToken)
            throw Exception("Screen's user doest not match when flip screen visibility!")
        if(screen.isPublic==1){
            screen.isPublic = 0
            val count = userScreenDao.updateScreenPublicStatus(screen)
            if(count!=1)
                return DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, info = "Screen flip visibility failed!", data = false)
            else
                return  DesignResponse(info = "Screen flip visibility success!", data = true)
        }else{
            return if(screen.systemReviewResult==1){
                DesignResponse(info = "Screen flip visibility success!", data = true)
            }else{
                DesignResponse(info = "Screen flip visibility success! but your screen need to be verify!", data = true)
            }
        }
    }

    override fun searchScreenByKeywords(
        token: String,
        keywords: String,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<List<UserScreenInfoRes>> {
        val limit = pageSize?:20
        val offset = ((page?:1)-1)*limit
        val screenInfoResList = userScreenDao.searchScreenByKeywords(keywords, limit, offset)
        return DesignResponse(data = screenInfoResList)
    }

    override fun getScreenReviewsByScreenId(screenId: String, page: Int?, pageSize: Int?): DesignResponse<List<ScreenReview>> {
        val limit = pageSize?:30
        val offset = ((page?:1)-1)*limit
        val screenReviews = screenReviewDao.getScreenReviewsByScreenId(screenId, true, true, limit, offset)
        return DesignResponse(data = screenReviews)
    }

    override fun addScreenReviews(
        token: String,
        addScreenReviewParams: AddScreenReviewParams
    ): DesignResponse<Boolean> {
        val reviewUid = tokenHelper.getUidByToken(token)
        val reviewId = Helpers.generateReviewId()
        val reviewPassed = 1
        val result = screenReviewDao.addScreenReview(
            reviewId,
            addScreenReviewParams.content,
            addScreenReviewParams.screenReviewId,
            reviewUid,
            addScreenReviewParams.screenId,
            reviewPassed,
            if(addScreenReviewParams.isPublic){1}else{0})
        if(result==1)
            return DesignResponse(data = true)
        return DesignResponse(data = false)
    }

    override fun screenViewedByUser(token: String, screenId: String): DesignResponse<Boolean> {
        if(!redisTemplate.hasKey("ScreenViewedByUser")){
            stringIntHashOps.put("ScreenViewedByUser", screenId, "1")
            return DesignResponse(data = true)
        }
        val viewCount = stringIntHashOps.get("ScreenViewedByUser", screenId)?.toIntOrNull()?:0
        stringIntHashOps.put("ScreenViewedByUser", screenId, (viewCount + 1).toString())
        return DesignResponse(data = true)
    }

    override fun getScreenViewCount(screenId: String): DesignResponse<Int> {
        if(!redisTemplate.hasKey("ScreenViewedByUser"))
            return DesignResponse(data = 0)
        val viewCount = stringIntHashOps.get("ScreenViewedByUser", screenId)?.toIntOrNull()?:0
        return DesignResponse(data = viewCount)
    }

    override fun screenLikeItByUser(screenId: String, count: Int): DesignResponse<Boolean> {
        if(!redisTemplate.hasKey("ScreenLikeItByUser")){
            stringIntHashOps.put("ScreenLikeItByUser", screenId, "1")
            return DesignResponse(data = true)
        }
        val likeCount = stringIntHashOps.get("ScreenLikeItByUser", screenId)?.toIntOrNull()?:0
        stringIntHashOps.put("ScreenLikeItByUser", screenId, (likeCount + count).toString())
        return DesignResponse(data = true)
    }

    override fun getScreenLikedCount(screenId: String): DesignResponse<Int> {
        if(!redisTemplate.hasKey("ScreenLikeItByUser"))
            return DesignResponse(data = 0)
        val likeCount = stringIntHashOps.get("ScreenLikeItByUser", screenId)?.toIntOrNull()?:0
        return DesignResponse(data = likeCount)
    }

    override fun screenCollectByUser(token: String, screenId: String, category: String?): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        val userScreen = userScreenDao.getScreenByScreenId(screenId)
        if(userScreen?.uid==uid){
            return DesignResponse(data = false, info = "This Screen is your own!")
        }
        val collected = userScreenCollectDao.isUserScreenCollectByUser(uid, screenId) == 1
        if(collected)
            return DesignResponse(data = false, info = "This Screen is already collected by this user!")
        val addCount = userScreenCollectDao.addUserScreenCollect(uid, screenId, category)
        return DesignResponse(data = addCount == 1)
    }

    override fun removeCollectedScreen(token: String, screenId: String): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        val notCollected = userScreenCollectDao.isUserScreenCollectByUser(uid, screenId) == 0
        if(notCollected)
            return DesignResponse(data = false, info = "This Screen was not originally collected!")
        val userScreenCollect = userScreenCollectDao.getUserScreenCollectByUidAndScreenId(uid, screenId)
        if(userScreenCollect==null)
            return DesignResponse(data = false, info = "This Screen was not originally collected!")
        if(System.currentTimeMillis()- userScreenCollect.collectTime.time<=86_400_000)
            return DesignResponse(data = false, info = "Cancelling favorites takes 1 day!")
        val updateCount = userScreenCollectDao.removeCollectScreenByUidAndScreenId(uid, screenId)
        return DesignResponse(data = updateCount == 1)
    }

    override fun isScreenCollectByUser(token: String, screenId: String): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        val queryCount = userScreenCollectDao.isUserScreenCollectByUser(uid, screenId)
        return DesignResponse(data = queryCount==1)
    }

    override fun changeScreenPublicState(token: String, screenId: String, isPublic: Boolean): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        val userScreen = userScreenDao.getScreenByScreenId(screenId)
        if(userScreen?.uid!=uid){
            return DesignResponse(data = false, info = "You are not the owner of this Screen!")
        }
        if(userScreen.isPublic==1)
            return DesignResponse(data = true, info = "This Screen is already public!")
        userScreen.isPublic = if(isPublic){ 1 }else{ 0 }
        val updateCount = userScreenDao.updateScreenPublicStatus(userScreen)
        return DesignResponse(data = updateCount==1)
    }

}