package cc.woverflow.onecore.api

import cc.woverflow.onecore.api.events.InitializationEvent
import cc.woverflow.onecore.api.gui.ElementaHud
import cc.woverflow.onecore.api.gui.notifications.Notifications
import me.kbrewster.eventbus.EventBus
import me.kbrewster.eventbus.Subscribe
import okhttp3.OkHttpClient
import org.apache.logging.log4j.Logger
import java.util.*

interface OneCore {
    fun initialize(event: InitializationEvent)

    fun version() = "@VERSION@"
    fun name() = "@NAME@"
    fun id() = "@ID@"

    fun logger(): Logger
    fun eventBus(): EventBus

    fun elementaHud(): ElementaHud
    fun notifications(): Notifications
    fun httpClient(): OkHttpClient

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
        @JvmStatic fun getEventBus() = instance.eventBus()

        @JvmStatic fun getElementaHud() = instance.elementaHud()
        @JvmStatic fun getNotifications() = instance.notifications()
        @JvmStatic fun getHttpClient() = instance.httpClient()
    }
}