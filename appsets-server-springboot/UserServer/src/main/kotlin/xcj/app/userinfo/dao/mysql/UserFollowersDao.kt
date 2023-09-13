package xcj.app.userinfo.dao.mysql

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import xcj.app.userinfo.model.res.UserInfoRes

@Mapper
interface UserFollowersDao {
    fun addFollowers(uid:String, followerUids:List<String>):Int
    fun deleteFollower(uid:String, followerUid:String):Int

    @Select("select count(1) from user_followers_2022 where uid=#{uid} and follower_uid=#{followerUid} limit 1")
    fun userHasFollower(uid: String, followerUid: String):Boolean
    fun getFollowersUidByUserId(uid: String):List<String>?

    fun getFollowersByUserId(uid: String):List<UserInfoRes>?
    fun getFollowedUsersByUser(uid: String): List<UserInfoRes>?
}