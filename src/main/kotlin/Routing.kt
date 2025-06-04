package trab

import trab.mapOfNonNull
import trab.createResultMessage
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
import trab.casino.RideTheBusGame
import trab.casino.CardMatch
import trab.casino.toCardWithImage
import trab.casino.Mines
import trab.casino.MinesGameState
import kotlin.compareTo


fun Application.configureRouting() {
    val exchangeLogic = ExchangeLogic()
    val bingoGames = mutableMapOf<String, BingoGame>()
    val higherOrLowerGames = mutableMapOf<String, HigherOrLowerGame>()
    val rideTheBusGames = mutableMapOf<String, RideTheBusGame>()
    val minesGames = mutableMapOf<String, Mines>()

    routing {
        staticResources("/", "static")
        post("/select-deck") {
            val player = call.sessions.get<Player>()
            val selectedDeck = call.receiveParameters()["deck"] ?: "minimalista" // Default to "minimalista"
            if (player != null && selectedDeck in listOf("minimalista", "pixel art", "balatro")) {
                call.sessions.set(DeckStyle(selectedDeck))
                val previewCards = generatePreviewCards(selectedDeck)
                call.respond(
                    ThymeleafContent(
                        "services",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "photoPath" to player.photoPath,
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
                    call.respondRedirect("/services")
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
                    call.respondRedirect("/services")
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
                player.chips -= chipsBet
                player.lastBet = chipsBet
                call.sessions.set(player)
                val gameState = blackjack.startGame(deckStyle)
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "deckStyle" to deckStyle, // Pass deckStyle here
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
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
                var updatedChips = player.chips
                val resultMessage = createResultMessage(
                    gameOver = gameState.gameOver && gameState.playerBust,
                    isWin = false,
                    chipsBet = chipsBet,
                    currentChips = updatedChips
                )
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState,
                            "chipsBet" to chipsBet,
                            "chips" to updatedChips,
                            "money" to player.money,
                            "resultMessage" to resultMessage,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/blackjack")
            }
        }

        post("/casino/blackjack/stand") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista" // Default to "minimalista"
            if (player != null) {
                val gameState = blackjack.stand(deckStyle) // Pass deckStyle here
                val chipsBet = player.lastBet ?: 0
                var resultMessage: String

                if (gameState.playerHasBlackjack && (gameState.dealerBust || gameState.playerTotal > gameState.dealerTotal)) {
                    val winAmount = chipsBet * 3
                    player.chips += winAmount
                    player.addBetRecord("Blackjack", chipsBet, winAmount)
                    resultMessage = "Blackjack! You won $winAmount chips and now have ${player.chips} chips."
                } else if (gameState.dealerBust || gameState.playerTotal > gameState.dealerTotal) {
                    val winAmount = chipsBet * 2
                    player.chips += winAmount
                    player.addBetRecord("Blackjack", chipsBet, winAmount)
                    resultMessage = createResultMessage(
                        gameOver = true,
                        isWin = true,
                        chipsBet = chipsBet,
                        currentChips = player.chips
                    )
                } else if (gameState.playerTotal < gameState.dealerTotal) {
                    player.addBetRecord("Blackjack", chipsBet, 0)
                    resultMessage = createResultMessage(
                        gameOver = true,
                        isWin = false,
                        chipsBet = chipsBet,
                        currentChips = player.chips
                    )
                } else {
                    player.chips += chipsBet
                    player.addBetRecord("Blackjack", chipsBet, chipsBet) // Tie - bet returned
                    resultMessage = "It's a tie! Your bet is returned. You have ${player.chips} chips."
                }

                call.sessions.set(player)
                call.respond(
                    ThymeleafContent(
                        "blackjack",
                        mapOf(
                            "name" to player.name,
                            "gameState" to gameState,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "resultMessage" to resultMessage,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/blackjack")
            }
        }

        post("/casino/blackjack/restart") {
            val player = call.sessions.get<Player>()
            val lastBet = player?.lastBet
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista" // Default to "minimalista"
            if (player != null && lastBet != null) {
                if (player.chips >= lastBet) {
                    player.chips -= lastBet
                    call.sessions.set(player)
                    val gameState = blackjack.restartGame(deckStyle) // Pass deckStyle here
                    call.respond(
                        ThymeleafContent(
                            "blackjack",
                            mapOf(
                                "name" to player.name,
                                "gameState" to gameState,
                                "chipsBet" to lastBet,
                                "chips" to player.chips,
                                "money" to player.money,
                                "playerPhoto" to getOrCreatePlayerPhoto(player.name)
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
                                "money" to player.money,
                                "error" to "You don't have enough.",
                                "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                            )
                        )
                    )
                }
            } else {
                call.respondRedirect("/casino/blackjack")
            }
        }

        post("/casino/slots/bet") {
            val player = call.sessions.get<Player>()
            val chipsBet = call.receiveParameters()["chipsBet"]?.toIntOrNull()
            if (player != null && chipsBet != null && chipsBet > 0 && chipsBet <= player.chips) {
                player.lastBet = chipsBet
                call.sessions.set(player)
                call.respond(
                    ThymeleafContent(
                        "slots",
                        mapOf(
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
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
                player.chips -= chipsBet
                player.lastBet = chipsBet

                val slots = trab.casino.Slots()
                val result = slots.spin(chipsBet)
                val emojiGrid = result.grid.map { row -> row.map { trab.casino.symbolToEmoji(it) } }
                val win = result.payout > 0
                // For slots, we keep the custom win message with line information
                val resultMessage = if (win) {
                    player.chips += result.payout
                    player.addBetRecord("Slots", chipsBet, result.payout)
                    "You won ${result.payout} chips! 🎉 (Lines: ${result.winLines.joinToString()})"
                } else {
                    player.addBetRecord("Slots", chipsBet, 0)
                    "No win. Try again!"
                }
                call.sessions.set(player)
                call.respond(
                    ThymeleafContent(
                        "slots",
                        mapOf(
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
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
                            "money" to player.money,
                            "title" to "Slots - Place Your Bet",
                            "formAction" to "/casino/slots/bet"
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/slots")
            }
        }

        post("/casino/bingo/bet") {
            val player = call.sessions.get<Player>()
            val chipsBet = call.receiveParameters()["chipsBet"]?.toIntOrNull()
            if (player != null && chipsBet != null && chipsBet > 0 && chipsBet <= player.chips) {
                player.lastBet = chipsBet
                player.chips -= chipsBet
                call.sessions.set(player)
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
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "numPlayers" to 1 + bingoGame.houseCards.size,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
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
                var resultMessage = ""
                if (userHasBingo && !tie) {
                    val winAmount = chipsBet * 3
                    player.chips += winAmount
                    player.addBetRecord("Bingo", chipsBet, winAmount)
                    resultMessage = "Bingo! You win $winAmount chips and now have ${player.chips} chips."
                } else if (tie) {
                    player.chips += chipsBet
                    player.addBetRecord("Bingo", chipsBet, chipsBet) // Tie - bet returned
                    resultMessage = "It's a tie! Your bet is returned. You have ${player.chips} chips."
                } else if (houseWinners.isNotEmpty()) {
                    player.addBetRecord("Bingo", chipsBet, 0)
                    resultMessage = createResultMessage(
                        gameOver = true,
                        isWin = false,
                        chipsBet = chipsBet,
                        currentChips = player.chips
                    )
                }
                call.sessions.set(player)
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
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "numPlayers" to 1 + bingoGame.houseCards.size,
                            "gameState" to gameState,
                            "resultMessage" to resultMessage,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
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
                player.chips -= chipsBet
                call.sessions.set(player)
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
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "numPlayers" to 1 + bingoGame.houseCards.size,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
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
            player.chips -= chipsBet
            player.lastBet = chipsBet
            call.sessions.set(player)

            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = HigherOrLowerGame()
            higherOrLowerGames[player.name] = game
            val gameState = game.getState(deckStyle)

            val resultMessage = createResultMessage(
                gameOver = gameState.gameOver,
                isWin = gameState.result != "Game over",
                chipsBet = chipsBet,
                currentChips = player.chips,
                multiplier = gameState.multiplier
            )

            call.respond(
                ThymeleafContent(
                    "higherorlower",
                    mapOfNonNull(
                        "name" to player.name,
                        "chips" to player.chips,
                        "money" to player.money,
                        "chipsBet" to chipsBet,
                        "playerCard" to gameState.playerCard,
                        "dealerCard" to gameState.dealerCard,
                        "showDealerCard" to gameState.showDealerCard,
                        "result" to gameState.result,
                        "resultMessage" to resultMessage,
                        "sideMenuResult" to gameState.sideMenuResult,
                        "currentRound" to gameState.currentRound,
                        "totalRounds" to gameState.totalRounds,
                        "multiplier" to gameState.multiplier,
                        "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                        "gameOver" to gameState.gameOver,
                        "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                    )
                )
            )
        }

        post("/casino/higherorlower/guess") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val guess = call.receiveParameters()["guess"]
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null && guess != null) {
                val higher = guess == "higher"
                val gameState = game.guess(higher, deckStyle)

                if (gameState.gameOver && gameState.result != "Game over") {
                    val payout = chipsBet * gameState.multiplier
                    player.chips += payout
                    player.addBetRecord("Higher or Lower", chipsBet, payout)
                    call.sessions.set(player)
                } else if (gameState.gameOver) {
                    player.addBetRecord("Higher or Lower", chipsBet, 0)
                }

                val resultMessage = createResultMessage(
                    gameOver = gameState.gameOver,
                    isWin = gameState.result != "Game over",
                    chipsBet = chipsBet,
                    currentChips = player.chips,
                    multiplier = gameState.multiplier
                )

                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to resultMessage,
                            "sideMenuResult" to gameState.sideMenuResult,
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

        post("/casino/higherorlower/next") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null) {
                val gameState = game.nextRound(deckStyle)

                val resultMessage = createResultMessage(
                    gameOver = gameState.gameOver,
                    isWin = gameState.result != "Game over",
                    chipsBet = chipsBet,
                    currentChips = player.chips,
                    multiplier = gameState.multiplier
                )

                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to resultMessage,
                            "sideMenuResult" to gameState.sideMenuResult,
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

        post("/casino/higherorlower/allin") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null) {
                val gameState = game.allIn(deckStyle)

                var resultMessage = ""
                if (gameState.gameOver) {
                    if (gameState.result == "Game over") {
                        resultMessage = "You lost $chipsBet chips and now have ${player.chips} chips."
                    } else {
                        resultMessage = "You won ${chipsBet * gameState.multiplier} chips and now have ${player.chips} chips."
                    }
                }

                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to resultMessage,
                            "sideMenuResult" to gameState.sideMenuResult,
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
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
                player.chips -= lastBet
                call.sessions.set(player)
                val game = HigherOrLowerGame()
                higherOrLowerGames[player.name] = game
                val gameState = game.getState(deckStyle)

                var resultMessage = ""
                if (gameState.gameOver) {
                    if (gameState.result == "Game over") {
                        resultMessage = "You lost $lastBet chips and now have ${player.chips} chips."
                    } else {
                        resultMessage = "You won ${lastBet * gameState.multiplier} chips and now have ${player.chips} chips."
                    }
                }

                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to lastBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to resultMessage,
                            "sideMenuResult" to gameState.sideMenuResult,
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

        post("/casino/higherorlower/leave") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = player?.name?.let { higherOrLowerGames[it] }
            val chipsBet = player?.lastBet ?: 0
            if (player != null && game != null) {
                val gameState = game.leave(deckStyle)
                val winnings = chipsBet * gameState.multiplier
                player.chips += winnings
                player.addBetRecord("Higher or Lower", chipsBet, winnings)
                player.lastBet = null
                call.sessions.set(player)
                val resultMessage = createResultMessage(
                    gameOver = true,
                    isWin = true,
                    chipsBet = chipsBet,
                    currentChips = player.chips,
                    multiplier = gameState.multiplier
                )
                call.respond(
                    ThymeleafContent(
                        "higherorlower",
                        mapOfNonNull(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to chipsBet,
                            "playerCard" to gameState.playerCard,
                            "dealerCard" to gameState.dealerCard,
                            "showDealerCard" to gameState.showDealerCard,
                            "result" to gameState.result,
                            "resultMessage" to resultMessage,
                            "sideMenuResult" to gameState.sideMenuResult,
                            "currentRound" to gameState.currentRound,
                            "totalRounds" to gameState.totalRounds,
                            "multiplier" to gameState.multiplier,
                            "canLeaveOrAllIn" to gameState.canLeaveOrAllIn,
                            "gameOver" to gameState.gameOver,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/higherorlower")
            }
        }

        post("/casino/ridethebus/bet") {
            val player = call.sessions.get<Player>()
            val params = call.receiveParameters()
            val chipsBet = params["chipsBet"]?.toIntOrNull() ?: 0
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            if (player != null && chipsBet > 0 && player.chips >= chipsBet) {
                player.chips -= chipsBet
                player.lastBet = chipsBet
                call.sessions.set(player)
                val game = RideTheBusGame(deckStyle)
                rideTheBusGames[player.name] = game
                val gameState = game.getState(deckStyle)
                call.respond(
                    ThymeleafContent(
                        "ridethebus",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to chipsBet,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/ridethebus")
            }
        }

        post("/casino/ridethebus/guess") {
            val player = call.sessions.get<Player>()
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            val game = player?.name?.let { rideTheBusGames[it] }
            val chipsBet = player?.lastBet ?: 0
            val choice = call.receiveParameters()["choice"] ?: ""
            if (player != null && game != null) {
                val gameState = if (choice == "leave") game.leave(deckStyle) else game.guess(choice, deckStyle)
                val payout = when {
                    choice == "leave" && gameState.gameOver && gameState.multiplier >= 1 -> chipsBet * gameState.multiplier
                    gameState.gameOver && gameState.result?.contains("won") == true -> chipsBet * gameState.multiplier
                    else -> 0
                }
                if (payout > 0) {
                    player.chips += payout
                    player.addBetRecord("Ride the Bus", chipsBet, payout)
                } else if (gameState.gameOver && gameState.result?.contains("lost") == true) {
                    player.addBetRecord("Ride the Bus", chipsBet, 0)
                }
                call.sessions.set(player)
                call.respond(
                    ThymeleafContent(
                        "ridethebus",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to chipsBet,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/ridethebus")
            }
        }

        post("/casino/ridethebus/restart") {
            val player = call.sessions.get<Player>()
            val lastBet = player?.lastBet
            val deckStyle = call.sessions.get<DeckStyle>()?.style ?: "minimalista"
            if (player != null && lastBet != null && player.chips >= lastBet) {
                player.chips -= lastBet
                call.sessions.set(player)
                val game = RideTheBusGame(deckStyle)
                rideTheBusGames[player.name] = game
                val gameState = game.getState(deckStyle)
                call.respond(
                    ThymeleafContent(
                        "ridethebus",
                        mapOf(
                            "name" to player.name,
                            "chips" to player.chips,
                            "money" to player.money,
                            "chipsBet" to lastBet,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/ridethebus")
            }
        }

        post("/casino/ridethebus/newbet") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                player.lastBet = null
                call.sessions.set(player)
                call.respondRedirect("/casino/ridethebus")
            } else {
                call.respondRedirect("/")
            }
        }

        post("/casino/mines/bet") {
            val player = call.sessions.get<Player>()
            val chipsBet = call.receiveParameters()["chipsBet"]?.toIntOrNull()
            if (player != null && chipsBet != null && chipsBet > 0 && chipsBet <= player.chips) {
                player.chips -= chipsBet
                player.lastBet = chipsBet
                call.sessions.set(player)
                val minesGame = Mines(chipsBet)
                minesGames[player.name] = minesGame
                val gameState = minesGame.getState()
                call.respond(
                    ThymeleafContent(
                        "mines",
                        mapOf(
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondText("Invalid bet", status = HttpStatusCode.BadRequest)
            }
        }
        get("/casino/mines/reveal") {
            val player = call.sessions.get<Player>()
            val minesGame = player?.name?.let { minesGames[it] }
            val chipsBet = player?.lastBet ?: 0
            val row = call.parameters["row"]?.toIntOrNull()
            val col = call.parameters["col"]?.toIntOrNull()

            if (player != null && minesGame != null && row != null && col != null) {
                val gameState = minesGame.revealSquare(row, col)

                call.respond(
                    ThymeleafContent(
                        "mines",
                        mapOf(
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/mines")
            }
        }

        post("/casino/mines/cashout") {
            val player = call.sessions.get<Player>()
            val minesGame = player?.name?.let { minesGames[it] }
            val chipsBet = player?.lastBet ?: 0

            if (player != null && minesGame != null) {
                val gameState = minesGame.endGame()

                // Only pay out if game is not over due to hitting a mine
                if (!gameState.mineRevealed) {
                    player.chips += gameState.payout
                    player.addBetRecord("Mines", chipsBet, gameState.payout)
                    call.sessions.set(player)
                } else {
                    player.addBetRecord("Mines", chipsBet, 0)
                }

                call.respond(
                    ThymeleafContent(
                        "mines",
                        mapOf(
                            "name" to player.name,
                            "chipsBet" to chipsBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "gameState" to gameState,
                            "resultMessage" to "You cashed out with ${gameState.payout} chips!",
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/mines")
            }
        }

        post("/casino/mines/restart") {
            val player = call.sessions.get<Player>()
            val lastBet = player?.lastBet

            if (player != null && lastBet != null && player.chips >= lastBet) {
                player.chips -= lastBet
                call.sessions.set(player)

                val minesGame = Mines(lastBet)
                minesGames[player.name] = minesGame
                val gameState = minesGame.getState()

                call.respond(
                    ThymeleafContent(
                        "mines",
                        mapOf(
                            "name" to player.name,
                            "chipsBet" to lastBet,
                            "chips" to player.chips,
                            "money" to player.money,
                            "gameState" to gameState,
                            "playerPhoto" to getOrCreatePlayerPhoto(player.name)
                        )
                    )
                )
            } else {
                call.respondRedirect("/casino/mines")
            }
        }

        post("/casino/mines/newbet") {
            val player = call.sessions.get<Player>()
            if (player != null) {
                // Reset the last bet
                player.lastBet = null
                call.sessions.set(player)
                // Redirect to the bet placement page
                call.respondRedirect("/casino/mines")
            } else {
                call.respondRedirect("/")
            }
        }
    }
}
