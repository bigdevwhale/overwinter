package app.overwinter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Look
import app.overwinter.data.Settings
import app.overwinter.domain.Obs
import app.overwinter.domain.Rare
import app.overwinter.ui.book.BookScreen
import app.overwinter.ui.components.ToastHost
import app.overwinter.ui.components.room
import app.overwinter.ui.log.LogScreen
import app.overwinter.ui.observe.ObserveScreen
import app.overwinter.ui.observe.SentScreen
import app.overwinter.ui.onboarding.Onboarding
import app.overwinter.ui.settings.GearPage
import app.overwinter.ui.settings.SettingsPage
import app.overwinter.ui.station.RarePage
import app.overwinter.ui.station.StationScreen
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T

enum class Tab { STATION, LOG, BOOK }

sealed interface Overlay {
    data object Observe : Overlay
    /** The telegram going out, and what it brought. */
    data class Sent(val obs: Obs, val rare: List<app.overwinter.domain.Rare>) : Overlay
    data object Rare : Overlay
    data object Settings : Overlay
    data object Gear : Overlay
}

class Nav(tab: Tab, first: Overlay?) {
    var tab by mutableStateOf(tab)
    val stack = mutableStateListOf<Overlay>().apply { if (first != null) add(first) }
    fun open(o: Overlay) { stack.add(o) }
    fun close() { if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) }
    fun replace(o: Overlay) { close(); open(o) }
}

@Composable
fun Root(s: Settings, tab: Tab = Tab.STATION, overlay: Overlay? = null, now: Long? = null) {
    val ctx = LocalContext.current
    val c = ctx.container
    if (!s.onboarded) { Onboarding(s); return }
    val journal by remember { c.journal }.collectAsState()
    val tape by remember { c.tape }.collectAsState()
    val observe by remember { c.observe }.collectAsState()
    val nav = remember { Nav(tab, overlay) }
    val names = remember { c.names() }
    val look = remember(journal, tape, s.started, now) {
        val t = now ?: System.currentTimeMillis()
        Look(journal, tape, names, java.time.Instant.ofEpochMilli(t).atZone(java.time.ZoneId.systemDefault()).toLocalDate(), t, s.startDate)
    }

    LaunchedEffect(observe) {
        if (observe) {
            if (nav.stack.none { it == Overlay.Observe }) nav.open(Overlay.Observe)
            c.observe.value = false
        }
    }

    Box(Modifier.fillMaxSize().background(C.bg).room().imePadding()) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (nav.tab) {
                    Tab.STATION -> StationScreen(look, s, nav)
                    Tab.LOG -> LogScreen(look, s, nav)
                    Tab.BOOK -> BookScreen(look, s, nav)
                }
            }
            Tabs(nav)
        }
        BackHandler(nav.stack.isEmpty() && nav.tab != Tab.STATION) { nav.tab = Tab.STATION }
        for (o in nav.stack.toList()) {
            Box(Modifier.fillMaxSize().background(C.bg).room().clickable(remember { MutableInteractionSource() }, null) {}) {
                Layer(o, look, s, nav)
            }
        }
        ToastHost(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 90.dp))
    }
}

@Composable
private fun Layer(o: Overlay, look: Look, s: Settings, nav: Nav) {
    BackHandler { nav.close() }
    when (o) {
        Overlay.Observe -> ObserveScreen(look, s, nav)
        is Overlay.Sent -> SentScreen(o.obs, o.rare, look, s, nav)
        Overlay.Rare -> RarePage(look, nav)
        Overlay.Settings -> SettingsPage(s, nav)
        Overlay.Gear -> GearPage(look, nav)
    }
}

@Composable
private fun Tabs(nav: Nav) {
    Row(
        Modifier.fillMaxWidth().background(C.bg2).navigationBarsPadding().height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
    ) {
        TabItem(Icons.Outlined.Home, stringResource(R.string.tab_station), nav.tab == Tab.STATION) { nav.tab = Tab.STATION }
        TabItem(Icons.Outlined.ShowChart, stringResource(R.string.tab_log), nav.tab == Tab.LOG) { nav.tab = Tab.LOG }
        TabItem(Icons.Outlined.AutoStories, stringResource(R.string.tab_book), nav.tab == Tab.BOOK) { nav.tab = Tab.BOOK }
    }
}

@Composable
private fun TabItem(icon: ImageVector, label: String, on: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = if (on) C.accent else C.muted, modifier = Modifier.size(24.dp))
        Text(label, style = T.Small.copy(color = if (on) C.accent else C.muted))
    }
}
