package jp.vcmc.welcome

import jp.vcmc.welcome.i18n.Language
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener(private val plugin: WelcomePlugin) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        // 遅延後にログアウトしていれば、エンティティのスケジューラが実行しない。
        player.scheduler.runDelayed(plugin, { _ -> sendWelcome(player) }, null, plugin.settings.delayTicks)
    }

    private fun sendWelcome(player: Player) {
        val settings = plugin.settings
        playSound(player, settings)

        val messages = plugin.messages
        val language = Language.of(player)
        fun text(key: String) = messages.text(language, key, "player" to player.name)

        player.sendMessage(text("separator"))
        player.sendMessage(Component.empty())
        player.sendMessage(text("welcome"))
        player.sendMessage(Component.empty())
        if (settings.url.isNotBlank()) {
            player.sendMessage(text("url-label"))
            player.sendMessage(
                Component.text(settings.url)
                    .color(NamedTextColor.AQUA)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.openUrl(settings.url))
                    .hoverEvent(HoverEvent.showText(text("url-hover"))),
            )
            player.sendMessage(Component.empty())
        }
        player.sendMessage(text("discord-warning"))
        player.sendMessage(text("discord-note"))
        if (messages.raw(language, "menu-note").isNotBlank()) {
            player.sendMessage(Component.empty())
            player.sendMessage(text("menu-note"))
        }
        player.sendMessage(Component.empty())
        player.sendMessage(text("separator"))
    }

    private fun playSound(player: Player, settings: WelcomeSettings) {
        val key = WelcomeSettings.soundKey(settings.soundName) ?: return
        try {
            player.playSound(Sound.sound(Key.key(key), Sound.Source.PLAYER, settings.soundVolume, settings.soundPitch))
        } catch (e: InvalidKeyException) {
            plugin.logger.warning("無効なサウンド名が指定されています ($key): ${e.message}")
        }
    }
}
