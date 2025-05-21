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
import trab.casino.BingoGame
import kotlin.random.Random

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<Player>("PLAYER_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
        cookie<DeckStyle>("DECK_STYLE_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    routing {
        post("/submit-name") {
            val name = call.receiveParameters()["name"] ?: return@post call.respondText(
                "Name is required", status = HttpStatusCode.BadRequest
            )
            call.sessions.set(Player(name = name)) // Use default constructor for random values
            call.sessions.set(DeckStyle("minimalista")) // Default deck style
            call.respondRedirect("/welcome")
        }
    }
}

@Serializable
data class Player(
    val name: String,
    val chips: Int = Random.nextInt(0, 301), // Random chips between 0 and 300
    val money: Int = Random.nextInt(50, 301), // Random money between 50 and 300
    val lastBet: Int? = null
)

@Serializable
data class DeckStyle(
    val style: String // e.g., "minimalista", "pixel Art", "balatro"
)