package co.ethanlibs.wcore

import co.ethanlibs.wcore.command.WCoreCommand
import co.ethanlibs.wcore.config.WCoreConfig
import co.ethanlibs.wcore.updater.Updater
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.io.File

@Mod(
    modid = WCore.ID,
    name = WCore.NAME,
    version = WCore.VER,
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
)
object WCore {

    const val NAME = "@NAME@"
    const val VER = "@VER@"
    const val ID = "@ID@"
    lateinit var jarFile: File
        private set

    val modDir = File(File(Minecraft.getMinecraft().mcDataDir, "W-OVERFLOW"), NAME)

    @Mod.EventHandler
    fun onFMLPreInitialization(event: FMLPreInitializationEvent) {
        if (!modDir.exists()) modDir.mkdirs()
        jarFile = event.sourceFile
    }

    @Mod.EventHandler
    fun onInitialization(event: FMLInitializationEvent) {
        WCoreConfig.preload()
        WCoreCommand.register()
        Updater.update()
    }
}