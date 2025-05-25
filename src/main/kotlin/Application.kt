package trab

import io.ktor.server.application.*

/**
 * Main entry point of the application.
 * 
 * This function starts the Ktor server using the Netty engine.
 * The server configuration is loaded from the application.conf file.
 * 
 * @param args Command line arguments passed to the application
 */
fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * Application module configuration function.
 * 
 * This function configures all aspects of the Ktor application:
 * - Templating (Thymeleaf for HTML rendering)
 * - Serialization (JSON conversion for API responses)
 * - Security (Authentication, authorization, and session management)
 * - HTTP settings (CORS configuration)
 * - Routing (All application endpoints)
 * 
 * The order of configuration is important as some features depend on others.
 */
fun Application.module() {
    configureTemplating()
    configureSerialization()
    configureSecurity()
    configureHTTP()
    configureRouting()
}
