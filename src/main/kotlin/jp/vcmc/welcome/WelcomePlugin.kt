package jp.vcmc.welcome

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class WelcomePlugin : JavaPlugin() {

    val miniMessage: MiniMessage = MiniMessage.miniMessage()

    @Volatile
    lateinit var settings: WelcomeSettings
        private set

    private val configLock = Any()

    override fun onEnable() {
        saveDefaultConfig()
        settings = WelcomeSettings.from(config)
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
        sender.sendMessage(Component.text("==== VCMCWelcome ヘルプ ====", NamedTextColor.GOLD))
        sender.sendMessage(helpLine("/welcome reload", "設定を再読み込み"))
        sender.sendMessage(helpLine("/welcome set <キー> <値>", "設定を変更"))
        sender.sendMessage(helpLine("/welcome get <キー>", "設定値を確認"))
        sender.sendMessage(Component.text("利用可能なキー: " + CONFIG_PATHS.keys.joinToString(", "), NamedTextColor.GRAY))
    }

    private fun helpLine(command: String, description: String): Component =
        Component.text(command, NamedTextColor.WHITE).append(Component.text(" - $description", NamedTextColor.GRAY))

    private fun reload(sender: CommandSender) {
        synchronized(configLock) {
            reloadConfig()
            settings = WelcomeSettings.from(config)
        }
        sender.sendMessage(Component.text("設定を再読み込みしました。", NamedTextColor.GREEN))
    }

    private fun set(sender: CommandSender, key: String, value: String) {
        val path = requirePath(sender, key) ?: return
        val converted: Any = when (key) {
            KEY_DELAY -> value.toIntOrNull()?.takeIf { it >= 1 } ?: run {
                sender.sendMessage(Component.text("delay には 1 以上の整数を指定してください (例: 20)", NamedTextColor.RED))
                return
            }
            KEY_URL -> value.takeIf(WelcomeSettings::isValidUrl) ?: run {
                sender.sendMessage(Component.text("url は http:// か https:// で始めてください", NamedTextColor.RED))
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
        sender.sendMessage(Component.text("$key を更新しました。", NamedTextColor.GREEN))
        // 値はタグを解釈せず、入力したとおりに見せる。
        sender.sendMessage(Component.text("新しい値: ", NamedTextColor.GRAY).append(Component.text(value, NamedTextColor.WHITE)))
    }

    private fun get(sender: CommandSender, key: String) {
        val path = requirePath(sender, key) ?: return
        val value = synchronized(configLock) { config.get(path) }?.toString() ?: "(未設定)"
        sender.sendMessage(
            Component.text(key, NamedTextColor.GOLD)
                .append(Component.text(" の現在の値: ", NamedTextColor.GRAY))
                .append(Component.text(value, NamedTextColor.WHITE)),
        )
    }

    private fun requirePath(sender: CommandSender, key: String): String? {
        val path = CONFIG_PATHS[key]
        if (path == null) {
            sender.sendMessage(Component.text("無効なキーです: $key", NamedTextColor.RED))
            sender.sendMessage(Component.text("利用可能なキー: " + CONFIG_PATHS.keys.joinToString(", "), NamedTextColor.GRAY))
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
            KEY_URL to "url",
            KEY_DELAY to "delay-ticks",
        )
    }
}
