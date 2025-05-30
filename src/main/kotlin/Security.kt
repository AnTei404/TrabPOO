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

/**
 * Configures security-related features for the application.
 * 
 * This function sets up:
 * - Session management for players and their preferences
 * - Player registration route
 * 
 * Sessions are used to track player information across requests,
 * allowing the application to maintain state for each player.
 * 
 * @param Application The Ktor application instance to configure
 */
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
        // Route to handle player registration/login
        post("/submit-name") {
            val parameters = call.receiveParameters()
            val name = parameters["name"] ?: return@post call.respondText(
                "Name is required", status = HttpStatusCode.BadRequest
            )
            val photoPath = parameters["photoPath"] ?: "/Player/Spongebob.jpg" // Default photo if none selected

            // Store the selected photo in the map
            setPlayerPhoto(name, photoPath)

            // Create player with the selected photo
            val player = Player(name = name)
            // Update the player's photoPath to ensure it uses the selected photo
            player.photoPath = photoPath

            // Set session cookies
            call.sessions.set(player)
            call.sessions.set(DeckStyle("minimalista")) // Default deck style
            call.respondRedirect("/lobby")
        }
    }
}

/**
 * Represents a record of a bet made by a player.
 *
 * @property gameName The name of the game where the bet was placed
 * @property betAmount The amount of chips bet
 * @property winAmount The amount of chips won (0 if lost)
 * @property timestamp The time when the bet was placed
 */
@Serializable
data class BetRecord(
    val gameName: String,
    val betAmount: Int,
    val winAmount: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Returns a formatted timestamp string for display
     */
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("HH:mm:ss")
        return format.format(date)
    }
}

/**
 * Represents a player in the casino.
 * 
 * This class stores all player-related information including:
 * - Personal details (name, photo)
 * - Game assets (chips, money)
 * - Game state (last bet)
 * - Bet history
 * 
 * @property name The player's name, used as a unique identifier
 * @property chips The player's current chip balance for gambling
 * @property money The player's current money balance (can be exchanged for chips)
 * @property lastBet The amount of chips from the player's last bet
 * @property photoPath The path to the player's avatar image
 * @property betHistory List of the player's past bets
 */
@Serializable
data class Player(
    val name: String,
    var chips: Int = Random.nextInt(0, 301), // Random chips between 0 and 300
    var money: Int = Random.nextInt(50, 301), // Random money between 50 and 300
    var lastBet: Int? = null,
    var photoPath: String = "", // Will be initialized in init block or set explicitly
    var betHistory: MutableList<BetRecord> = mutableListOf()
) {
    // Initialize photoPath when Player is created
    init {
        // Only set a default photo if one hasn't been explicitly set
        if (photoPath.isEmpty()) {
            photoPath = getOrCreatePlayerPhoto(name)
        }
    }

    /**
     * Adds a bet record to the player's history
     *
     * @param gameName The name of the game
     * @param betAmount The amount bet
     * @param winAmount The amount won (0 if lost)
     */
    fun addBetRecord(gameName: String, betAmount: Int, winAmount: Int) {
        betHistory.add(BetRecord(gameName, betAmount, winAmount))
    }

    /**
     * Calculates the total amount won or lost from all bets
     *
     * @return The net amount (positive for overall win, negative for overall loss)
     */
    fun getTotalWinLoss(): Int {
        return betHistory.sumOf { it.winAmount - it.betAmount }
    }
}

/**
 * Represents the visual style of playing cards.
 * 
 * This class stores the player's preference for card appearance,
 * which is used when rendering card games.
 * 
 * @property style The name of the card style (e.g., "minimalista", "pixel art", "balatro")
 */
@Serializable
data class DeckStyle(
    val style: String
)
