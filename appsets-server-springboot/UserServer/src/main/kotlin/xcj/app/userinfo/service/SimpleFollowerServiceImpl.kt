package xcj.app.userinfo.service

import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mysql.UserFollowersDao

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
}