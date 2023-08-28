package appsets_proxy.app.xcj.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/appsets/file/get/app/release/android") {
            call.respondRedirect("https://localhost/applications/android/appsets3-dev-latest.apk")
        }
    }
}
