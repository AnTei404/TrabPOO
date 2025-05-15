package trab

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.*
import trab.casino.ExchangeLogic

fun Application.configureRouting() {
    val exchangeLogic = ExchangeLogic()

    routing {
        staticResources("/", "static")

        post("/exchange-money-for-chips") {
            val player = call.sessions.get<Player>()
            val moneyToExchange = call.receiveParameters()["money"]?.toIntOrNull()
            if (player != null && moneyToExchange != null) {
                val updatedPlayer = exchangeLogic.exchangeMoneyForChips(player, moneyToExchange)
                if (updatedPlayer != null) {
                    call.sessions.set(updatedPlayer)
                    call.respondRedirect("/receptionist")
                } else {
                    call.respondText("Invalid exchange request", status = HttpStatusCode.BadRequest)
                }
            } else {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
            }
        }

        post("/exchange-chips-for-money") {
            val player = call.sessions.get<Player>()
            val chipsToExchange = call.receiveParameters()["chips"]?.toIntOrNull()
            if (player != null && chipsToExchange != null) {
                val updatedPlayer = exchangeLogic.exchangeChipsForMoney(player, chipsToExchange)
                if (updatedPlayer != null) {
                    call.sessions.set(updatedPlayer)
                    call.respondRedirect("/receptionist")
                } else {
                    call.respondText("Invalid exchange request", status = HttpStatusCode.BadRequest)
                }
            } else {
                call.respondText("Invalid input", status = HttpStatusCode.BadRequest)
            }
        }

        post("/casino/blackjack/hit") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                val gameState = blackjack.hit()
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState
                        )
                    )
                )
            } else {
                call.respondRedirect("/index.html")
            }
        }

        post("/casino/blackjack/stand") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                val gameState = blackjack.stand()
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState
                        )
                    )
                )
            } else {
                call.respondRedirect("/index.html")
            }
        }

        post("/casino/blackjack/restart") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                val gameState = blackjack.restartGame()
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState
                        )
                    )
                )
            } else {
                call.respondRedirect("/index.html")
            }
        }
    }
}