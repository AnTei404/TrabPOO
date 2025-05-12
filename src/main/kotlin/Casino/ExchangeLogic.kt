package trab.casino

import trab.Player

class ExchangeLogic {
    fun exchangeMoneyForChips(player: Player, moneyToExchange: Double): Player? {
        return if (moneyToExchange > 0 && player.money >= moneyToExchange) {
            player.copy(
                money = player.money - moneyToExchange,
                chips = player.chips + (moneyToExchange * 10).toInt() // Example: 1 money = 10 chips
            )
        } else {
            null
        }
    }

    fun exchangeChipsForMoney(player: Player, chipsToExchange: Int): Player? {
        return if (chipsToExchange > 0 && player.chips >= chipsToExchange) {
            player.copy(
                chips = player.chips - chipsToExchange,
                money = player.money + (chipsToExchange / 10.0) // Example: 10 chips = 1 money
            )
        } else {
            null
        }
    }
}