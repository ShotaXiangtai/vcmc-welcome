package jp.vcmc.welcome;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class WelcomePlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    private static final List<String> MESSAGE_KEYS = List.of(
        "welcome", "separator", "url-label", "url-hover",
        "discord-warning", "discord-note", "url", "delay"
    );

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        var cmd = getCommand("welcome");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
        getLogger().info("VCMCWelcome が有効になりました！");
    }

    @Override
    public void onDisable() {
        getLogger().info("VCMCWelcome が無効になりました。");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "reload" -> {
                    reloadConfig();
                    sender.sendMessage(miniMessage.deserialize("<green>設定を再読み込みしました！"));
                    return true;
                }
                case "set" -> {
                    if (args.length < 3) {
                        sender.sendMessage(miniMessage.deserialize("<red>使用方法: /welcome set <キー> <値>"));
                        sender.sendMessage(miniMessage.deserialize("<gray>キー一覧: " + String.join(", ", MESSAGE_KEYS)));
                        return true;
                    }
                    String key = args[1].toLowerCase();
                    String value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    return handleSet(sender, key, value);
                }
                case "get" -> {
                    if (args.length < 2) {
                        sender.sendMessage(miniMessage.deserialize("<red>使用方法: /welcome get <キー>"));
                        sender.sendMessage(miniMessage.deserialize("<gray>キー一覧: " + String.join(", ", MESSAGE_KEYS)));
                        return true;
                    }
                    return handleGet(sender, args[1].toLowerCase());
                }
            }
        }
        sender.sendMessage(miniMessage.deserialize("<gold>==== VCMCWelcome コマンド ===="));
        sender.sendMessage(miniMessage.deserialize("<white>/welcome reload <gray>- 設定を再読み込み"));
        sender.sendMessage(miniMessage.deserialize("<white>/welcome set <キー> <値> <gray>- メッセージを変更して保存"));
        sender.sendMessage(miniMessage.deserialize("<white>/welcome get <キー> <gray>- 現在の設定値を確認"));
        return true;
    }

    private boolean handleSet(CommandSender sender, String key, String value) {
        String configKey = resolveConfigKey(key);
        if (configKey == null) {
            sender.sendMessage(miniMessage.deserialize("<red>不明なキー: <white>" + key));
            sender.sendMessage(miniMessage.deserialize("<gray>有効なキー: " + String.join(", ", MESSAGE_KEYS)));
            return true;
        }
        if (key.equals("delay")) {
            try {
                int ticks = Integer.parseInt(value);
                getConfig().set(configKey, ticks);
            } catch (NumberFormatException e) {
                sender.sendMessage(miniMessage.deserialize("<red>delay には整数値を指定してください (例: 20)"));
                return true;
            }
        } else {
            getConfig().set(configKey, value);
        }
        saveConfig();
        sender.sendMessage(miniMessage.deserialize("<green><bold>" + key + "</bold> を更新しました！"));
        sender.sendMessage(miniMessage.deserialize("<gray>新しい値: <white>" + value));
        return true;
    }

    private boolean handleGet(CommandSender sender, String key) {
        String configKey = resolveConfigKey(key);
        if (configKey == null) {
            sender.sendMessage(miniMessage.deserialize("<red>不明なキー: <white>" + key));
            sender.sendMessage(miniMessage.deserialize("<gray>有効なキー: " + String.join(", ", MESSAGE_KEYS)));
            return true;
        }
        Object val = getConfig().get(configKey);
        sender.sendMessage(miniMessage.deserialize("<gold>" + key + "<gray>: <white>" + (val != null ? val : "(未設定)")));
        return true;
    }

    private String resolveConfigKey(String key) {
        return switch (key) {
            case "welcome"         -> "messages.welcome";
            case "separator"       -> "messages.separator";
            case "url-label"       -> "messages.url-label";
            case "url-hover"       -> "messages.url-hover";
            case "discord-warning" -> "messages.discord-warning";
            case "discord-note"    -> "messages.discord-note";
            case "url"             -> "url";
            case "delay"           -> "delay-ticks";
            default                -> null;
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "set", "get").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("get"))) {
            return MESSAGE_KEYS.stream()
                .filter(s -> s.startsWith(args[1].toLowerCase()))
                .toList();
        }
        return List.of();
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
