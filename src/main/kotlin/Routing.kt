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

        post("/casino/blackjack/bet") {
            val player = call.sessions.get<Player>()
            val chipsBet = call.receiveParameters()["chipsBet"]?.toIntOrNull()
            if (player != null && chipsBet != null && chipsBet > 0 && chipsBet <= player.chips) {
                val updatedPlayer = player.copy(lastBet = chipsBet, chips = player.chips - chipsBet)
                call.sessions.set(updatedPlayer)
                val gameState = blackjack.startGame()
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState,
                            "chipsBet" to chipsBet,
                            "chips" to updatedPlayer.chips
                        )
                    )
                )
            } else {
                call.respondText("Invalid bet", status = HttpStatusCode.BadRequest)
            }
        }

        post("/casino/blackjack/hit") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                val gameState = blackjack.hit()
                val chipsBet = player.lastBet ?: 0
                var resultMessage = ""
                var updatedChips = player.chips
                if (gameState.gameOver) {
                    when (gameState.gameState) {
                        "Bust! You lose." -> {
                            resultMessage = "You lost $chipsBet chips and now have $updatedChips chips."
                        }
                        else -> {}
                    }
                }
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState,
                            "chipsBet" to chipsBet,
                            "chips" to updatedChips,
                            "resultMessage" to resultMessage
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
                val chipsBet = player.lastBet ?: 0
                var resultMessage = ""
                var updatedPlayer = player
                when (gameState.gameState) {
                    "You win!" -> {
                        updatedPlayer = player.copy(chips = player.chips + chipsBet * 2)
                        resultMessage = "You won $chipsBet chips and now have ${updatedPlayer.chips} chips."
                    }
                    "You lose!" -> {
                        // No chips refunded
                        resultMessage = "You lost $chipsBet chips and now have ${player.chips} chips."
                    }
                    "It's a tie!" -> {
                        updatedPlayer = player.copy(chips = player.chips + chipsBet)
                        resultMessage = "It's a tie! Your bet is returned. You have ${updatedPlayer.chips} chips."
                    }
                }
                call.sessions.set(updatedPlayer)
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to updatedPlayer.name,
                            "gameState" to gameState,
                            "chipsBet" to chipsBet,
                            "chips" to updatedPlayer.chips,
                            "resultMessage" to resultMessage
                        )
                    )
                )
            } else {
                call.respondRedirect("/index.html")
            }
        }

        post("/casino/blackjack/restart") {
            val player = call.sessions.get<Player>()
            val lastBet = player?.lastBet
            if (player != null && lastBet != null) {
                if (player.chips >= lastBet) {
                    val updatedPlayer = player.copy(chips = player.chips - lastBet)
                    call.sessions.set(updatedPlayer)
                    val gameState = blackjack.restartGame()
                    call.respond(
                        ThymeleafContent(
                            "blackjack",
                            mapOf(
                                "name" to player.name,
                                "gameState" to gameState,
                                "chipsBet" to lastBet,
                                "chips" to updatedPlayer.chips
                            )
                        )
                    )
                } else {
                    val gameState = blackjack.restartGame()
                    call.respond(
                        ThymeleafContent(
                            "blackjack",
                            mapOf(
                                "name" to player.name,
                                "gameState" to gameState,
                                "chipsBet" to lastBet,
                                "chips" to player.chips,
                                "error" to "You don't have enough."
                            )
                        )
                    )
                }
            } else {
                call.respondRedirect("/index.html")
            }
        }
    }
}