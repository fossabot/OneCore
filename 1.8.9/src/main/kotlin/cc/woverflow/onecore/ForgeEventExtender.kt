package cc.woverflow.onecore

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.events.*
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class ForgeEventExtender {
    @SubscribeEvent fun onRenderTick(event: TickEvent.RenderTickEvent) {
        OneCore.getEventBus().post(RenderTickEvent())
    }

    @SubscribeEvent fun onMouseInput(event: InputEvent.MouseInputEvent) {
        val isScrolled = Mouse.getEventDWheel() != 0
        if (isScrolled) {
            OneCore.getEventBus().post(MouseScrollEvent(Mouse.getEventDWheel().toDouble()))
        } else {
            val isButton = Mouse.getEventButton() != 0
            if (isButton) {
                OneCore.getEventBus().post(MouseClickEvent(Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventX().toDouble(), Mouse.getEventY().toDouble()))
            } else OneCore.getEventBus().post(MouseMoveEvent(Mouse.getEventX().toDouble(), Mouse.getEventY().toDouble()))
        }
    }

    @SubscribeEvent fun onKeyboardInput(event: InputEvent.KeyInputEvent) {
        if (Keyboard.getEventKey() == Keyboard.KEY_H)
            OneCore.getNotifications().post("OneCore", "Hello, World!")
        OneCore.getEventBus().post(KeyboardInputEvent(Keyboard.getEventCharacter(), Keyboard.getEventKey()))
    }
}