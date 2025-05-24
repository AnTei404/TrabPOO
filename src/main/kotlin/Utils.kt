package trab

import kotlin.random.Random

fun mapOfNonNull(vararg pairs: Pair<String, Any?>): Map<String, Any> =
    pairs.filter { it.second != null }.associate { it.first to it.second as Any }

// Static map to store player photos by name
private val playerPhotos = mutableMapOf<String, String>()

// Function to remove a player's photo from the map
fun removePlayerPhoto(name: String) {
    playerPhotos.remove(name)
}

fun getRandomPlayerImage(): String {
    val playerImages = listOf(
        "/Player/Spongebob.jpg",
        "/Player/Forgor.jpg",
        "/Player/Peta.jpg",
        "Player/House.jpg",
        "/Player/cat.png",
        "/Player/crash.png",
        "/Player/Bugs.png",
        "/Player/BugsGun.png"
    )
    return playerImages[Random.nextInt(playerImages.size)]
}

// Get all available player images for selection
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

// Function to select a photo and return HTML for the photo grid
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

// Set a specific player photo
fun setPlayerPhoto(name: String, photoPath: String) {
    playerPhotos[name] = photoPath
}

fun getOrCreatePlayerPhoto(name: String): String {
    return playerPhotos.getOrPut(name) { getRandomPlayerImage() }
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
