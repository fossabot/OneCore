package cc.woverflow.onecore

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.events.InitializationEvent
import cc.woverflow.onecore.api.gui.ElementaHud
import cc.woverflow.onecore.api.gui.notifications.Notifications
import cc.woverflow.onecore.api.utils.FileHelper
import cc.woverflow.onecore.api.utils.GuiHelper
import cc.woverflow.onecore.api.utils.JsonHelper
import cc.woverflow.onecore.api.utils.updater.Updater
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

    private lateinit var fileHelper: FileHelper
    private lateinit var jsonHelper: JsonHelper
    private lateinit var updater: Updater
    private lateinit var guiHelper: GuiHelper
    private lateinit var elementaHud: ElementaHud
    private lateinit var notifications: Notifications
    private lateinit var httpClient: OkHttpClient

    override fun initialize(event: InitializationEvent) {
        MinecraftForge.EVENT_BUS.register(ForgeEventExtender())

        fileHelper = FileHelper(event.gameDir)
        jsonHelper = JsonHelper()
        updater = Updater()
        guiHelper = GuiHelper()
        elementaHud = ElementaHud().also { it.initialize() }
        notifications = Notifications()
        httpClient = OkHttpClient()
    }

    override fun logger() = logger
    override fun eventBus() = eventBus

    override fun fileHelper() = fileHelper
    override fun jsonHelper() = jsonHelper
    override fun updater() = updater
    override fun guiHelper() = guiHelper
    override fun elementaHud() = elementaHud
    override fun notifications() = notifications
    override fun httpClient() = httpClient
}