package trab

import kotlin.random.Random

/**
 * Filters out null values from a list of key-value pairs and returns a map with non-null values.
 * 
 * @param pairs Variable number of key-value pairs where values might be null
 * @return A map containing only the key-value pairs where the value is not null
 */
fun mapOfNonNull(vararg pairs: Pair<String, Any?>): Map<String, Any> =
    pairs.filter { it.second != null }.associate { it.first to it.second as Any }

/**
 * Static map to store player photos by name.
 * This map maintains the association between player names and their selected photo paths.
 */
private val playerPhotos = mutableMapOf<String, String>()

/**
 * Removes a player's photo from the photo storage.
 * 
 * @param name The name of the player whose photo should be removed
 */
fun removePlayerPhoto(name: String) {
    playerPhotos.remove(name)
}

/**
 * Returns a list of all available player avatar images.
 * 
 * @return List of file paths to available player images
 */
fun getAllPlayerImages(): List<String> {
    return listOf(
        "/Player/Spongebob.jpg",
        "/Player/Forgor.jpg",
        "/Player/Peta.jpg",
        "/Player/House.png",
        "/Player/cat.png",
        "/Player/crash.png",
        "/Player/Bugs.png",
        "/Player/BugsGun.png"
    )
}

/**
 * Generates HTML for a photo selection grid with the specified photo pre-selected.
 * 
 * @param photoPath The path of the currently selected photo
 * @param allPhotos List of all available photos to display in the grid
 * @return HTML string representing the photo selection grid
 */
fun selectPhoto(photoPath: String, allPhotos: List<String> = getAllPlayerImages()): String {
    val sb = StringBuilder()
    sb.append("<div class=\"photo-grid\">")

    for (photo in allPhotos) {
        val photoName = photo.substringAfterLast("/").substringBeforeLast(".")
        val isSelected = photo == photoPath
        val selectedClass = if (isSelected) " selected" else ""

        sb.append("<div class=\"photo-option${selectedClass}\" onclick=\"document.getElementById('selectedPhoto').value='${photo}'; this.closest('.photo-grid').querySelectorAll('.photo-option').forEach(el => el.classList.remove('selected')); this.classList.add('selected');\" data-photo=\"${photo}\">")
        sb.append("<img src=\"${photo}\" alt=\"${photoName}\">")
        sb.append("<div class=\"photo-label\">${photoName}</div>")
        sb.append("</div>")
    }

    sb.append("</div>")
    return sb.toString()
}

/**
 * Associates a specific photo with a player.
 * 
 * @param name The name of the player
 * @param photoPath The path to the selected photo
 */
fun setPlayerPhoto(name: String, photoPath: String) {
    playerPhotos[name] = photoPath
}

/**
 * Gets a player's photo or assigns a random one if none exists.
 * 
 * @param name The name of the player
 * @return The path to the player's photo
 */
fun getOrCreatePlayerPhoto(name: String): String {
    return playerPhotos.getOrPut(name) { 
        val playerImages = getAllPlayerImages()
        playerImages[Random.nextInt(playerImages.size)]
    }
}

/**
 * Creates a standardized result message for casino games
 * 
 * @param gameOver Whether the game is over
 * @param isWin Whether the player won (true) or lost (false)
 * @param chipsBet The amount of chips bet
 * @param currentChips The player's current chip balance
 * @param multiplier Optional multiplier for win calculation (default is 1)
 * @return A formatted result message string
 */
fun createResultMessage(
    gameOver: Boolean,
    isWin: Boolean,
    chipsBet: Int,
    currentChips: Int,
    multiplier: Int = 1
): String {
    if (!gameOver) return ""

    return if (isWin) {
        "You won ${chipsBet * multiplier} chips and now have $currentChips chips."
    } else {
        "You lost $chipsBet chips and now have $currentChips chips."
    }
}
