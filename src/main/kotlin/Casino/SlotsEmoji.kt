package trab.casino

fun symbolToEmoji(symbol: String): String = when (symbol) {
    "Cherry" -> "🍒"
    "Lemon" -> "🍋"
    "Orange" -> "🍊"
    "Bell" -> "🔔"
    "Seven" -> "7️⃣"
    "Mine" -> "💣"
    else -> "⬜"
}
