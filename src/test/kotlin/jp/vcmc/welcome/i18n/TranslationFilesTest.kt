package jp.vcmc.welcome.i18n

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TranslationFilesTest {
    private val config = TranslationFilesCheck.resource("config.yml")

    private fun plainText(miniMessage: String?) =
        PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(assertNotNull(miniMessage)))

    @Test
    fun `どの言語にも日本語と同じ文面がある`() {
        val messages = assertNotNull(config.getConfigurationSection("messages"))
        TranslationFilesCheck.check(TranslationFilesCheck.texts(messages))
    }

    @Test
    fun `コマンドの書式はタグとして解釈されない`() {
        assertEquals("/welcome set <key> <value> - Change the Japanese text or a setting", plainText(TranslationFilesCheck.translated.getValue(Language.ENGLISH).getString("help-set")))
    }

    @Test
    fun `メニューの案内を既定で表示する`() {
        assertEquals("[メニュー] メインサーバーでは /menu で、よく使うコマンドをボタンから実行できます", plainText(config.getString("messages.menu-note")))
        assertEquals(
            "[Menu] On the main server, use /menu to run common commands with buttons",
            plainText(TranslationFilesCheck.translated.getValue(Language.ENGLISH).getString("menu-note")),
        )
    }
}
