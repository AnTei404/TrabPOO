package trab

import kotlin.random.Random

fun mapOfNonNull(vararg pairs: Pair<String, Any?>): Map<String, Any> =
    pairs.filter { it.second != null }.associate { it.first to it.second as Any }

private val playerPhotos = mutableMapOf<String, String>()

fun removePlayerPhoto(name: String) {
    playerPhotos.remove(name)
}


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

fun setPlayerPhoto(name: String, photoPath: String) {
    playerPhotos[name] = photoPath
}

fun getOrCreatePlayerPhoto(name: String): String {
    return playerPhotos.getOrPut(name) { 
        val playerImages = getAllPlayerImages()
        playerImages[Random.nextInt(playerImages.size)]
    }
}

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
