package jp.vcmc.welcome

import org.bukkit.configuration.file.YamlConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WelcomeSettingsTest {
    @Test
    fun `空の設定では既定値を使う`() {
        val settings = WelcomeSettings.from(YamlConfiguration())
        assertEquals(20L, settings.delayTicks)
        assertEquals(WelcomeSettings.DEFAULT_URL, settings.url)
        assertEquals(WelcomeSettings.DEFAULT_SOUND, settings.soundName)
    }

    @Test
    fun `遅延は 1 tick 未満にしない`() {
        val config = YamlConfiguration().apply { set("delay-ticks", 0) }
        assertEquals(1L, WelcomeSettings.from(config).delayTicks)
    }

    @Test
    fun `プレースホルダーは波かっこでも書ける`() {
        assertEquals("ようこそ <player> さん", Messages.normalizePlaceholders("ようこそ {player} さん"))
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
