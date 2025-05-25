package trab

import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/**
 * Configures JSON serialization for the application.
 * 
 * This function sets up the serialization framework for converting Kotlin objects
 * to JSON responses. It also includes a test endpoint that demonstrates
 * the serialization functionality by returning a simple JSON object.
 * 
 * @param Application The Ktor application instance to configure
 */
fun Application.configureSerialization() {
    routing {
        // Test endpoint to verify serialization is working
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
