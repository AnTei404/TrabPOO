package trab.casino

import trab.Player

/**
 * Handles the exchange of money and chips in the casino.
 * 
 * This class provides methods for converting between real money and casino chips,
 * applying the appropriate exchange rates.
 */
class ExchangeLogic {
    /**
     * Converts a player's money to casino chips.
     * 
     * This method applies an exchange rate of 1:10 (1 unit of money = 10 chips).
     * The exchange only occurs if the player has sufficient money.
     * 
     * @param player The player making the exchange
     * @param moneyToExchange The amount of money to convert to chips
     * @return The updated Player object if the exchange was successful, null otherwise
     */
    fun exchangeMoneyForChips(player: Player, moneyToExchange: Int): Player? {
        return if (moneyToExchange > 0 && player.money >= moneyToExchange) {
            player.money -= moneyToExchange
            player.chips += (moneyToExchange * 10)
            player
        } else {
            null
        }
    }

    /**
     * Converts a player's casino chips back to money.
     * 
     * This method applies an exchange rate of 10:1 (10 chips = 1 unit of money).
     * The exchange only occurs if the player has sufficient chips.
     * 
     * @param player The player making the exchange
     * @param chipsToExchange The amount of chips to convert to money
     * @return The updated Player object if the exchange was successful, null otherwise
     */
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
