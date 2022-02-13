package cc.woverflow.onecore.api.utils.updater

//#if MC>=10809
//$$ import net.minecraftforge.fml.common.Mod
//#endif
import cc.woverflow.onecore.api.OneCore
import gg.essential.universal.UDesktop
import xyz.deftu.deftils.Multithreading
import java.io.File

class Updater {
    private val mods = mutableListOf<UpdaterMod>()
    private val outdated = mutableListOf<UpdaterMod>()

    fun include(name: String, version: String, id: String, path: String, fetcher: UpdateFetcher, file: File) = mods.add(UpdaterMod(name, version, id, path, fetcher, file))

    fun check() {
        for (mod in mods) {
            Multithreading.runAsync {
                mod.fetcher.check()
                if (mod.fetcher.hasUpdate()) {
                    OneCore.getNotifications().post(
                        OneCore.getName(),
                        "${mod.name} has an update available. Click to check."
                    ) {
                        // TODO - OneCore.getGuiHelper().showScreen(ModUpdateScreen(mod))
                    }
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(Thread({
            var changes = false
            val arguments = StringBuilder()
            for (mod in outdated) {
                if (mod.allowedUpdate) {
                    try {
                        if (System.getProperty("os.name").lowercase().contains("mac")) {
                            val sipStatus = Runtime.getRuntime().exec("csrutil status")
                            sipStatus.waitFor()
                            if (!sipStatus.inputStream.use { it.bufferedReader().readText() }
                                    .contains("System Integrity Protection status: disabled.")) {
                                UDesktop.open(mod.file.parentFile)
                            }
                        }

                        arguments.append("--file ${mod.file.absolutePath}")
                        changes = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (changes) {
                val deleterFile = File(OneCore.getFileHelper().dataDir, "Deleter.jar")
                if (UDesktop.isLinux) Runtime.getRuntime().exec("chmod +x \"${deleterFile.absolutePath}\"")
                if (UDesktop.isMac) Runtime.getRuntime().exec("chmod 755 \"${deleterFile.absolutePath}\"")
                Runtime.getRuntime().exec("java -jar ${deleterFile.name} $arguments", null, deleterFile.parentFile)
            }
        }, "${OneCore.getName()} Updater Deletion Thread"))
    }
}

internal data class UpdaterMod(
    val name: String,
    val version: String,
    val id: String,
    val path: String,
    val fetcher: UpdateFetcher,
    val file: File
) {
    var allowedUpdate = false
}

//#if MC>=10809
//$$ fun fromForge(
//$$     clz: Class<*>,
//$$     path: String,
//$$     file: File
//$$ ) {
//$$     if (clz.isAnnotationPresent(Mod::class.java)) {
//$$         val mod = clz.getAnnotation(Mod::class.java)
//$$         return UpdaterMod(mod.name, mod.version, mod.modid, path, file)
//$$     }
//$$ }
//#endif