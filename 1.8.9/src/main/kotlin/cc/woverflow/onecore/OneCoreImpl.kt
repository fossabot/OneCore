package cc.woverflow.onecore

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.commands.CommandRegistry
import cc.woverflow.onecore.api.events.InitializationEvent
import cc.woverflow.onecore.api.gui.ElementaHud
import cc.woverflow.onecore.api.gui.notifications.Notifications
import cc.woverflow.onecore.api.utils.ColorHelper
import cc.woverflow.onecore.api.utils.FileHelper
import cc.woverflow.onecore.api.utils.GuiHelper
import cc.woverflow.onecore.api.utils.JsonHelper
import cc.woverflow.onecore.api.utils.updater.Updater
import cc.woverflow.onecore.commands.CommandRegistryImpl
import cc.woverflow.onecore.commands.OneCoreCommand
import com.google.gson.GsonBuilder
import me.kbrewster.eventbus.*
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import okhttp3.OkHttpClient
import org.apache.logging.log4j.LogManager

@Mod(
    name = "OneCore",
    version = "__VERSION__",
    modid = "onecore",
    clientSideOnly = true
)
class OneCoreImpl : OneCore {
    private val logger = LogManager.getLogger(name())
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    private val eventBus = eventbus {  }

    private lateinit var fileHelper: FileHelper
    private lateinit var jsonHelper: JsonHelper
    private lateinit var updater: Updater
    private lateinit var guiHelper: GuiHelper
    private lateinit var elementaHud: ElementaHud
    private lateinit var notifications: Notifications
    private lateinit var commandRegistry: CommandRegistry
    private lateinit var httpClient: OkHttpClient
    private lateinit var colorHelper: ColorHelper

    override fun initialize(event: InitializationEvent) {
        MinecraftForge.EVENT_BUS.register(ForgeEventExtender())

        fileHelper = FileHelper(event.gameDir)
        jsonHelper = JsonHelper()
        updater = Updater()
        guiHelper = GuiHelper()
        elementaHud = ElementaHud().also { it.initialize() }
        notifications = Notifications()
        commandRegistry = CommandRegistryImpl().also { it.registerCommand(OneCoreCommand()) }
        httpClient = OkHttpClient()
        colorHelper = ColorHelper()
    }

    override fun logger() = logger
    override fun gson() = gson
    override fun eventBus() = eventBus

    override fun fileHelper() = fileHelper
    override fun jsonHelper() = jsonHelper
    override fun updater() = updater
    override fun guiHelper() = guiHelper
    override fun elementaHud() = elementaHud
    override fun notifications() = notifications
    override fun commandRegistry() = commandRegistry
    override fun httpClient() = httpClient
    override fun colorHelper() = colorHelper
}