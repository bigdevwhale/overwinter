package app.overwinter.ui.settings

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Look
import app.overwinter.domain.Gear
import app.overwinter.ui.Nav
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Chip
import app.overwinter.ui.components.Field
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.IconBtn
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import java.util.UUID

private val ICONS = listOf("🧪", "🍵", "🧘", "🚿", "🥗", "🍬", "🚬", "🤧", "🌡", "💧", "😴", "🎧", "🧴", "❄", "🔥", "📚")

/** The weather box: which instruments to read at each term. */
@Composable
fun GearPage(look: Look, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val list = look.j.gear
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(ICONS[0]) }
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.gear_t), onBack = { nav.close() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.gear_page_b), style = T.Small)
            Gap(12.dp)
            for (g in list) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(g.icon, style = T.Heading, modifier = Modifier.width(38.dp))
                    Text(Words.gear(ctx, g), style = T.Body, modifier = Modifier.weight(1f))
                    if (!g.builtin) {
                        IconBtn(Icons.Outlined.Delete, { c.write { gear(list.filterNot { it.id == g.id }) } }, tint = C.muted)
                        Gap(0.dp)
                        Text("", Modifier.width(8.dp))
                    }
                    Switch(g.on, { on -> c.write { gear(list.map { if (it.id == g.id) it.copy(on = on) else it }) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = C.onAccent, checkedTrackColor = C.accent, uncheckedTrackColor = C.surface2, uncheckedBorderColor = C.line))
                }
            }
            Gap(16.dp)
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.gear_add))
                Gap(8.dp)
                Field(name, { name = it.take(24) }, Modifier.fillMaxWidth(), stringResource(R.string.gear_add_hint))
                Gap(10.dp)
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in ICONS) Chip(i, icon == i, { icon = i })
                }
                Gap(12.dp)
                Btn(stringResource(R.string.gear_add_btn), {
                    val n = name.trim()
                    if (n.isNotEmpty()) {
                        c.write { gear(list + Gear("u" + UUID.randomUUID().toString().take(8), icon, n)) }
                        name = ""
                    }
                }, Modifier.fillMaxWidth(), enabled = name.isNotBlank())
            }
            Gap(24.dp)
        }
    }
}
