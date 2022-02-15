package cc.woverflow.onecore.api.utils

import cc.woverflow.onecore.api.OneCore
import com.google.gson.JsonElement
import gg.essential.universal.UDesktop
import gg.essential.universal.UGraphics
import net.minecraft.client.gui.GuiScreen
import java.net.URI

/**
 * @return A changing colour based on the users' computer time. Simulates a "chroma" colour.
 */
fun timeBasedChroma() = OneCore.getColorUtils().timeBasedChroma()

/**
 * @return The red value of the provided RGBA value.
 */
fun Int.getRed() = OneCore.getColorUtils().getRed(this)

/**
 * @return The green value of the provided RGBA value.
 */
fun Int.getGreen() = OneCore.getColorUtils().getGreen(this)

/**
 * @return The blue value of the provided RGBA value.
 */
fun Int.getBlue() = OneCore.getColorUtils().getBlue(this)

/**
 * @return The alpha value of the provided RGBA value.
 */
fun Int.getAlpha() = OneCore.getColorUtils().getAlpha(this)

/**
 * Open a website URL in the user's web browser.
 * @param url website URL
 * @return Whether opening succeeded.
 */
fun UDesktop.browseURL(url: String): Boolean = browse(URI.create(url))

/**
 * Return the string provided as a [JsonElement].
 */
fun String.asJsonElementSafe(): Result<JsonElement> = runCatching { return@runCatching this.asJsonElement() }

/**
 * Return the string provided as a [JsonElement]
 *
 * @return string as a [JsonElement]
 */
fun String.asJsonElement(): JsonElement = OneCore.getJsonHelper().parse(this)

//#if MC<=11202
/**
 * Queue a new screen for opening.
 * @see GuiHelper.showScreen
 */
fun GuiScreen.showScreen() = OneCore.getGuiHelper().showScreen(this)
//#endif

inline fun <T, R> T.newMatrix(block: T.() -> R) {
    UGraphics.GL.pushMatrix()
    block.invoke(this)
    UGraphics.GL.popMatrix()
}

inline fun <T, R> T.withColor(rgba: Int, block: T.() -> R) = this.newMatrix {
    UGraphics.color4f(
        rgba.getRed().toFloat(),
        rgba.getGreen().toFloat(),
        rgba.getBlue().toFloat(),
        rgba.getAlpha().toFloat()
    )
    block.invoke(this)
}

inline fun <T, R> T.withScale(xScale: Float, yScale: Float, zScale: Float, block: T.() -> R) = this.newMatrix {
    UGraphics.GL.translate(xScale, yScale, zScale)
    block.invoke(this)
}

inline fun <T, R> T.withTranslate(x: Float, y: Float, z: Float, block: T.() -> R) = this.newMatrix {
    UGraphics.GL.translate(x, y, z)
    block.invoke(this)
}

inline fun <T, R> T.withRotation(angle: Float, x: Float, y: Float, z: Float, block: T.() -> R) = this.newMatrix {
    UGraphics.GL.rotate(angle, x, y, z)
    block.invoke(this)
}