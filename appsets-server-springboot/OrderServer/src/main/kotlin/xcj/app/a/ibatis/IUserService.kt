package xcj.app.a.ibatis

import xcj.app.a.model.User

interface IUserService {
    fun getAll():List<User>
}