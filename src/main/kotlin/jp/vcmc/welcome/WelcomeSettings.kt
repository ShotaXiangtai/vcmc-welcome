package jp.vcmc.welcome

import org.bukkit.configuration.ConfigurationSection

/** config.yml の内容。読み直すと新しいインスタンスに置き換えるので、どのスレッドから読んでもよい。 */
data class WelcomeSettings(
    val delayTicks: Long,
    val url: String,
    val soundName: String,
    val soundVolume: Float,
    val soundPitch: Float,
    val separator: String,
    val welcome: String,
    val urlLabel: String,
    val urlHover: String,
    val discordWarning: String,
    val discordNote: String,
) {
    companion object {
        const val DEFAULT_URL = "https://www.vcmc.jp/"
        const val DEFAULT_SOUND = "entity.player.levelup"

        fun from(config: ConfigurationSection) = WelcomeSettings(
            delayTicks = config.getLong("delay-ticks", 20L).coerceAtLeast(1L),
            url = config.getString("url", DEFAULT_URL).orEmpty(),
            soundName = config.getString("sound.name", DEFAULT_SOUND).orEmpty(),
            soundVolume = config.getDouble("sound.volume", 1.0).toFloat(),
            soundPitch = config.getDouble("sound.pitch", 1.0).toFloat(),
            separator = message(config, "separator", "<dark_gray><st>--------------------------------------------</st>"),
            welcome = message(config, "welcome", "<yellow><bold>ようこそ！<aqua><player></aqua><white> さん"),
            urlLabel = message(config, "url-label", "<green><bold>[公式HP] <white>クリックしてアクセス:"),
            urlHover = message(config, "url-hover", "クリックしてHPを開く"),
            discordWarning = message(
                config,
                "discord-warning",
                "<red><bold>【重要】<gold>Discordサーバーへの参加は <dark_red><u>必須</u></dark_red><gold> です！",
            ),
            discordNote = message(config, "discord-note", "<gray>※HP内に招待リンクがあります"),
        )

        private fun message(config: ConfigurationSection, key: String, default: String): String =
            normalizePlaceholders(config.getString("messages.$key", default).orEmpty())

        /** {player} でも <player> と同じように書ける。 */
        fun normalizePlaceholders(template: String): String = template.replace("{player}", "<player>")

        /** クリックで開ける形式か。それ以外はクライアントが開かないので設定させない。 */
        fun isValidUrl(url: String): Boolean = url.startsWith("https://") || url.startsWith("http://")

        /** 名前空間のないサウンド名は minecraft のものとして扱う。空なら音を鳴らさない。 */
        fun soundKey(name: String): String? {
            val trimmed = name.trim().lowercase()
            return when {
                trimmed.isEmpty() -> null
                ':' in trimmed -> trimmed
                else -> "minecraft:$trimmed"
            }
        }
    }
}
