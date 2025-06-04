package trab.casino

import trab.Player

class ExchangeLogic {
    fun exchangeMoneyForChips(player: Player, moneyToExchange: Int): Player? {
        return if (moneyToExchange > 0 && player.money >= moneyToExchange) {
            player.money -= moneyToExchange
            player.chips += (moneyToExchange * 10)
            player
        } else {
            null
        }
    }

    fun exchangeChipsForMoney(player: Player, chipsToExchange: Int): Player? {
        return if (chipsToExchange > 0 && player.chips >= chipsToExchange) {
            player.chips -= chipsToExchange
            player.money += (chipsToExchange / 10)
            player
        } else {
            null
        }
    }
}
