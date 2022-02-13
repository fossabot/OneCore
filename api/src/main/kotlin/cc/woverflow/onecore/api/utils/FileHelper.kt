package cc.woverflow.onecore.api.utils

import java.io.File

class FileHelper(
    val gameDir: File
) {
    val dataDir: File
        get() = File(gameDir, "OneCore")
}