package cc.woverflow.onecore.api.utils

import java.awt.Color

class ColorHelper {

    /**
     * @return A changing colour based on the users' computer time. Simulates a "chroma" colour.
     */
    fun getChroma(): Int {
        val l = System.currentTimeMillis()
        return Color.HSBtoRGB(l % 2000L / 2000.0f, 1.0f, 1.0f)
    }

    /**
     * @return The red value of the provided RGBA value.
     */
    fun getRed(rgba: Int): Int {
        return (rgba shr 16) and 0xFF
    }

    /**
     * @return The green value of the provided RGBA value.
     */
    fun getGreen(rgba: Int): Int {
        return (rgba shr 8) and 0xFF
    }

    /**
     * @return The blue value of the provided RGBA value.
     */
    fun getBlue(rgba: Int): Int {
        return (rgba shr 0) and 0xFF
    }

    /**
     * @return The alpha value of the provided RGBA value.
     */
    fun getAlpha(rgba: Int): Int {
        return (rgba shr 24) and 0xFF
    }
}