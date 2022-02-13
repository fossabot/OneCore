package cc.woverflow.onecore.api.utils.updater

//#if MC>=10809
//$$ import net.minecraftforge.fml.common.Mod
//#endif
import cc.woverflow.onecore.api.OneCore
import gg.essential.universal.UDesktop
import okhttp3.OkHttpClient
import xyz.deftu.deftils.Multithreading
import java.io.File

class Updater {
    private val mods = mutableListOf<UpdaterMod>()
    private val outdated = mutableListOf<UpdaterMod>()
    lateinit var httpClient: OkHttpClient
        private set

    fun initialize() {
        httpClient = OkHttpClient.Builder()
            .cache(null)
            .addInterceptor { it.proceed(it.request().newBuilder().header("User-Agent", "Mozilla 4.76 (${OneCore.getName()}/${OneCore.getVersion()})").build()) }
            .build()
    }

    fun include(name: String, version: String, id: String, path: String, fetcher: UpdateFetcher, file: File) = mods.add(UpdaterMod(name, version, id, path, fetcher, file))

    fun check() {
        val outdated = mutableListOf<UpdaterMod>()
        for (mod in mods) {
            Multithreading.runAsync {
                mod.fetcher.check(this, mod)
                if (mod.fetcher.hasUpdate()) {

                }
            }

            // TODO 2022/02/13 - OneCore.getGuiHelper().showScreen(ModUpdateListScreen(outdated))
            // TODO 2022/02/13 - INSIDE THE MOD UPDATE LIST. - OneCore.getGuiHelper().showScreen(ModUpdateScreen(mod))
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

data class UpdaterMod(
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