package xcj.app.test

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Lazy
import xcj.app.UserApplication
//import xcj.app.account.service.AccountService

@SpringBootTest(classes = [UserApplication::class])
@Lazy
class Testing {
    /*@Autowired
    lateinit var accountService: AccountService*/

    @Test
    fun getServiceName_withGiveAnnotationName(){
       /* accountService.doSomeThing()*/
    }
}