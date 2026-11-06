package jp.vcmc.welcome

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WelcomeSettingsTest {
    private fun plainText(miniMessage: String) =
        PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(miniMessage))

    @Test
    fun `以前の設定ファイルでもメニューの案内を表示する`() {
        val text = plainText(WelcomeSettings.from(YamlConfiguration()).menuNote)
        assertEquals("[メニュー] メインサーバーでは /menu で、よく使うコマンドをボタンから実行できます", text)
    }

    @Test
    fun `メニューの案内は config yml と既定値で同じ文面にする`() {
        val bundled = javaClass.getResourceAsStream("/config.yml")!!.reader(Charsets.UTF_8).use {
            YamlConfiguration.loadConfiguration(it)
        }
        assertEquals(WelcomeSettings.from(YamlConfiguration()).menuNote, WelcomeSettings.from(bundled).menuNote)
    }

    @Test
    fun `メニューの案内は空文字にできる`() {
        val config = YamlConfiguration().apply { set("messages.menu-note", "") }
        assertEquals("", WelcomeSettings.from(config).menuNote)
    }

    @Test
    fun `空の設定では既定値を使う`() {
        val settings = WelcomeSettings.from(YamlConfiguration())
        assertEquals(20L, settings.delayTicks)
        assertEquals(WelcomeSettings.DEFAULT_URL, settings.url)
        assertEquals(WelcomeSettings.DEFAULT_SOUND, settings.soundName)
        assertTrue("<player>" in settings.welcome)
    }

    @Test
    fun `遅延は 1 tick 未満にしない`() {
        val config = YamlConfiguration().apply { set("delay-ticks", 0) }
        assertEquals(1L, WelcomeSettings.from(config).delayTicks)
    }

    @Test
    fun `プレースホルダーは波かっこでも書ける`() {
        val config = YamlConfiguration().apply { set("messages.welcome", "ようこそ {player} さん") }
        assertEquals("ようこそ <player> さん", WelcomeSettings.from(config).welcome)
    }

    @Test
    fun `http と https だけを URL として認める`() {
        assertTrue(WelcomeSettings.isValidUrl("https://www.vcmc.jp/"))
        assertTrue(WelcomeSettings.isValidUrl("http://example.com"))
        assertFalse(WelcomeSettings.isValidUrl("www.vcmc.jp"))
        assertFalse(WelcomeSettings.isValidUrl("javascript:alert(1)"))
    }

    @Test
    fun `サウンド名に名前空間を補う`() {
        assertEquals("minecraft:entity.player.levelup", WelcomeSettings.soundKey("entity.player.levelup"))
        assertEquals("custom:ding", WelcomeSettings.soundKey("Custom:Ding"))
        assertNull(WelcomeSettings.soundKey("  "))
    }
}
