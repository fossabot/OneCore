package cc.woverflow.onecore.api.commands.arguments

import cc.woverflow.onecore.api.commands.annotations.Greedy
import java.lang.reflect.Parameter

class StringArgumentSerializer : ArgumentSerializer<String> {
    override fun parse(queue: ArgumentQueue, parameter: Parameter): String {
        return if (parameter.isAnnotationPresent(Greedy::class.java)) {
            val compiled = mutableListOf<String>()
            while (!queue.isEmpty()) compiled.add(queue.poll())
            compiled.joinToString(" ")
        } else queue.poll()
    }
}