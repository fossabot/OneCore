package cc.woverflow.onecore.commands

import cc.woverflow.onecore.api.commands.annotations.*

@Command(
    name = "onecore"
) class OneCoreCommand {
    @Default
    private fun execute() {
        println("TEST EXECUTION")
        // TODO 2022/02/14 - OneCore.getGuiHelper().showScreen(OneCoreScreen())
    }
}