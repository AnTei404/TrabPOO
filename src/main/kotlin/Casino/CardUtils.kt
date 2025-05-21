package trab.casino

fun generatePreviewCards(deckStyle: String): List<Map<String, String>> {
    val suits = listOf("Clubs", "Diamonds", "Hearts", "Spades")
    val ranks = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10")
    val faceCards = listOf("J", "Q", "K")
    val ace = "A"

    return listOf(
        mapOf(
            "rank" to ranks.random(),
            "suit" to suits.random(),
            "imagePath" to "/Baralhos/$deckStyle/${ranks.random()}-${suits.random()}.png"
        ),
        mapOf(
            "rank" to faceCards.random(),
            "suit" to suits.random(),
            "imagePath" to "/Baralhos/$deckStyle/${faceCards.random()}-${suits.random()}.png"
        ),
        mapOf(
            "rank" to ace,
            "suit" to suits.random(),
            "imagePath" to "/Baralhos/$deckStyle/$ace-${suits.random()}.png"
        )
    )
}

// Change this function:
fun Card.toCardWithImage(deckStyle: String): Map<String, String> {
    val rankShort = if (rank == "10") rank else rank.substring(0, 1)
    return mapOf(
        "rank" to rank,
        "suit" to suit,
        "imagePath" to "/Baralhos/$deckStyle/$rankShort-$suit.png"
    )
}

fun CardMatch(card: Card?, deckStyle: String, isHidden: Boolean = false): String {
    return if (isHidden) {
        "/Baralhos/CardBack.png"
    } else {
        val rankShort = if (card?.rank == "10") card.rank else card?.rank?.substring(0, 1)
        "/Baralhos/$deckStyle/$rankShort-${card?.suit}.png"
    }
}