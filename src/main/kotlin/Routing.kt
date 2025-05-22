package trab

import trab.mapOfNonNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.thymeleaf.*
import trab.casino.ExchangeLogic
import io.ktor.server.thymeleaf.ThymeleafContent
import trab.casino.BingoGame
import trab.casino.generatePreviewCards
import kotlin.collections.set
import kotlin.compareTo
import kotlin.text.get
import kotlin.text.set
import trab.casino.BingoGameState
import kotlin.text.get
import kotlin.text.set
import kotlin.times
import trab.casino.HigherOrLowerGame
import trab.casino.CardMatch
import trab.casino.toCardWithImage
import kotlin.text.get
import kotlin.text.set

fun Application.configureRouting() {
    val exchangeLogic = ExchangeLogic()
    val bingoGames = mutableMapOf<String, BingoGame>() // In-memory storage for BingoGame per player
    val higherOrLowerGames = mutableMapOf<String, HigherOrLowerGame>()

    routing {
        staticResources("/", "static")

        post("/select-deck") {
            val player = call.sessions.get<Player>()
            val selectedDeck = call.receiveParameters()["deck"] ?: "minimalista" // Default to "minimalista"
            if (player != null && selectedDeck in listOf("minimalista", "pixel Art", "balatro")) {
                call.sessions.set(DeckStyle(selectedDeck))
                val previewCards = generatePreviewCards(selectedDeck)
                call.respond(
                    ThymeleafContent(
                        "receptionist",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "deckStyle" to selectedDeck,
                            "previewCards" to previewCards,
                            "successMessage" to "Deck saved successfully!"
                        )
                    )
                )
            } else {
                call.respondText("Invalid deck selection", status = HttpStatusCode.BadRequest)
            }
        }

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
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista" // Default to "minimalista"
            if (player != null && chipsBet != null && chipsBet > 0 && chipsBet <= player.chips) {
                val updatedPlayer = player.copy(lastBet = chipsBet, chips = player.chips - chipsBet)
                call.sessions.set(updatedPlayer)
                val gameState = blackjack.startGame(deckStyle)
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState,
                            "chipsBet" to chipsBet,
                            "chips" to updatedPlayer.chips,
                            "deckStyle" to deckStyle // Pass deckStyle here
                        )
                    )
                )
            } else {
                call.respondText("Invalid bet", status = HttpStatusCode.BadRequest)
            }
        }

        post("/casino/blackjack/hit") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista" // Default to "minimalista"
            if (player != null) {
                val gameState = blackjack.hit(deckStyle) // Pass deckStyle here
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
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista" // Default to "minimalista"
            if (player != null) {
                val gameState = blackjack.stand(deckStyle) // Pass deckStyle here
                val chipsBet = player.lastBet ?: 0
                var resultMessage = ""
                var updatedPlayer = player
                when (gameState.gameState) {
                    "Blackjack! You win 3x!" -> {
                        updatedPlayer = player.copy(chips = player.chips + chipsBet * 3)
                        resultMessage = "Blackjack! You won ${chipsBet * 3} chips and now have ${updatedPlayer.chips} chips."
                    }
                    "You win!" -> {
                        updatedPlayer = player.copy(chips = player.chips + chipsBet * 2)
                        resultMessage = "You won $chipsBet chips and now have ${updatedPlayer.chips} chips."
                    }
                    "You lose!" -> {
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
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista" // Default to "minimalista"
            if (player != null && lastBet != null) {
                if (player.chips >= lastBet) {
                    val updatedPlayer = player.copy(chips = player.chips - lastBet)
                    call.sessions.set(updatedPlayer)
                    val gameState = blackjack.restartGame(deckStyle) // Pass deckStyle here
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
                    val gameState = blackjack.restartGame(deckStyle) // Pass deckStyle here
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

        post("/casino/slots/bet") {
            val player = call.sessions.get<Player>()
            val chipsBet = call.receiveParameters()["chipsBet"]?.toIntOrNull()
            if (player != null && chipsBet != null && chipsBet > 0 && chipsBet <= player.chips) {
                val updatedPlayer = player.copy(lastBet = chipsBet, chips = player.chips - chipsBet)
                call.sessions.set(updatedPlayer)
                call.respond(
                    ThymeleafContent(
                        "slots",
                        mapOf(
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to updatedPlayer.chips,
                            "reels" to listOf("❓", "❓", "❓"),
                            "resultMessage" to "Press Spin to play!",
                            "win" to false,
                            "payout" to 0
                        )
                    )
                )
            } else {
                call.respondText("Invalid bet", status = HttpStatusCode.BadRequest)
            }
        }

        post("/casino/slots/spin") {
            val player = call.sessions.get<Player>()
            val chipsBet = call.receiveParameters()["chipsBet"]?.toIntOrNull() ?: player?.lastBet ?: 0
            if (player != null && chipsBet > 0 && player.chips >= chipsBet) {
                val updatedPlayer = player.copy(
                    chips = player.chips - chipsBet,
                    lastBet = chipsBet
                )
                val slots = trab.casino.Slots()
                val result = slots.spin(chipsBet)
                val emojiGrid = result.grid.map { row -> row.map { trab.casino.symbolToEmoji(it) } }
                var finalPlayer = updatedPlayer
                val win = result.payout > 0
                val resultMessage = if (win) {
                    finalPlayer = updatedPlayer.copy(chips = updatedPlayer.chips + result.payout)
                    "You won ${result.payout} chips! 🎉 (Lines: ${result.winLines.joinToString()})"
                } else {
                    "No win. Try again!"
                }
                call.sessions.set(finalPlayer)
                call.respond(
                    ThymeleafContent(
                        "slots",
                        mapOf(
                            "name" to finalPlayer.name,
                            "chipsBet" to chipsBet,
                            "chips" to finalPlayer.chips,
                            "grid" to emojiGrid,
                            "resultMessage" to resultMessage,
                            "win" to win,
                            "payout" to result.payout
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/slots")
            }
        }

        post("/casino/slots/change-bet") {
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

        post("/casino/bingo/bet") {
            val player = call.sessions.get<Player>()
            val chipsBet = call.receiveParameters()["chipsBet"]?.toIntOrNull()
            if (player != null && chipsBet != null && chipsBet > 0 && chipsBet <= player.chips) {
                val updatedPlayer = player.copy(lastBet = chipsBet, chips = player.chips - chipsBet)
                call.sessions.set(updatedPlayer)
                val bingoGame = BingoGame()
                bingoGames[player.name] = bingoGame // Store in-memory
                val gameState = BingoGameState(
                    userCard = bingoGame.userCard,
                    houseCards = bingoGame.houseCards,
                    drawnNumbers = bingoGame.drawnNumbers,
                    lastNumber = bingoGame.lastNumber,
                    userHasBingo = bingoGame.userHasBingo(),
                    houseWinners = bingoGame.houseWinners()
                )
                call.respond(
                    ThymeleafContent(
                        "Bingo",
                        mapOf(
                            "name" to updatedPlayer.name,
                            "chipsBet" to chipsBet,
                            "chips" to updatedPlayer.chips,
                            "numPlayers" to 1 + bingoGame.houseCards.size,
                            "gameState" to gameState
                        )
                    )
                )
            } else {
                call.respondText("Invalid bet", status = HttpStatusCode.BadRequest)
            }
        }

        post("/casino/bingo/next") {
            val player = call.sessions.get<Player>()
            val bingoGame = player?.name?.let { bingoGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && bingoGame != null) {
                bingoGame.nextNumber()
                val userHasBingo = bingoGame.userHasBingo()
                val houseWinners = bingoGame.houseWinners()
                val tie = userHasBingo && houseWinners.isNotEmpty()
                var tempPlayer = player
                var resultMessage = ""
                if (userHasBingo && !tie) {
                    tempPlayer = player.copy(chips = player.chips + chipsBet * 6)
                    resultMessage = "Bingo! You win ${chipsBet * 6} chips and now have ${tempPlayer.chips} chips."
                } else if (tie) {
                    tempPlayer = player.copy(chips = player.chips + chipsBet)
                    resultMessage = "It's a tie! Your bet is returned. You have ${tempPlayer.chips} chips."
                } else if (houseWinners.isNotEmpty()) {
                    resultMessage = "House wins! You lost $chipsBet chips and now have ${player.chips} chips."
                }
                val finalPlayer = tempPlayer
                call.sessions.set(finalPlayer)
                val gameState = BingoGameState(
                    userCard = bingoGame.userCard,
                    houseCards = bingoGame.houseCards,
                    drawnNumbers = bingoGame.drawnNumbers,
                    lastNumber = bingoGame.lastNumber,
                    userHasBingo = userHasBingo,
                    houseWinners = houseWinners,
                    tie = tie
                )
                call.respond(
                    ThymeleafContent(
                        "Bingo",
                        mapOf(
                            "name" to finalPlayer.name,
                            "chipsBet" to chipsBet,
                            "chips" to finalPlayer.chips,
                            "numPlayers" to 1 + bingoGame.houseCards.size,
                            "gameState" to gameState,
                            "resultMessage" to resultMessage
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/bingo")
            }
        }

        post("/casino/bingo/new") {
            val player = call.sessions.get<Player>()
            val chipsBet = player?.lastBet ?: 0
            if (player != null && chipsBet > 0 && player.chips >= chipsBet) {
                val updatedPlayer = player.copy(chips = player.chips - chipsBet)
                call.sessions.set(updatedPlayer)
                val bingoGame = BingoGame()
                bingoGames[player.name] = bingoGame // Store in-memory
                val gameState = BingoGameState(
                    userCard = bingoGame.userCard,
                    houseCards = bingoGame.houseCards,
                    drawnNumbers = bingoGame.drawnNumbers,
                    lastNumber = bingoGame.lastNumber,
                    userHasBingo = bingoGame.userHasBingo(),
                    houseWinners = bingoGame.houseWinners()
                )
                call.respond(
                    ThymeleafContent(
                        "Bingo",
                        mapOf(
                            "name" to updatedPlayer.name,
                            "chipsBet" to chipsBet,
                            "chips" to updatedPlayer.chips,
                            "numPlayers" to 1 + bingoGame.houseCards.size,
                            "gameState" to gameState
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/bingo")
            }
        }

        post("/casino/higherorlower/bet") {
            val player = call.sessions.get<Player>()
            val params = call.receiveParameters()
            val chipsBet = params["chipsBet"]?.toIntOrNull() ?: 0
            if (player == null || chipsBet <= 0 || chipsBet > player.chips) {
                call.respondRedirect("/casino/higherorlower")
                return@post
            }
            val updatedPlayer = player.copy(chips = player.chips - chipsBet, lastBet = chipsBet)
            call.sessions.set(updatedPlayer)

            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = HigherOrLowerGame()
            higherOrLowerGames[updatedPlayer.name] = game
            val gameState = game.getState(deckStyle)
            call.respond(
                ThymeleafContent(
                    "higherorlower",
                    mapOfNonNull(
                        "name" to updatedPlayer.name,
                        "chips" to updatedPlayer.chips,
                        "chipsBet" to chipsBet,
                        "playerCard" to gameState.playerCard,
                        "dealerCard" to gameState.dealerCard,
                        "showDealerCard" to gameState.showDealerCard,
                        "result" to gameState.result,
                        "resultMessage" to "",
                        "currentRound" to gameState.currentRound,
                        "totalRounds" to gameState.totalRounds,
                        "multiplier" to gameState.multiplier,
                        "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                        "gameOver" to gameState.gameOver
                    )
                )
            )
        }

        // GUESS: Player makes a guess (higher/lower)
        post("/casino/higherorlower/guess") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val guess = call.receiveParameters()["guess"]
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null && guess != null) {
                val higher = guess == "higher"
                val gameState = game.guess(higher, deckStyle)
                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to "",
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

// CONTINUE: Next round after a win (not all-in/leave)
        post("/casino/higherorlower/next") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null) {
                val gameState = game.nextRound(deckStyle)
                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to "",
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

// ALL IN: Player chooses to go all in (increase rounds/multiplier)
        post("/casino/higherorlower/allin") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null) {
                val gameState = game.allIn(deckStyle)
                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to "",
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

        post("/casino/higherorlower/restart") {
            val player = call.sessions.get<Player>()
            val lastBet = player?.lastBet
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            if (player != null && lastBet != null && player.chips >= lastBet) {
                val updatedPlayer = player.copy(chips = player.chips - lastBet)
                call.sessions.set(updatedPlayer)
                val game = HigherOrLowerGame()
                higherOrLowerGames[updatedPlayer.name] = game
                val gameState = game.getState(deckStyle)
                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to updatedPlayer.name,
                            "chips" to updatedPlayer.chips,
                            "chipsBet" to lastBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to "",
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

    // LEAVE: Player leaves and cashes out winnings
        post("/casino/higherorlower/leave") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null) {
                val gameState = game.leave(deckStyle)
                // Calculate winnings
                val winnings = chipsBet * gameState.multiplier
                val updatedPlayer = player.copy(
                    chips = player.chips + winnings,
                    lastBet = null
                )
                call.sessions.set(updatedPlayer)
                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to "",
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }
    }
}