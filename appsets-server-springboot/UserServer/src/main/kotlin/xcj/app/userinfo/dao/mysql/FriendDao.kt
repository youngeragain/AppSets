package xcj.app.userinfo.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.userinfo.model.res.UserInfoRes
import xcj.app.userinfo.model.table.mysql.Friend

@Mapper
interface FriendDao {
    fun getAllFriends():List<Friend>
    fun getAllFriendsUserInfo():List<UserInfoRes>
    fun getFriendsPaged(page:Int, pageSize:Int):List<Friend>
    fun addFriend(uid:String, friendUid:String):Int
    fun addFriends(uid:String, friendUids:List<String>):Int
    fun deleteFriends(uid:String, friendUids:List<String>):Int
    fun deleteFriend(uid:String, friendUid:String):Int
    fun getFriend(friendUids:String):Friend?
    fun getFriendsByFriendId(friendUids:List<String>): List<Friend>?
    fun getFriendUidsByUid(uid: String):List<String>?
    fun isShipExist(uid: String, friendUid: String):Boolean
}