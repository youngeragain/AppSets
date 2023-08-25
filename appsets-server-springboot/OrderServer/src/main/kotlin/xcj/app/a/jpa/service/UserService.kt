package xcj.app.a.jpa.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xcj.app.a.ibatis.IUserService
import xcj.app.a.jpa.repository.UserRepository
import xcj.app.a.model.User

@Service("jpaUserService")
class UserService: IUserService {
    @Autowired
    lateinit var userRepository: UserRepository

    override fun getAll(): List<User> {
        return userRepository.findAll()
    }
}