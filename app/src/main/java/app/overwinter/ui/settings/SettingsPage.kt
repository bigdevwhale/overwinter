package app.overwinter.ui.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.overwinter.BuildConfig
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Barometer
import app.overwinter.data.Settings
import app.overwinter.ui.Nav
import app.overwinter.ui.Overlay
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Chip
import app.overwinter.ui.components.CommunityLinks
import app.overwinter.ui.components.Field
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.IconBtn
import app.overwinter.ui.components.Kind
import app.overwinter.ui.components.Ornament
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import kotlinx.coroutines.delay

@Composable
fun SettingsPage(s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    var callsign by remember { mutableStateOf(s.callsign) }
    LaunchedEffect(callsign) { delay(400); if (callsign.trim() != s.callsign) c.settings.update { it.copy(callsign = callsign.trim()) } }
    val notify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    fun save(change: (Settings) -> Settings) = c.write { settings.update(change); replan() }

    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.settings), onBack = { nav.close() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 18.dp)) {
            Gap(8.dp)
            Caps(stringResource(R.string.callsign))
            Gap(6.dp)
            Field(callsign, { callsign = it.take(12) }, Modifier.fillMaxWidth(), stringResource(R.string.callsign_default))
            Gap(4.dp)
            Text(stringResource(R.string.callsign_note), style = T.Small)
            Gap(20.dp)

            Caps(stringResource(R.string.terms_t))
            Gap(6.dp)
            val slots = Words.arr(ctx, R.array.slot_terms)
            for (i in 0..2) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(slots[i], style = T.Body, modifier = Modifier.weight(1f))
                    val lo = if (i == 0) 4 else s.hours[i - 1] + 1
                    val hi = if (i == 2) 23 else s.hours[i + 1] - 1
                    IconBtn(Icons.Rounded.Remove, { if (s.hours[i] > lo) save { it.copy(hours = it.hours.toMutableList().also { h -> h[i]-- }) } })
                    Text(Words.hour(s.hours[i]), style = T.Tape, modifier = Modifier.padding(horizontal = 10.dp))
                    IconBtn(Icons.Rounded.Add, { if (s.hours[i] < hi) save { it.copy(hours = it.hours.toMutableList().also { h -> h[i]++ }) } })
                }
                Gap(6.dp)
            }
            Gap(6.dp)
            Toggle(stringResource(R.string.remind), stringResource(R.string.remind_b), s.remind) { on ->
                if (on && android.os.Build.VERSION.SDK_INT >= 33) notify.launch(Manifest.permission.POST_NOTIFICATIONS)
                save { it.copy(remind = on) }
            }
            Gap(10.dp)
            Toggle(stringResource(R.string.sound), stringResource(R.string.sound_b), s.sound) { on -> save { it.copy(sound = on) } }
            Gap(20.dp)

            Caps(stringResource(R.string.gear_t))
            Gap(6.dp)
            Btn(stringResource(R.string.gear_edit_long), { nav.open(Overlay.Gear) }, Modifier.fillMaxWidth(), kind = Kind.QUIET)
            Gap(20.dp)

            Caps(stringResource(R.string.baro))
            Gap(6.dp)
            Text(stringResource(if (Barometer.has(ctx)) R.string.baro_yes else R.string.baro_no), style = T.Small)
            Gap(20.dp)

            Caps(stringResource(R.string.theme))
            Gap(8.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(R.string.theme_system, R.string.theme_light, R.string.theme_dark).forEachIndexed { i, r ->
                    Chip(stringResource(r), s.theme == i, { c.write { settings.update { it.copy(theme = i) } } })
                }
            }
            Gap(20.dp)
            Caps(stringResource(R.string.language))
            Gap(8.dp)
            val cur = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for ((tag, label) in listOf("" to stringResource(R.string.lang_system), "ru" to stringResource(R.string.lang_ru), "en" to stringResource(R.string.lang_en))) {
                    Chip(label, cur == tag || (tag.isNotEmpty() && cur.startsWith(tag)), {
                        AppCompatDelegate.setApplicationLocales(if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
                    })
                }
            }
            Gap(22.dp)
            Card(Modifier.fillMaxWidth(), color = C.surface2) {
                Caps(stringResource(R.string.about_t))
                Gap(6.dp)
                Text(stringResource(R.string.about_b), style = T.Small.copy(color = C.ink2))
            }
            Gap(22.dp)
            Caps(stringResource(R.string.community))
            Gap(8.dp)
            CommunityLinks()
            Gap(18.dp)
            Ornament()
            Gap(8.dp)
            Text(stringResource(R.string.version, BuildConfig.VERSION_NAME), style = T.Small)
            Gap(24.dp)
        }
    }
}

@Composable
fun Toggle(title: String, sub: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!on) }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = T.Body)
            Text(sub, style = T.Small)
        }
        Gap(0.dp)
        Text("", Modifier.width(8.dp))
        Switch(on, onChange, colors = SwitchDefaults.colors(checkedThumbColor = C.onAccent, checkedTrackColor = C.accent, uncheckedTrackColor = C.surface2, uncheckedBorderColor = C.line))
    }
}
