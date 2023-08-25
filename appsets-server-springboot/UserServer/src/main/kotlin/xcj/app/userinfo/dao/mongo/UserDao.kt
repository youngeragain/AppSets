package xcj.app.userinfo.dao.mongo

import xcj.app.userinfo.model.table.mongo.User

interface UserDao {
    fun addUser(user: User)
}