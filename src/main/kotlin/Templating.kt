package trab

import trab.casino.ExchangeLogic
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import kotlinx.serialization.Serializable
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
        get("/welcome") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(ThymeleafContent("welcome", mapOf("name" to player.name, "chips" to player.chips, "money" to player.money)))
            } else {
                call.respondRedirect("/static/index.html")
            }
        }

        get("/leave") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(ThymeleafContent("leave", mapOf("name" to player.name)))
            } else {
                call.respondRedirect("/static/index.html")
            }
        }

        get("/receptionist") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(ThymeleafContent("receptionist", mapOf("name" to player.name, "chips" to player.chips, "money" to player.money)))
            } else {
                call.respondRedirect("/static/index.html")
            }
        }
    }
}