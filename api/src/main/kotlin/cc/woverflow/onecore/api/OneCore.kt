package cc.woverflow.onecore.api

import cc.woverflow.onecore.api.commands.CommandRegistry
import cc.woverflow.onecore.api.events.InitializationEvent
import cc.woverflow.onecore.api.gui.ElementaHud
import cc.woverflow.onecore.api.gui.notifications.Notifications
import cc.woverflow.onecore.api.utils.*
import cc.woverflow.onecore.api.utils.http.HttpRequester
import cc.woverflow.onecore.api.utils.updater.Updater
import com.google.gson.Gson
import me.kbrewster.eventbus.EventBus
import me.kbrewster.eventbus.Subscribe
import org.apache.logging.log4j.Logger
import java.util.*

interface OneCore {
    fun initialize(event: InitializationEvent)

    fun version() = "__VERSION__"
    fun name() = "OneCore"
    fun id() = "onecore"

    fun logger(): Logger
    fun gson(): Gson
    fun eventBus(): EventBus

    fun fileHelper(): FileHelper
    fun jsonHelper(): JsonHelper
    fun updater(): Updater
    fun guiHelper(): GuiHelper
    fun elementaHud(): ElementaHud
    fun notifications(): Notifications
    fun commandRegistry(): CommandRegistry
    fun httpRequester(): HttpRequester
    fun internetHelper(): InternetHelper
    fun colorHelper(): ColorHelper

    companion object {
        var initialized = false
            @JvmStatic get
            private set
        lateinit var instance: OneCore
            private set

        @JvmStatic fun initialize(): Boolean {
            return if (!initialized) {
                val service = ServiceLoader.load(OneCore::class.java)
                val iterator = service.iterator()
                if (iterator.hasNext()) {
                    instance = iterator.next()
                    if (iterator.hasNext()) throw IllegalStateException("There is more than one implementation, this is not supported.")
                } else throw IllegalStateException("Couldn't find implementation.")
                instance.eventBus().register(this)
                true.also { initialized = true }
            } else false
        }

        @Subscribe private fun onInitialize(event: InitializationEvent) = instance.initialize(event)

        @JvmStatic fun getVersion() = instance.version()
        @JvmStatic fun getName() = instance.name()
        @JvmStatic fun getId() = instance.id()

        @JvmStatic fun getLogger() = instance.logger()
        @JvmStatic fun getGson() = instance.gson()
        @JvmStatic fun getEventBus() = instance.eventBus()

        @JvmStatic fun getFileHelper() = instance.fileHelper()
        @JvmStatic fun getJsonHelper() = instance.jsonHelper()
        @JvmStatic fun getUpdater() = instance.updater()
        @JvmStatic fun getGuiHelper() = instance.guiHelper()
        @JvmStatic fun getElementaHud() = instance.elementaHud()
        @JvmStatic fun getCommandRegistry() = instance.commandRegistry()
        @JvmStatic fun getNotifications() = instance.notifications()
        @JvmStatic fun getHttpRequester() = instance.httpRequester()
        @JvmStatic fun getInternetHelper() = instance.internetHelper()
        @JvmStatic fun getColorHelper() = instance.colorHelper()
    }
}