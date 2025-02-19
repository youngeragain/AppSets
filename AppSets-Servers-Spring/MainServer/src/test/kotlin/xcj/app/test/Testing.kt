package xcj.app.test

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Lazy
import xcj.app.MainApplication
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

//import xcj.app.account.service.AccountService

@SpringBootTest(classes = [MainApplication::class])
@Lazy
class Testing {
    /*@Autowired
    lateinit var accountService: AccountService*/

    @Test
    fun getServiceName_withGiveAnnotationName(){
       /* accountService.doSomeThing()*/
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun decodeBase64() {
        val value =
            "eyJyYWJiaXQtaG9zdCI6IjE5Mi4xNjguMTguMTM4IiwicmFiYml0LXBvcnQiOjU2NzIsInJhYmJpdC1hZG1pbi11c2VybmFtZSI6InRlc3R1c2VyMSIsInJhYmJpdC1hZG1pbi1wYXNzd29yZCI6InRlc3R1c2VyMXB3ZCIsInJhYmJpdC12aXJ0dWFsLWhvc3QiOiIvIiwicXVldWUtcHJlZml4IjoidXNlcl8iLCJyb3V0aW5nLWtleS1wcmVmaXgiOiJtc2cuIiwidXNlci1leGNoYW5nZS1tYWluIjoib25lMm9uZS10b3BpYyIsInVzZXItZXhjaGFuZ2UtbWFpbi1wYXJlbnQiOiJvbmUyb25lLWZhbm91dCIsInVzZXItZXhjaGFuZ2UtZ3JvdXBzIjoiZ3JvdXAxLGdyb3VwMiIsInVzZXItZXhjaGFuZ2UtZ3JvdXAtcHJlZml4Ijoib25lMm1hbnktZmFub3V0LSJ9"
        val rawValue = Base64.decode(value.toByteArray()).decodeToString()
        println(rawValue)
    }
}