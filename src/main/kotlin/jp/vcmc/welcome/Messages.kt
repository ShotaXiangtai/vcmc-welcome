package jp.vcmc.welcome

import jp.vcmc.welcome.i18n.Language
import jp.vcmc.welcome.i18n.Translations
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.plugin.java.JavaPlugin

/**
 * 受け取る人の言語で文面を作る。日本語は config.yml の messages、ほかの言語は lang フォルダから読む。
 * プレースホルダーの値はタグとして解釈しない。設定値を入力したとおりに見せるため。
 */
class Messages(private val plugin: JavaPlugin) {
    private val translations = Translations(plugin)
    private val miniMessage = MiniMessage.miniMessage()

    fun reload() = translations.reload()

    fun raw(language: Language, key: String): String =
        normalizePlaceholders(translations.string(language, key) ?: plugin.config.getString("messages.$key") ?: key)

    fun text(language: Language, key: String, vararg args: Pair<String, String>): Component =
        miniMessage.deserialize(raw(language, key), TagResolver.resolver(args.map { (name, value) -> Placeholder.unparsed(name, value) }))

    fun send(target: Audience, key: String, vararg args: Pair<String, String>) {
        target.sendMessage(text(Language.of(target), key, *args))
    }

    companion object {
        /** {player} でも <player> と同じように書ける。 */
        fun normalizePlaceholders(template: String): String = template.replace("{player}", "<player>")
    }
}
