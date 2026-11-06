package jp.vcmc.welcome

import org.bukkit.configuration.ConfigurationSection

/** config.yml の内容。読み直すと新しいインスタンスに置き換えるので、どのスレッドから読んでもよい。 */
data class WelcomeSettings(
    val delayTicks: Long,
    val url: String,
    val soundName: String,
    val soundVolume: Float,
    val soundPitch: Float,
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
        )

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
