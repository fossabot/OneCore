package cc.woverflow.onecore.commands

import cc.woverflow.onecore.api.OneCore
import cc.woverflow.onecore.api.commands.BaseCommand
import cc.woverflow.onecore.api.commands.CommandRegistry
import cc.woverflow.onecore.api.commands.arguments.*
import cc.woverflow.onecore.api.events.ChatSendEvent
import me.kbrewster.eventbus.Subscribe

class CommandRegistryImpl : CommandRegistry {
    override val argumentSerializers = mutableMapOf<Class<*>, ArgumentSerializer<*>>()
    override val commands = mutableMapOf<String, BaseCommand>()

    init {
        OneCore.getEventBus().register(this)

        argumentSerializers[Boolean::class.java] = BooleanArgumentSerializer()
        argumentSerializers[Double::class.java] = DoubleArgumentSerializer()
        argumentSerializers[Float::class.java] = FloatArgumentSerializer()
        argumentSerializers[Int::class.java] = IntArgumentSerializer()
        argumentSerializers[String::class.java] = StringArgumentSerializer()
        argumentSerializers[ArgumentQueue::class.java] = ArgumentsQueueSerializer()
    }

    override fun registerCommand(command: BaseCommand) {
        commands[command.name] = command
        command.aliases.forEach { commands[it] = command }
    }

    override fun <T> registerArgumentParser(type: Class<T>, parser: ArgumentSerializer<T>) {
        argumentSerializers[type] = parser
    }

    @Subscribe private fun onChatSent(event: ChatSendEvent) {
        var message = event.message.trim()
        if (!message.startsWith("/")) return

        message = message.replaceFirst("/", "")
        val split = message.split(" ")
        val name = split[0]
        if (!commands.containsKey(name)) return

        val args = split.subList(1, split.size)
        val command = commands[name]!!
        command.execute(args.toList())
        event.cancelled = true
    }
}