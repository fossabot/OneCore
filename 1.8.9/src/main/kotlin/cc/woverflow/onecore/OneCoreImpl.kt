package cc.woverflow.onecore

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.events.InitializationEvent
import me.kbrewster.eventbus.eventbus
import okhttp3.OkHttpClient
import org.apache.logging.log4j.LogManager

class OneCoreImpl : OneCore {
    private val logger = LogManager.getLogger(name())
    private val eventBus = eventbus {  }

    private lateinit var httpClient: OkHttpClient

    override fun initialize(event: InitializationEvent) {
        httpClient = OkHttpClient()
        println("Started OneCore!")
    }

    override fun logger() = logger
    override fun eventBus() = eventBus

    override fun httpClient() = httpClient
}