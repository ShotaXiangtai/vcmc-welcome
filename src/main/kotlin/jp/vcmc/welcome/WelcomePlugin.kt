package jp.vcmc.welcome

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin

class WelcomePlugin : JavaPlugin(), CommandExecutor, TabCompleter {

    val miniMessage: MiniMessage = MiniMessage.miniMessage()

    companion object {
        private val MESSAGE_KEYS = listOf(
            "welcome", "separator", "url-label", "url-hover",
            "discord-warning", "discord-note", "url", "delay"
        )
    }

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(JoinListener(this), this)

        val bcmd = object : org.bukkit.command.Command("welcome") {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
                return this@WelcomePlugin.onCommand(sender, this, commandLabel, args)
            }

            override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
                return this@WelcomePlugin.onTabComplete(sender, this, alias, args)
            }
        }
        bcmd.permission = "vcmc.welcome.admin"
        server.commandMap.register(name, bcmd)
        logger.info("VCMCWelcome enabled.")
    }

    override fun onDisable() {
        logger.info("VCMCWelcome disabled.")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("vcmcwelcome.admin") && !sender.hasPermission("vcmc.welcome.admin")) {
            sender.sendMessage(miniMessage.deserialize("<red>このコマンドを実行する権限がありません。"))
            return true
        }

        if (args.isNotEmpty()) {
            when (args[0].lowercase()) {
                "reload" -> {
                    synchronized(this) {
                        reloadConfig()
                    }
                    sender.sendMessage(miniMessage.deserialize("<green>設定を再読み込みしました。"))
                    return true
                }
                "set" -> {
                    if (args.size < 3) {
                        sender.sendMessage(miniMessage.deserialize("<red>使用方法: /welcome set <キー> <値>"))
                        sender.sendMessage(miniMessage.deserialize("<gray>利用可能なキー: " + MESSAGE_KEYS.joinToString(", ")))
                        return true
                    }
                    val key = args[1].lowercase()
                    val value = args.drop(2).joinToString(" ")
                    return handleSet(sender, key, value)
                }
                "get" -> {
                    if (args.size < 2) {
                        sender.sendMessage(miniMessage.deserialize("<red>使用方法: /welcome get <キー>"))
                        sender.sendMessage(miniMessage.deserialize("<gray>利用可能なキー: " + MESSAGE_KEYS.joinToString(", ")))
                        return true
                    }
                    return handleGet(sender, args[1].lowercase())
                }
            }
        }

        sender.sendMessage(miniMessage.deserialize("<gold>==== VCMCWelcome ヘルプ ===="))
        sender.sendMessage(miniMessage.deserialize("<white>/welcome reload <gray>- 設定を再読み込み"))
        sender.sendMessage(miniMessage.deserialize("<white>/welcome set <キー> <値> <gray>- 設定を変更"))
        sender.sendMessage(miniMessage.deserialize("<white>/welcome get <キー> <gray>- 設定値を確認"))
        return true
    }

    private fun handleSet(sender: CommandSender, key: String, value: String): Boolean {
        val configKey = resolveConfigKey(key)
        if (configKey == null) {
            sender.sendMessage(miniMessage.deserialize("<red>無効なキーです: <white>$key"))
            sender.sendMessage(miniMessage.deserialize("<gray>利用可能なキー: " + MESSAGE_KEYS.joinToString(", ")))
            return true
        }
        synchronized(this) {
            if (key == "delay") {
                val ticks = value.toIntOrNull()
                if (ticks == null || ticks < 1) {
                    sender.sendMessage(miniMessage.deserialize("<red>delay には 1 以上の整数を指定してください (例: 20)"))
                    return true
                }
                config.set(configKey, ticks)
            } else {
                config.set(configKey, value)
            }
            saveConfig()
        }
        sender.sendMessage(miniMessage.deserialize("<green><bold>$key</bold> を更新しました。"))
        sender.sendMessage(miniMessage.deserialize("<gray>新しい値: <white>$value"))
        return true
    }

    private fun handleGet(sender: CommandSender, key: String): Boolean {
        val configKey = resolveConfigKey(key)
        if (configKey == null) {
            sender.sendMessage(miniMessage.deserialize("<red>無効なキーです: <white>$key"))
            sender.sendMessage(miniMessage.deserialize("<gray>利用可能なキー: " + MESSAGE_KEYS.joinToString(", ")))
            return true
        }
        val valObj = synchronized(this) { config.get(configKey) }
        sender.sendMessage(miniMessage.deserialize("<gold>$key<gray> の現在の値: <white>${valObj ?: "(未設定)"}"))
        return true
    }

    private fun resolveConfigKey(key: String): String? {
        return when (key) {
            "welcome"         -> "messages.welcome"
            "separator"       -> "messages.separator"
            "url-label"       -> "messages.url-label"
            "url-hover"       -> "messages.url-hover"
            "discord-warning" -> "messages.discord-warning"
            "discord-note"    -> "messages.discord-note"
            "url"             -> "url"
            "delay"           -> "delay-ticks"
            else              -> null
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String> {
        if (!sender.hasPermission("vcmcwelcome.admin") && !sender.hasPermission("vcmc.welcome.admin")) {
            return emptyList()
        }
        if (args.size == 1) {
            return listOf("reload", "set", "get").filter { it.startsWith(args[0].lowercase()) }
        }
        if (args.size == 2 && (args[0].equals("set", ignoreCase = true) || args[0].equals("get", ignoreCase = true))) {
            return MESSAGE_KEYS.filter { it.startsWith(args[1].lowercase()) }
        }
        return emptyList()
    }
}
