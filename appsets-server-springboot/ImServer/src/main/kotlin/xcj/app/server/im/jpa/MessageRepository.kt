package xcj.app.server.im.jpa

import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository: JpaRepository<xcj.app.server.im.controller.Message, Long> {


}