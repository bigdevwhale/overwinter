package app.overwinter.ui.observe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Look
import app.overwinter.data.Settings
import app.overwinter.domain.Obs
import app.overwinter.domain.Rain
import app.overwinter.domain.Zones
import app.overwinter.ui.Nav
import app.overwinter.ui.Overlay
import app.overwinter.ui.art.Body
import app.overwinter.ui.art.Body.centres
import app.overwinter.ui.art.Body.figure
import app.overwinter.ui.art.Marks.okta
import app.overwinter.ui.art.Marks.precip
import app.overwinter.ui.art.StationMark
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Chip
import app.overwinter.ui.components.Field
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.IconBtn
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.station.Terms
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ObserveScreen(look: Look, s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    val now = Instant.ofEpochMilli(look.now).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val slot = Terms.current(s.hours, now)
    // Sleep is asked at the first term of the day.
    val askSleep = look.todayDay?.sleep == null
    val prev = look.j.obs.maxByOrNull { it.at }

    var wind by remember { mutableIntStateOf(prev?.wind ?: 5) }
    var cloud by remember { mutableIntStateOf(prev?.cloud ?: 3) }
    var vis by remember { mutableIntStateOf(2) }
    var rain by remember { mutableStateOf(mapOf<Int, Int>()) }
    var sleep by remember { mutableFloatStateOf(7.5f) }
    // Bed and wake times instead of hours (#3): the mode the last night was written in.
    val lastNight = look.j.obs.filter { it.bed != null && it.wake != null }.maxByOrNull { it.at }
    var byClock by remember { mutableStateOf(lastNight != null) }
    var bed by remember { mutableIntStateOf(lastNight?.bed ?: (23 * 60)) }
    var wake by remember { mutableIntStateOf(lastNight?.wake ?: (7 * 60)) }
    // Blood pressure, written only if measured (#5).
    var bpOpen by remember { mutableStateOf(false) }
    var sys by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var pulse by remember { mutableStateOf("") }
    val bp = sys.toIntOrNull()?.takeIf { it in 50..300 }?.let { sv -> dia.toIntOrNull()?.takeIf { it in 30..200 }?.let { dv -> app.overwinter.domain.Bp(sv, dv, pulse.toIntOrNull()?.takeIf { it in 20..250 }) } }
    var gear by remember { mutableStateOf(setOf<String>()) }
    var note by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    val terms = Words.arr(ctx, R.array.slot_terms)
    Column(Modifier.fillMaxSize()) {
        TopBar(terms[slot.ordinal], sub = stringResource(R.string.obs_sub), onBack = { nav.close() }, right = {
            StationMark(wind, cloud, C.ink, Modifier.size(52.dp))
        })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Gap(6.dp)
            // Wind: energy.
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.wind_t))
                Gap(4.dp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$wind", style = T.Number.copy(color = C.accent))
                    Text("  " + Words.arr(ctx, R.array.beaufort)[wind], style = T.Heading, modifier = Modifier.padding(bottom = 3.dp))
                }
                Text(Words.arr(ctx, R.array.beaufort_body)[wind], style = T.Italic)
                Slider(
                    value = wind.toFloat(), onValueChange = { wind = it.toInt() }, valueRange = 0f..12f, steps = 11,
                    colors = SliderDefaults.colors(thumbColor = C.accent, activeTrackColor = C.accent, inactiveTrackColor = C.line, activeTickColor = C.onAccent, inactiveTickColor = C.muted),
                    modifier = Modifier.testTag("wind"),
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.wind_lo), style = T.Small, modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.wind_hi), style = T.Small)
                }
            }
            Gap(10.dp)
            // Cloud: mood.
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.cloud_t))
                Gap(4.dp)
                Text(stringResource(R.string.okta_x, cloud) + " · " + Words.arr(ctx, R.array.okta)[cloud], style = T.Heading)
                Text(Words.arr(ctx, R.array.okta_mood)[cloud], style = T.Italic)
                Gap(10.dp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (k in 0..8) {
                        val on = k == cloud
                        val ink = if (on) C.accent else C.ink2
                        Canvas(Modifier.size(32.dp).clip(CircleShape).clickable { cloud = k }.padding(4.dp)) {
                            okta(Offset(size.width / 2, size.height / 2), size.minDimension / 2.3f, k, ink, if (on) 3.5f else 2.2f)
                        }
                    }
                }
            }
            Gap(10.dp)
            // Visibility: how clear the head is.
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.vis_t))
                Gap(8.dp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Words.arr(ctx, R.array.vis).forEachIndexed { i, label -> Chip(label, vis == i, { vis = i }) }
                }
            }
            Gap(10.dp)
            // Precipitation: pain.
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.rain_t))
                Gap(4.dp)
                Text(stringResource(R.string.rain_b), style = T.Small)
                Gap(8.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val ink = C.ink2
                    val fill = C.surface2
                    val mark = C.ice
                    val storm = C.accent
                    Canvas(
                        Modifier.width(130.dp).height(200.dp).testTag("body").pointerInput(Unit) {
                            detectTapGestures { p ->
                                Body.hit(p, size.width.toFloat(), size.height.toFloat())?.let { z -> rain = cycle(rain, z) }
                            }
                        },
                    ) {
                        figure(ink, fill)
                        for ((z, lvl) in rain) for (p in centres(z)) {
                            drawCircle(if (lvl == 3) storm.copy(alpha = .18f) else mark.copy(alpha = .18f), 16.dp.toPx(), p)
                            precip(p, 18.dp.toPx(), lvl, if (lvl == 3) storm else mark, 2.dp.toPx())
                        }
                    }
                    Gap(0.dp)
                    Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val zones = Words.arr(ctx, R.array.zones)
                        val levels = Words.arr(ctx, R.array.rain_levels)
                        for (z in listOf(Zones.BACK, Zones.ALL)) {
                            val lvl = rain[z] ?: 0
                            Chip(zones[z] + if (lvl > 0) " · " + levels[lvl] else "", lvl > 0, { rain = cycle(rain, z) })
                        }
                        Gap(4.dp)
                        if (rain.isEmpty()) Text(stringResource(R.string.rain_none), style = T.Small)
                        else for ((z, lvl) in rain.toSortedMap()) if (z != Zones.BACK && z != Zones.ALL) Text("${zones[z]}: ${levels[lvl]}", style = T.Label)
                        Text(stringResource(R.string.rain_legend), style = T.Small)
                    }
                }
            }
            Gap(10.dp)
            if (askSleep) {
                Card(Modifier.fillMaxWidth()) {
                    Caps(stringResource(R.string.sleep_t))
                    Gap(8.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Chip(stringResource(R.string.sleep_hours), !byClock, { byClock = false })
                        Chip(stringResource(R.string.sleep_clock), byClock, { byClock = true })
                    }
                    Gap(6.dp)
                    if (!byClock) Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBtn(Icons.Rounded.Remove, { sleep = (sleep - .5f).coerceAtLeast(0f) })
                        Text(stringResource(R.string.hours_x, Words.num(ctx, sleep)), style = T.Number, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        IconBtn(Icons.Rounded.Add, { sleep = (sleep + .5f).coerceAtMost(14f) })
                    } else {
                        ClockRow(stringResource(R.string.sleep_bed), bed) { bed = it }
                        ClockRow(stringResource(R.string.sleep_wake), wake) { wake = it }
                        val h = app.overwinter.domain.sleptHours(bed, wake)
                        Text(stringResource(R.string.slept_x, Words.num(ctx, h, if (h * 4 % 2 == 0f) 1 else 2)), style = T.Small)
                    }
                }
                Gap(10.dp)
            }
            // Blood pressure from a cuff, kept apart from the sky (#5).
            Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Caps(stringResource(R.string.bp_t), Modifier.weight(1f))
                    if (!bpOpen) Text(stringResource(R.string.bp_add), style = T.Label.copy(color = C.accent), modifier = Modifier.clickable { bpOpen = true }.padding(4.dp))
                }
                if (bpOpen) {
                    Gap(8.dp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Field(sys, { t -> sys = t.filter(Char::isDigit).take(3) }, Modifier.weight(1f), stringResource(R.string.bp_sys), number = true, style = T.Strong)
                        Text("/", style = T.Strong)
                        Field(dia, { t -> dia = t.filter(Char::isDigit).take(3) }, Modifier.weight(1f), stringResource(R.string.bp_dia), number = true, style = T.Strong)
                        Field(pulse, { t -> pulse = t.filter(Char::isDigit).take(3) }, Modifier.weight(1f), stringResource(R.string.bp_pulse), number = true, style = T.Strong)
                    }
                    Gap(4.dp)
                    Text(stringResource(R.string.bp_b), style = T.Small)
                }
            }
            Gap(10.dp)
            // Instruments.
            Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Caps(stringResource(R.string.gear_t), Modifier.weight(1f))
                    Text(stringResource(R.string.gear_edit), style = T.Label.copy(color = C.accent), modifier = Modifier.clickable { nav.open(Overlay.Gear) }.padding(4.dp))
                }
                Gap(4.dp)
                Text(stringResource(R.string.gear_b), style = T.Small)
                Gap(8.dp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (g in look.j.gear.filter { it.on }) {
                        Chip(g.icon + " " + Words.gear(ctx, g), g.id in gear, { gear = if (g.id in gear) gear - g.id else gear + g.id })
                    }
                }
            }
            Gap(10.dp)
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.note_t))
                Gap(8.dp)
                Field(note, { note = it }, Modifier.fillMaxWidth(), stringResource(R.string.note_hint), lines = 3, style = T.Hand)
            }
            Gap(16.dp)
            Btn(stringResource(R.string.send), {
                if (busy) return@Btn
                busy = true
                scope.launch {
                    val draft = Obs(
                        id = "", at = System.currentTimeMillis(), slot = slot, wind = wind, cloud = cloud, vis = vis,
                        rain = rain.map { (z, l) -> Rain(z, l) }, gear = gear, note = note.trim(), bp = bp,
                        sleep = if (!askSleep) null else if (byClock) app.overwinter.domain.sleptHours(bed, wake) else sleep,
                        bed = if (askSleep && byClock) bed else null, wake = if (askSleep && byClock) wake else null,
                    )
                    val found = c.observe(draft, s)
                    val saved = c.journal.value.obs.maxByOrNull { it.at } ?: draft
                    nav.replace(Overlay.Sent(saved, found))
                }
            }, Modifier.fillMaxWidth().navigationBarsPadding(), enabled = !busy)
            Gap(20.dp)
        }
    }
}

/** Taps go: nothing → drizzle → rain → storm → nothing. */
private fun cycle(m: Map<Int, Int>, z: Int): Map<Int, Int> {
    val next = ((m[z] ?: 0) + 1) % 4
    return if (next == 0) m - z else m + (z to next)
}

/** A time of day stepped by a quarter of an hour. */
@Composable
private fun ClockRow(label: String, min: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = T.Label, modifier = Modifier.width(90.dp))
        IconBtn(Icons.Rounded.Remove, { onChange(((min - 15) % 1440 + 1440) % 1440) })
        Text(Words.clock(min), style = T.Number, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        IconBtn(Icons.Rounded.Add, { onChange((min + 15) % 1440) })
    }
}
