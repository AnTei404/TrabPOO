package trab.casino

import trab.Player

class ExchangeLogic {
    fun exchangeMoneyForChips(player: Player, moneyToExchange: Int): Player? {
        return if (moneyToExchange > 0 && player.money >= moneyToExchange) {
            player.copy(
                money = player.money - moneyToExchange,
                chips = player.chips + (moneyToExchange * 10)
            )
        } else {
            null
        }
    }

    fun exchangeChipsForMoney(player: Player, chipsToExchange: Int): Player? {
        return if (chipsToExchange > 0 && player.chips >= chipsToExchange) {
            player.copy(
                chips = player.chips - chipsToExchange,
                money = player.money + (chipsToExchange / 10)
            )
        } else {
            null
        }
    }
}