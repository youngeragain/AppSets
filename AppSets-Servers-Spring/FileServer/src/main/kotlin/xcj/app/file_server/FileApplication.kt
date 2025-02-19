package xcj.app.file_server

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import xcj.app.util.PurpleLogger
import java.io.File

@SpringBootApplication
class FileApplication {
}

fun main(args: Array<String>) {
    SpringApplication.run(FileApplication::class.java)
}