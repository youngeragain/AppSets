package xcj.app.main.dao.mongo

import xcj.app.main.model.table.mongo.User

interface UserDao {
    fun addUser(user: User)
}