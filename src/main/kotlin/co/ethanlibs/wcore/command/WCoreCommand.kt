package co.ethanlibs.wcore.command

import co.ethanlibs.wcore.WCore
import co.ethanlibs.wcore.config.WCoreConfig
import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler

object WCoreCommand : Command(WCore.ID, true) {

    @DefaultHandler
    fun handle() {
        EssentialAPI.getGuiUtil().openScreen(WCoreConfig.gui())
    }
}