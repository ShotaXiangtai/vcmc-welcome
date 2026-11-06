package jp.vcmc.welcome

import net.kyori.adventure.key.InvalidKeyException
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
        // 遅延後にログアウトしていれば、エンティティのスケジューラが実行しない。
        player.scheduler.runDelayed(plugin, { _ -> sendWelcome(player) }, null, plugin.settings.delayTicks)
    }

    private fun sendWelcome(player: Player) {
        val settings = plugin.settings
        playSound(player, settings)

        val mm = plugin.miniMessage
        val playerTag = Placeholder.component("player", Component.text(player.name))

        player.sendMessage(mm.deserialize(settings.separator, playerTag))
        player.sendMessage(Component.empty())
        player.sendMessage(mm.deserialize(settings.welcome, playerTag))
        player.sendMessage(Component.empty())
        if (settings.url.isNotBlank()) {
            player.sendMessage(mm.deserialize(settings.urlLabel, playerTag))
            player.sendMessage(
                Component.text(settings.url)
                    .color(NamedTextColor.AQUA)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.openUrl(settings.url))
                    .hoverEvent(HoverEvent.showText(mm.deserialize(settings.urlHover, playerTag))),
            )
            player.sendMessage(Component.empty())
        }
        player.sendMessage(mm.deserialize(settings.discordWarning, playerTag))
        player.sendMessage(mm.deserialize(settings.discordNote, playerTag))
        if (settings.menuNote.isNotBlank()) {
            player.sendMessage(Component.empty())
            player.sendMessage(mm.deserialize(settings.menuNote, playerTag))
        }
        player.sendMessage(Component.empty())
        player.sendMessage(mm.deserialize(settings.separator, playerTag))
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
