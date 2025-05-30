package trab

import io.ktor.server.application.*
import io.ktor.server.http.content.* // Import for static and resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import trab.casino.Blackjack
import trab.casino.generatePreviewCards
import trab.removePlayerPhoto
import trab.selectPhoto

val blackjack = Blackjack() // Shared instance

fun Application.configureTemplating() {
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/thymeleaf/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }

    routing {
        // Serve the index page as a Thymeleaf template
        get("/") {
            val defaultPhoto = "/Player/Spongebob.jpg"
            val photoGrid = selectPhoto(defaultPhoto)
            call.respond(
                ThymeleafContent(
                    "index",
                    mapOf(
                        "photoGrid" to photoGrid,
                        "selectedPhoto" to defaultPhoto
                    )
                )
            )
        }

        get("/lobby") {
            val player = call.sessions.get<Player>()
            if (player == null) {
                call.application.log.info("Player session is null")
                call.respondRedirect("/")
            } else {
                call.respond(
                    ThymeleafContent(
                        "lobby",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money
                        )
                    )
                )
            }
        }

        get("/services") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            if (player != null) {
                val previewCards = generatePreviewCards(deckStyle)

                // Get the player's bet history and total win/loss
                val betHistory = player.betHistory.sortedByDescending { it.timestamp }.take(20) // Show last 20 bets
                val totalWinLoss = player.getTotalWinLoss()

                call.respond(
                    ThymeleafContent(
                        "services",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "photoPath" to player.photoPath,
                            "deckStyle" to deckStyle,
                            "previewCards" to previewCards,
                            "betHistory" to betHistory,
                            "totalWinLoss" to totalWinLoss
                        )
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }

        get("/games") {
            call.respond(ThymeleafContent("games", emptyMap()))
        }

        get("/leave") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                // Store the name before clearing the session
                val playerName = player.name

                // Remove the player's photo from the map
                removePlayerPhoto(playerName)

                // Clear the session when the user leaves
                call.sessions.clear<Player>()
                call.sessions.clear<DeckStyle>()

                call.respond(
                    ThymeleafContent(
                        "leave",
                        mapOf("name" to playerName)
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }

        get("/casino/blackjack") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "bet",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "title" to "Blackjack - Place Your Bet",
                            "formAction" to "/casino/blackjack/bet"
                        )
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }

        get("/casino/slots") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "bet",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "title" to "Slots - Place Your Bet",
                            "formAction" to "/casino/slots/bet"
                        )
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }

        get("/casino/bingo") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "bet",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "title" to "Bingo - Place Your Bet",
                            "formAction" to "/casino/bingo/bet"
                        )
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }

        get("/casino/higherorlower") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "bet",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "title" to "Higher or Lower - Place Your Bet",
                            "formAction" to "/casino/higherorlower/bet"
                        )
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }

        get("/casino/ridethebus") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "bet",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "title" to "Ride the Bus - Place Your Bet",
                            "formAction" to "/casino/ridethebus/bet"
                        )
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }

        // Mines Game Routes
        get("/casino/mines") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "bet",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "title" to "Mines - Place Your Bet",
                            "formAction" to "/casino/mines/bet"
                        )
                    )
                )
            } else {
                call.respondRedirect("/")
            }
        }
    }
}
