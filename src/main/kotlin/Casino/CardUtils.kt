package trab.casino

/**
 * Generates a list of preview cards for display in the UI.
 * 
 * This function creates three sample cards (one numeric, one face card, and one ace)
 * with random suits to showcase the selected deck style.
 * 
 * @param deckStyle The visual style of the deck (e.g., "minimalista", "pixel art", "balatro")
 * @return A list of maps, each representing a card with rank, suit, and image path
 */
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

/**
 * Extension function that converts a Card object to a map with image path.
 * 
 * This function takes a Card object and adds the appropriate image path
 * based on the card's rank, suit, and the selected deck style.
 * 
 * @param deckStyle The visual style of the deck (e.g., "minimalista", "pixel art", "balatro")
 * @return A map containing the card's rank, suit, and image path
 */
fun Card.toCardWithImage(deckStyle: String): Map<String, String> {
    val rankShort = if (rank == "10") rank else rank.substring(0, 1)
    return mapOf(
        "rank" to rank,
        "suit" to suit,
        "imagePath" to "/Baralhos/$deckStyle/$rankShort-$suit.png"
    )
}

/**
 * Returns the image path for a card, with an option to show a hidden card back.
 * 
 * This function is used to get the appropriate image path for a card in games
 * where cards may be face-up or face-down (hidden).
 * 
 * @param card The Card object to get the image for, can be null
 * @param deckStyle The visual style of the deck (e.g., "minimalista", "pixel art", "balatro")
 * @param isHidden Whether the card should be displayed face-down (true) or face-up (false)
 * @return A string containing the path to the card image
 */
fun CardMatch(card: Card?, deckStyle: String, isHidden: Boolean = false): String {
    return if (isHidden) {
        "/Baralhos/CardBack.png"
    } else {
        val rankShort = if (card?.rank == "10") card.rank else card?.rank?.substring(0, 1)
        "/Baralhos/$deckStyle/$rankShort-${card?.suit}.png"
    }
}
