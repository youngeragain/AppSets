package xcj.app.a.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import xcj.app.a.model.User

interface UserRepository: JpaRepository<User, Long> {


}