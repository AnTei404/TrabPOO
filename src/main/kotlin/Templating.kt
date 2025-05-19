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
import kotlin.text.get

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
        static("/static") {
            resources("static")
        }

        static("/Baralhos") {
            resources("Baralhos")
        }

        get("/welcome") {
            val player = call.sessions.get<Player>()
            if (player == null) {
                call.application.log.info("Player session is null")
                call.respondRedirect("/index.html")
            } else {
                call.respond(
                    ThymeleafContent(
                        "welcome",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money
                        )
                    )
                )
            }
        }

        get("/receptionist") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val previewCards = generatePreviewCards(deckStyle)
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "receptionist",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "deckStyle" to deckStyle,
                            "previewCards" to previewCards
                        )
                    )
                )
            } else {
                call.respondRedirect("/index.html")
            }
        }

        get("/games") {
            call.respond(ThymeleafContent("games", emptyMap()))
        }

        get("/leave") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                call.respond(
                    ThymeleafContent(
                        "leave",
                        mapOf("name" to player.name)
                    )
                )
            } else {
                call.respondRedirect("/index.html")
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
                call.respondRedirect("/index.html")
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
                call.respondRedirect("/index.html")
            }
        }
    }
}