package xcj.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class UserApplication

fun main(args:Array<String>) {
    SpringApplication.run(UserApplication::class.java, *args)
}