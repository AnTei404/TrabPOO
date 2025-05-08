package ahahahh.domain

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

fun Application.configureTemplating() {
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/thymeleaf/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
    routing {
        static("/") {
            resources("static")
            defaultResource("index.html", "static")
        }

        post("/submit-name") {
            val userName = call.receiveParameters()["name"] ?: "Guest"
            call.respondRedirect("/welcome?name=$userName")
        }

        get("/welcome") {
            val userName = call.request.queryParameters["name"] ?: "Guest"
            call.respond(ThymeleafContent("welcome", mapOf("userName" to userName)))
        }

        get("/leave") {
            call.respond(ThymeleafContent("leave", emptyMap<String, Any>()))
        }
    }
}
data class ThymeleafUser(val id: Int, val name: String)
