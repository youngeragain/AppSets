package xcj.app.userinfo.service

import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mysql.UserFollowersDao
import xcj.app.userinfo.model.res.UserInfoRes

@Service
class SimpleFollowerServiceImpl(
    private val userFollowersDao: UserFollowersDao,
    private val tokenHelper: TokenHelper
):FollowerService{
    override fun flipFollowToUserState(token: String, uid: String): DesignResponse<Boolean> {
        val currentUid = tokenHelper.getUidByToken(token)
        val userHasFollower = userFollowersDao.userHasFollower(uid, currentUid)
        return if(userHasFollower){
            val resultCount = userFollowersDao.deleteFollower(uid, currentUid)
            DesignResponse(data = resultCount==1)
        }else{
            val resultCount = userFollowersDao.addFollowers(uid, listOf(currentUid))
            DesignResponse(data = resultCount==1)
        }
    }

    override fun getFollowersByUser(uid: String): DesignResponse<List<UserInfoRes>?> {
        val userInfoList = userFollowersDao.getFollowersByUserId(uid)
        return DesignResponse(data = userInfoList)
    }


    override fun getIsFollowedToUser(token: String, uid: String): DesignResponse<Boolean> {
        val currentUid = tokenHelper.getUidByToken(token)
        val userHasFollower = userFollowersDao.userHasFollower(uid, currentUid)
        return DesignResponse(data = userHasFollower)
    }

    override fun getFollowedUsersByUser(uid: String): DesignResponse<List<UserInfoRes>?> {
        val userInfoList = userFollowersDao.getFollowedUsersByUser(uid)
        return DesignResponse(data = userInfoList)
    }

    override fun getFollowersAndFollowedByUser(uid: String): DesignResponse<Map<String, List<UserInfoRes>?>> {
        val followers = userFollowersDao.getFollowersByUserId(uid)
        val followed = userFollowersDao.getFollowedUsersByUser(uid)
        val data = mapOf("followers" to followers, "followed" to followed)
        return DesignResponse(data = data)
    }
}