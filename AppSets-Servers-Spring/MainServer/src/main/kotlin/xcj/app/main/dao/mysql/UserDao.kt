package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import xcj.app.main.model.res.UserInfoRes
import xcj.app.main.model.table.mysql.User

//TODO UserID，GroupID, ScreenID, AppID脱敏处理
@Mapper
interface UserDao {

    fun getUserByAccountAndPassword(account: String, password: String): User?

    fun getUserByAccount(account: String): User?

    fun getUserUidByAccountAndPassword(account: String, password: String): String?

    fun addUser(
        uid: String,
        account: String,
        password: String,
        canMultiOnline: Int,
        salt: String?,
        hash: String?,
        name: String,
        avatarUrl: String?,
        introduction: String?,
        tags: String?,
        sex: String?,
        age: Int?,
        phone: String?,
        email: String?,
        area: String?,
        address: String?,
        website: String?
    ): Int

    fun getUserInfoResByUid(uid: String): UserInfoRes

    fun getUserInfoResList(uids: List<String>): List<UserInfoRes>

    fun updateUser(user: User): Int

    fun updateUserSaltHash(user: User): Int

    @Select("select count(1) from user_2022 where account = #{account}")
    fun isUserAccountExist(account: String): Boolean

    @Insert("insert into user_2022_deleted(uid, create_time) value(#{uid}, current_timestamp())")
    fun addDeleteUser(uid: String): Int

    @Delete("delete from user_2022 where uid=#{uid}")
    fun deleteUser(uid: String): Int

    fun findUserCountByUserIds(uids: List<String>): Int

    fun isUserIdExist(uid: String): Boolean

    fun searchUserInfoResByKeywords(
        accountEncFromKeywords: String,
        keywords: String,
        limit: Int,
        offset: Int
    ): List<UserInfoRes>

    fun getUserByUid(uid: String): User?
}