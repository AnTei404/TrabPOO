package trab

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

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<Player>("PLAYER_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    routing {
        post("/submit-name") {
            val name = call.receiveParameters()["name"] ?: return@post call.respondText(
                "Name is required", status = HttpStatusCode.BadRequest
            )
            call.sessions.set(Player(name))
            call.respondRedirect("/welcome")
        }
    }
}

@Serializable
data class Player(
    val name: String,
    val chips: Int = (0..500).random(),
    val money: Int = (500..1000).random()
)