package trab

import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureSerialization() {
    routing {
        // Test endpoint to verify serialization is working
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
