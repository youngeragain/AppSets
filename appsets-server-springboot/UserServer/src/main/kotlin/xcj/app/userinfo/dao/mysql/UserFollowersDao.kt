package xcj.app.userinfo.dao.mysql

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface UserFollowersDao {
    fun addFollowers(uid:String, followerUids:List<String>):Int
    fun deleteFollower(uid:String, followerUid:String):Int

    @Select("select count(1) from user_followers_2022 where uid=#{uid} and follower_uid=#{followerUid}")
    fun userHasFollower(uid: String, followerUid: String):Boolean
}