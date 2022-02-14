package cc.woverflow.onecore.api.commands.arguments

import java.lang.reflect.Parameter

class IntArgumentSerializer : ArgumentSerializer<Int> {
    override fun parse(queue: ArgumentQueue, parameter: Parameter): Int {
        return java.lang.Integer.parseInt(queue.poll())
    }
}