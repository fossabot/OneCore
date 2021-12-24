package co.ethanlibs.wcore.config

import co.ethanlibs.wcore.WCore
import co.ethanlibs.wcore.updater.DownloadGui
import co.ethanlibs.wcore.updater.Updater
import gg.essential.api.EssentialAPI
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import java.io.File

object WCoreConfig : Vigilant(File(WCore.modDir, "${WCore.ID}.toml"), WCore.NAME) {

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Update Notification",
        description = "Show a notification when you start Minecraft informing you of new updates.",
        category = "Updater"
    )
    var showUpdate = true

    @Property(
        type = PropertyType.BUTTON,
        name = "Update Now",
        description = "Update by clicking the button.",
        category = "Updater"
    )
    fun update() {
        if (Updater.shouldUpdate) EssentialAPI.getGuiUtil()
            .openScreen(DownloadGui()) else EssentialAPI.getNotifications()
            .push(
                WCore.NAME,
                "No update had been detected at startup, and thus the update GUI has not been shown."
            )
    }

    init {
        initialize()
    }
}