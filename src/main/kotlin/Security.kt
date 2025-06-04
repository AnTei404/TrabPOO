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
import kotlin.random.Random
import trab.getOrCreatePlayerPhoto

fun Application.configureSecurity() {
    // Configure session cookies for player data and deck style preferences
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
            val parameters = call.receiveParameters()
            val name = parameters["name"] ?: return@post call.respondText(
                "Name is required", status = HttpStatusCode.BadRequest
            )
            val photoPath = parameters["photoPath"] ?: "/Player/Spongebob.jpg"

            setPlayerPhoto(name, photoPath)

            val player = Player(name = name)
            player.photoPath = photoPath

            // Set session cookies
            call.sessions.set(player)
            call.sessions.set(DeckStyle("minimalista"))
            call.respondRedirect("/lobby")
        }
    }
}

@Serializable
data class BetRecord(
    val gameName: String,
    val betAmount: Int,
    val winAmount: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm:ss")
        return format.format(date)
    }
}

@Serializable
data class Player(
    val name: String,
    var chips: Int = Random.nextInt(0, 301),
    var money: Int = Random.nextInt(50, 301),
    var lastBet: Int? = null,
    var photoPath: String = "",
    var betHistory: MutableList<BetRecord> = mutableListOf()
) {
    init {
        if (photoPath.isEmpty()) {
            photoPath = getOrCreatePlayerPhoto(name)
        }
    }


    fun addBetRecord(gameName: String, betAmount: Int, winAmount: Int) {
        betHistory.add(BetRecord(gameName, betAmount, winAmount))
    }

    fun getTotalWinLoss(): Int {
        return betHistory.sumOf { it.winAmount - it.betAmount }
    }
}

@Serializable
data class DeckStyle(
    val style: String
)
