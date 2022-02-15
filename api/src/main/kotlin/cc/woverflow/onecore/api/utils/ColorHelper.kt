package cc.woverflow.onecore.api.utils

import cc.woverflow.onecore.api.OneCore
import java.awt.Color

class ColorHelper {

    /**
     * @return A changing colour based on the users' computer time. Simulates a "chroma" colour.
     */
    fun getChroma() = Color.HSBtoRGB(System.currentTimeMillis() % 2000L / 2000.0f, 1.0f, 1.0f)

    /**
     * @return The red value of the provided RGBA value.
     */
    fun getRed(rgba: Int) = (rgba shr 16) and 0xFF

    /**
     * @return The green value of the provided RGBA value.
     */
    fun getGreen(rgba: Int) = (rgba shr 8) and 0xFF

    /**
     * @return The blue value of the provided RGBA value.
     */
    fun getBlue(rgba: Int) = (rgba shr 0) and 0xFF

    /**
     * @return The alpha value of the provided RGBA value.
     */
    fun getAlpha(rgba: Int) = (rgba shr 24) and 0xFF
}

/**
 * @return The red value of the provided RGBA value.
 */
fun Int.getRed() = OneCore.getColorHelper().getRed(this)

/**
 * @return The green value of the provided RGBA value.
 */
fun Int.getGreen() = OneCore.getColorHelper().getGreen(this)

/**
 * @return The blue value of the provided RGBA value.
 */
fun Int.getBlue() = OneCore.getColorHelper().getBlue(this)

/**
 * @return The alpha value of the provided RGBA value.
 */
fun Int.getAlpha() = OneCore.getColorHelper().getAlpha(this)