package jp.vcmc.welcome

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener(private val plugin: WelcomePlugin) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val delay = synchronized(plugin) {
            plugin.config.getInt("delay-ticks", 20).toLong().coerceAtLeast(1L)
        }

        player.scheduler.runDelayed(plugin, { _ ->
            if (player.isOnline) {
                sendWelcome(player)
            }
        }, null, delay)
    }

    private fun sendWelcome(player: Player) {
        val mm = plugin.miniMessage
        var url: String
        var soundName: String
        var volume: Float
        var pitch: Float
        var sepMsg: String
        var welcomeMsg: String
        var urlLabelMsg: String
        var urlHoverMsg: String
        var warnMsg: String
        var noteMsg: String

        synchronized(plugin) {
            val config = plugin.config
            url = config.getString("url", "https://www.vcmc.jp/") ?: "https://www.vcmc.jp/"
            soundName = config.getString("sound.name", "entity.player.levelup") ?: "entity.player.levelup"
            volume = config.getDouble("sound.volume", 1.0).toFloat()
            pitch = config.getDouble("sound.pitch", 1.0).toFloat()
            sepMsg = config.getString("messages.separator", "<dark_gray><st>--------------------------------------------</st>") ?: ""
            welcomeMsg = config.getString("messages.welcome", "<yellow><bold>ようこそ！<aqua><player></aqua><white> さん") ?: ""
            urlLabelMsg = config.getString("messages.url-label", "<green><bold>[公式HP] <white>クリックしてアクセス:") ?: ""
            urlHoverMsg = config.getString("messages.url-hover", "クリックしてHPを開く") ?: ""
            warnMsg = config.getString("messages.discord-warning", "<red><bold>【重要】<gold>Discordサーバーへの参加は <dark_red><u>必須</u></dark_red><gold> です！") ?: ""
            noteMsg = config.getString("messages.discord-note", "<gray>※HP内に招待リンクがあります") ?: ""
        }

        // サウンド再生
        if (!soundName.contains(":")) {
            soundName = "minecraft:$soundName"
        }
        try {
            player.playSound(Sound.sound(Key.key(soundName.lowercase()), Sound.Source.PLAYER, volume, pitch))
        } catch (e: Exception) {
            plugin.logger.warning("無効なサウンド名が指定されています ($soundName): ${e.message}")
        }

        // <player> プレースホルダーをプレイヤー名に置換
        val playerTag = Placeholder.component("player", Component.text(player.name))

        // クリック可能な URL コンポーネント
        val urlComponent = Component.text(url)
            .color(NamedTextColor.AQUA)
            .decorate(TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.openUrl(url))
            .hoverEvent(HoverEvent.showText(mm.deserialize(urlHoverMsg)))

        // メッセージ送信
        player.sendMessage(mm.deserialize(sepMsg, playerTag))
        player.sendMessage(Component.empty())
        player.sendMessage(mm.deserialize(welcomeMsg, playerTag))
        player.sendMessage(Component.empty())
        player.sendMessage(mm.deserialize(urlLabelMsg, playerTag))
        player.sendMessage(urlComponent)
        player.sendMessage(Component.empty())
        player.sendMessage(mm.deserialize(warnMsg, playerTag))
        player.sendMessage(mm.deserialize(noteMsg, playerTag))
        player.sendMessage(Component.empty())
        player.sendMessage(mm.deserialize(sepMsg, playerTag))
    }
}
