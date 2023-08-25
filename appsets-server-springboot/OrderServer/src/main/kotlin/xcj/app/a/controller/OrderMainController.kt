package xcj.app.a.controller

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xcj.app.DesignResponse
import xcj.app.a.ibatis.IUserService
import xcj.app.a.ibatis.service.UserService1
import xcj.app.a.jpa.service.UserService
import xcj.app.a.model.CustomQueryBody
import xcj.app.a.model.User
import jakarta.annotation.Resource


@RequestMapping("/order")
@RestController
class OrderMainController {
    val log: Log = LogFactory.getLog(OrderMainController::class.java)

    @Autowired
    @Qualifier("jpaUserService")
    lateinit var userService: UserService

    @Resource
    lateinit var ibatisUserService:IUserService

    @Resource
    lateinit var ibatisUserService1:IUserService

    @RequestMapping("/order/{orderNo}")
    fun getOderInfo(@PathVariable orderNo: String): DesignResponse<List<User>?> {
        val any = userService.getAll()
        val designResponse = DesignResponse<List<User>?>(data = any, info = "orderNo:$orderNo, info is xxx")
        log.debug("baseResponse:$designResponse")
        return designResponse
    }

    @RequestMapping("/users")
    fun getUsers(): DesignResponse<List<User>?> {
        val any = ibatisUserService.getAll()
        val designResponse = DesignResponse<List<User>?>(data = any, info = "get all users")
        log.debug("baseResponse:$designResponse")
        return designResponse
    }


    @RequestMapping("/customQuery")
    fun customQuery(
        @RequestBody(required = false)
        body: CustomQueryBody?,
        @RequestParam(name = "q") queryString: String?):
            DesignResponse<List<HashMap<String, String?>>?>? {
        val any = (body?.queryString?:queryString)?.let { (ibatisUserService1 as? UserService1)?.customQuery(it) }
        val designResponse = DesignResponse<List<HashMap<String, String?>>?>(data = any, info = "customQuery")
        log.debug("baseResponse:$designResponse")
        return designResponse
    }

}