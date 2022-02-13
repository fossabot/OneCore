package cc.woverflow.onecore

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.events.InitializationEvent
import cc.woverflow.onecore.api.gui.ElementaHud
import cc.woverflow.onecore.api.gui.notifications.Notifications
import me.kbrewster.eventbus.*
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import okhttp3.OkHttpClient
import org.apache.logging.log4j.LogManager

@Mod(
    name = "OneCore",
    version = "1.0.0",
    modid = "onecore",
    clientSideOnly = true
)
class OneCoreImpl : OneCore {
    private val logger = LogManager.getLogger(name())
    private val eventBus = eventbus {  }

    private lateinit var httpClient: OkHttpClient
    private lateinit var notifications: Notifications
    private lateinit var elementaHud: ElementaHud

    override fun initialize(event: InitializationEvent) {
        MinecraftForge.EVENT_BUS.register(ForgeEventExtender())

        elementaHud = ElementaHud().also { it.initialize() }
        notifications = Notifications()
        httpClient = OkHttpClient()
    }

    override fun logger() = logger
    override fun eventBus() = eventBus

    override fun elementaHud() = elementaHud
    override fun notifications() = notifications
    override fun httpClient() = httpClient
}