package jp.vcmc.welcome

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import jp.vcmc.welcome.i18n.Language
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class WelcomePlugin : JavaPlugin() {

    lateinit var messages: Messages
        private set

    @Volatile
    lateinit var settings: WelcomeSettings
        private set

    private val configLock = Any()

    override fun onEnable() {
        saveDefaultConfig()
        settings = WelcomeSettings.from(config)
        messages = Messages(this)
        messages.reload()
        server.pluginManager.registerEvents(JoinListener(this), this)
        registerCommands()
        logger.info("VCMCWelcome enabled.")
    }

    override fun onDisable() {
        logger.info("VCMCWelcome disabled.")
    }

    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal("welcome")
                    .requires { source -> PERMISSIONS.any { source.sender.hasPermission(it) } }
                    .executes { context ->
                        sendHelp(context.source.sender)
                        Command.SINGLE_SUCCESS
                    }
                    .then(
                        Commands.literal("reload").executes { context ->
                            reload(context.source.sender)
                            Command.SINGLE_SUCCESS
                        },
                    )
                    .then(
                        Commands.literal("set").then(
                            keyArgument().then(
                                Commands.argument(ARG_VALUE, StringArgumentType.greedyString()).executes { context ->
                                    set(context.source.sender, keyOf(context), StringArgumentType.getString(context, ARG_VALUE))
                                    Command.SINGLE_SUCCESS
                                },
                            ),
                        ),
                    )
                    .then(
                        Commands.literal("get").then(
                            keyArgument().executes { context ->
                                get(context.source.sender, keyOf(context))
                                Command.SINGLE_SUCCESS
                            },
                        ),
                    )
                    .build(),
                "ウェルカムメッセージの設定を変更します",
            )
        }
    }

    private fun keyArgument(): ArgumentBuilder<CommandSourceStack, *> =
        Commands.argument(ARG_KEY, StringArgumentType.word()).suggests { _, builder -> suggestKeys(builder) }

    private fun suggestKeys(builder: SuggestionsBuilder) = builder.apply {
        CONFIG_PATHS.keys.filter { it.startsWith(remainingLowerCase) }.forEach { suggest(it) }
    }.buildFuture()

    private fun keyOf(context: CommandContext<CommandSourceStack>): String =
        StringArgumentType.getString(context, ARG_KEY).lowercase()

    private fun sendHelp(sender: CommandSender) {
        messages.send(sender, "help-header")
        messages.send(sender, "help-reload")
        messages.send(sender, "help-set")
        messages.send(sender, "help-get")
        sendAvailableKeys(sender)
    }

    private fun sendAvailableKeys(sender: CommandSender) {
        messages.send(sender, "help-keys", "keys" to CONFIG_PATHS.keys.joinToString(", "))
    }

    private fun reload(sender: CommandSender) {
        synchronized(configLock) {
            reloadConfig()
            settings = WelcomeSettings.from(config)
        }
        messages.reload()
        messages.send(sender, "reloaded")
    }

    private fun set(sender: CommandSender, key: String, value: String) {
        val path = requirePath(sender, key) ?: return
        val converted: Any = when (key) {
            KEY_DELAY -> value.toIntOrNull()?.takeIf { it >= 1 } ?: run {
                messages.send(sender, "invalid-delay")
                return
            }
            KEY_URL -> value.takeIf(WelcomeSettings::isValidUrl) ?: run {
                messages.send(sender, "invalid-url")
                return
            }
            else -> value
        }
        synchronized(configLock) {
            config.set(path, converted)
            settings = WelcomeSettings.from(config)
        }
        // ファイルの書き込みでリージョンを止めない。
        server.asyncScheduler.runNow(this) { _ ->
            synchronized(configLock) { saveConfig() }
        }
        messages.send(sender, "updated", "name" to key)
        messages.send(sender, "new-value", "value" to value)
        if (key != KEY_DELAY && key != KEY_URL) messages.send(sender, "language-note")
    }

    private fun get(sender: CommandSender, key: String) {
        val path = requirePath(sender, key) ?: return
        val value = synchronized(configLock) { config.get(path) }?.toString()
            ?: messages.raw(Language.of(sender), "value-unset")
        messages.send(sender, "current-value", "name" to key, "value" to value)
    }

    private fun requirePath(sender: CommandSender, key: String): String? {
        val path = CONFIG_PATHS[key]
        if (path == null) {
            messages.send(sender, "invalid-key", "name" to key)
            sendAvailableKeys(sender)
        }
        return path
    }

    private companion object {
        const val ARG_KEY = "key"
        const val ARG_VALUE = "value"
        const val KEY_URL = "url"
        const val KEY_DELAY = "delay"

        val PERMISSIONS = listOf("vcmc.welcome.admin", "vcmcwelcome.admin")

        val CONFIG_PATHS = linkedMapOf(
            "welcome" to "messages.welcome",
            "separator" to "messages.separator",
            "url-label" to "messages.url-label",
            "url-hover" to "messages.url-hover",
            "discord-warning" to "messages.discord-warning",
            "discord-note" to "messages.discord-note",
            "menu-note" to "messages.menu-note",
            KEY_URL to "url",
            KEY_DELAY to "delay-ticks",
        )
    }
}
