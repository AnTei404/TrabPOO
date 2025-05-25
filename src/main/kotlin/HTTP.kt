package trab

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Configures HTTP-related settings for the application.
 * 
 * This function sets up Cross-Origin Resource Sharing (CORS) configuration
 * to allow specific HTTP methods and headers from any host.
 * 
 * @param Application The Ktor application instance to configure
 */
fun Application.configureHTTP() {
    // Install CORS plugin to handle cross-origin requests
    install(CORS) {
        // Allow specific HTTP methods
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // Allow specific HTTP headers
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")

        // Allow requests from any host
        // @TODO: Don't do this in production if possible. Try to limit it.
        anyHost()
    }
}
