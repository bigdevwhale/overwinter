package app.overwinter

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import app.overwinter.data.DemoSeed
import app.overwinter.data.Settings
import app.overwinter.domain.Rare
import app.overwinter.ui.Overlay
import app.overwinter.ui.Root
import app.overwinter.ui.Tab
import app.overwinter.ui.onboarding.Onboarding
import app.overwinter.ui.theme.OverwinterTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

/** Every main screen over the demo station, drawn to PNGs (run with `-Prender`). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }
    private val app get() = ApplicationProvider.getApplicationContext<OverwinterApp>()
    /** An evening, so the forecast question is out and all three terms are in. */
    private val now = LocalDateTime.now().withHour(21).withMinute(40)
    private val nowMs = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun seed(): Settings = runBlocking {
        val c = app.container
        DemoSeed.seedIfEmpty(app, c, now)
        c.refresh()
        c.settings.now()
    }

    private fun shot(name: String, waitFor: String?, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent { OverwinterTheme { content() } }
        rule.waitUntil(20_000) { rule.mainClock.advanceTimeBy(100); waitFor == null || rule.onAllNodesWithText(waitFor, substring = true).fetchSemanticsNodes().isNotEmpty() }
        rule.mainClock.advanceTimeBy(3_500)
        rule.waitForIdle()
        Thread.sleep(500)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun s(id: Int) = app.getString(id)

    private fun screen(name: String, tab: Tab, overlay: Overlay? = null, wait: String) {
        val st = seed()
        shot(name, wait) { Root(st, tab, overlay, nowMs) }
    }

    @Test fun welcome() { val st = seed(); shot("0-welcome", null) { Onboarding(st.copy(onboarded = false)) } }
    @Test fun instruments() { val st = seed(); shot("0-instruments", null) { Onboarding(st.copy(onboarded = false), start = 1) } }

    @Test fun logo() {
        val n = 512
        val image = android.graphics.Bitmap.createBitmap(n, n, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(image)
        canvas.clipPath(android.graphics.Path().apply { addRoundRect(0f, 0f, n.toFloat(), n.toFloat(), n * .22f, n * .22f, android.graphics.Path.Direction.CW) })
        val k = n / 80f * 108f
        val off = (n - k) / 2
        for (id in listOf(R.drawable.ic_launcher_background, R.drawable.ic_launcher_foreground)) {
            val d = androidx.core.content.ContextCompat.getDrawable(app, id)!!
            d.setBounds(off.toInt(), off.toInt(), (off + k).toInt(), (off + k).toInt())
            d.draw(canvas)
        }
        File(out.parentFile, "logo.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun station() = screen("1-station", Tab.STATION, wait = s(R.string.rare_open).substringBefore(":"))
    @Test fun observe() = screen("2-observe", Tab.STATION, Overlay.Observe, s(R.string.send))
    @Test fun sent() {
        val st = seed()
        val o = app.container.journal.value.obs.maxBy { it.at }
        shot("3-sent", s(R.string.sent_ok)) { Root(st, Tab.STATION, Overlay.Sent(o, listOf(Rare.AURORA)), nowMs) }
    }
    @Test fun log() = screen("4-barogram", Tab.LOG, wait = s(R.string.log_legend).take(10))
    @Test fun book() = screen("5-manual", Tab.BOOK, wait = s(R.string.rule_keep))
    @Test fun rare() = screen("6-rare", Tab.STATION, Overlay.Rare, s(R.string.rare_b).take(10))
    @Test fun settings() = screen("7-settings", Tab.STATION, Overlay.Settings, s(R.string.theme_dark))
}

@Config(sdk = [34], qualifiers = "en-w411dp-h891dp-notnight-xxhdpi")
class RenderScreensEn : ScreensBase() { override val lang = "en" }

@Config(sdk = [34], qualifiers = "ru-w411dp-h891dp-notnight-xxhdpi")
class RenderScreensRu : ScreensBase() { override val lang = "ru" }

@Config(sdk = [34], qualifiers = "ru-w411dp-h891dp-night-xxhdpi")
class RenderScreensRuNight : ScreensBase() { override val lang = "ru-night" }
