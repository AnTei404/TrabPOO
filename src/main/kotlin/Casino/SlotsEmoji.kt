package trab.casino

/**
 * Converts a slot machine symbol name to its corresponding emoji representation.
 * 
 * This function maps common slot machine symbols to their visual emoji equivalents
 * for display in the UI. If an unknown symbol is provided, a question mark emoji is returned.
 * 
 * @param symbol The name of the slot machine symbol
 * @return The emoji representation of the symbol
 */
fun symbolToEmoji(symbol: String): String = when (symbol) {
    "Cherry" -> "🍒"
    "Lemon" -> "🍋"
    "Orange" -> "🍊"
    "Bell" -> "🔔"
    "Seven" -> "7️⃣"
    "Mine" -> "💣"
    else -> "❓"
}
