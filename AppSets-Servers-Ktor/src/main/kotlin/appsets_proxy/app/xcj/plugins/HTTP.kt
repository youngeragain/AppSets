package appsets_proxy.app.xcj.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowHeaders {
            true
        }
        allowOrigins {
            true
        }
        allowHost("localhost")
        //anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}
