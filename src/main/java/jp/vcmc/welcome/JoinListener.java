package jp.vcmc.welcome;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final WelcomePlugin plugin;

    public JoinListener(WelcomePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int delay = plugin.getConfig().getInt("delay-ticks", 20);

        player.getScheduler().runDelayed(plugin, task -> {
            sendWelcome(player);
        }, null, delay);
    }

    private void sendWelcome(Player player) {
        var config = plugin.getConfig();
        var mm = plugin.getMiniMessage();
        String url = config.getString("url", "https://www.vcmc.jp/");

        // サウンド再生
        String soundName = config.getString("sound.name", "entity.player.levelup");
        if (!soundName.contains(":")) {
            soundName = "minecraft:" + soundName;
        }
        float volume = (float) config.getDouble("sound.volume", 1.0);
        float pitch = (float) config.getDouble("sound.pitch", 1.0);

        player.playSound(Sound.sound(Key.key(soundName), Sound.Source.PLAYER, volume, pitch));

        // {player} プレースホルダーをプレイヤー名に置換
        var playerTag = Placeholder.component("player", Component.text(player.getName()));

        // config からメッセージを取得
        String sepMsg      = config.getString("messages.separator",        "<dark_gray><st>--------------------------------------------</st>");
        String welcomeMsg  = config.getString("messages.welcome",          "<yellow><bold>ようこそ！ <aqua>{player}</aqua><white> さん");
        String urlLabelMsg = config.getString("messages.url-label",        "<green><bold>[公式HP] <white>クリックしてアクセス:");
        String urlHoverMsg = config.getString("messages.url-hover",        "クリックしてHPを開く");
        String warnMsg     = config.getString("messages.discord-warning",  "<red><bold>【重要】 <gold>Discordサーバーへの参加は <dark_red><u>必須</u></dark_red><gold> です！");
        String noteMsg     = config.getString("messages.discord-note",     "<gray>※HP内に招待リンクがあります");

        // クリック可能な URL コンポーネント
        Component urlComponent = Component.text(url)
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(mm.deserialize(urlHoverMsg)));

        // メッセージ送信
        player.sendMessage(mm.deserialize(sepMsg, playerTag));
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize(welcomeMsg, playerTag));
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize(urlLabelMsg, playerTag));
        player.sendMessage(urlComponent);
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize(warnMsg, playerTag));
        player.sendMessage(mm.deserialize(noteMsg, playerTag));
        player.sendMessage(Component.empty());
        player.sendMessage(mm.deserialize(sepMsg, playerTag));
    }
}
