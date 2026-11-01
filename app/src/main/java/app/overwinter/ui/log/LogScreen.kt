package app.overwinter.ui.log

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Look
import app.overwinter.data.Settings
import app.overwinter.domain.Day
import app.overwinter.domain.Obs
import app.overwinter.domain.Telegram
import app.overwinter.domain.Weather
import app.overwinter.ui.Nav
import app.overwinter.ui.art.Marks.precip
import app.overwinter.ui.art.SkyMark
import app.overwinter.ui.art.StationMark
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.IconBtn
import app.overwinter.ui.components.Kind
import app.overwinter.ui.components.Round
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.Mono
import app.overwinter.ui.theme.T
import java.time.LocalDate

private const val WINDOW = 30

/** The barogram of the month, its named cyclones, and the journal day by day. */
@Composable
fun LogScreen(look: Look, s: Settings, @Suppress("UNUSED_PARAMETER") nav: Nav) {
    val ctx = LocalContext.current
    // A rolling tape of thirty days, like the barograph drum; the arrows turn it a whole tape back or forth.
    var end by remember { mutableStateOf(look.today) }
    val from = end.minusDays(WINDOW - 1L)
    val first = look.days.firstOrNull()?.date ?: look.today
    val days = look.days.filter { it.date >= from && it.date <= end }
    val fronts = look.fronts.filter { it.to >= from && it.from <= end }
    val title = Words.dateShort(ctx, from) + " – " + Words.dateShort(ctx, end)

    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.log_t), sub = stringResource(R.string.log_sub))
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val canBack = from > first
                    IconBtn(Icons.AutoMirrored.Rounded.ArrowBack, { if (canBack) end = end.minusDays(WINDOW.toLong()) }, tint = if (canBack) C.ink2 else C.line)
                    Text(title, style = T.Sign, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    val canNext = end < look.today
                    IconBtn(Icons.AutoMirrored.Rounded.ArrowForward, { if (canNext) end = minOf(look.today, end.plusDays(WINDOW.toLong())) }, tint = if (canNext) C.ink2 else C.line)
                }
                Gap(10.dp)
                Barogram(look, from, Modifier.fillMaxWidth().height(220.dp))
                Gap(6.dp)
                Text(stringResource(R.string.log_legend), style = T.Small)
                Gap(12.dp)
                if (fronts.isNotEmpty()) {
                    Caps(stringResource(R.string.fronts_t))
                    Gap(6.dp)
                    for (f in fronts) {
                        val dates = Words.dateShort(ctx, f.from) + " – " + Words.dateShort(ctx, f.to)
                        Text(
                            if (f.low) stringResource(if (f.ongoing) R.string.front_low_now else R.string.front_low, f.name, dates)
                            else stringResource(R.string.front_high, dates),
                            style = T.Body.copy(color = if (f.low) C.ink else C.ink2),
                        )
                        Gap(2.dp)
                    }
                    Gap(14.dp)
                }
                if (days.isEmpty()) {
                    Text(stringResource(R.string.log_empty), style = T.Small)
                }
            }
            items(days.reversed(), key = { it.date.toString() }) { d ->
                DayCard(d, look, s)
                Gap(10.dp)
            }
            item { Gap(20.dp) }
        }
    }
}

@Composable
private fun DayCard(d: Day, look: Look, s: Settings) {
    val ctx = LocalContext.current
    var open by remember { mutableStateOf<String?>(null) }
    Card(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(Words.date(ctx, d.date), style = T.Strong)
                val bits = buildList {
                    d.sleep?.let { add(ctx.getString(R.string.slept_x, Words.num(ctx, it))) }
                    d.hpa?.let { add(Words.num(ctx, it, 0) + " " + ctx.getString(R.string.hpa)) }
                }
                if (bits.isNotEmpty()) Text(bits.joinToString(" · "), style = T.MonoSmall)
            }
            SkyMark(d.sky, C.ink2, C.fair, Modifier.size(34.dp))
        }
        Gap(8.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (o in d.obs) {
                Column(
                    Modifier.clip(Round).border(1.dp, if (open == o.id) C.accent else C.line, Round).clickable { open = if (open == o.id) null else o.id }.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StationMark(o.wind, o.cloud, C.ink, Modifier.size(44.dp))
                    Text(Words.time(ctx, o.at), style = T.MonoSmall)
                    if (o.pain > 0) {
                        val col = if (o.pain == 3) C.accent else C.ice
                        Canvas(Modifier.size(16.dp)) { precip(Offset(size.width / 2, size.height / 2), size.width * .8f, o.pain, col, 1.5.dp.toPx()) }
                    }
                }
            }
        }
        val gear = d.gear.mapNotNull { id -> look.j.gear.firstOrNull { it.id == id } }
        if (gear.isNotEmpty()) {
            Gap(6.dp)
            Text(gear.joinToString("  ") { it.icon + " " + Words.gear(ctx, it) }, style = T.Small.copy(color = C.ink2))
        }
        for (o in d.obs) if (o.note.isNotEmpty()) {
            Gap(6.dp)
            Text(o.note, style = T.Hand)
        }
        val sel = d.obs.firstOrNull { it.id == open }
        if (sel != null) ObsDetail(sel, look, s) { open = null }
    }
}

@Composable
private fun ObsDetail(o: Obs, look: Look, s: Settings, onGone: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    var ask by remember { mutableStateOf(false) }
    val callsign = s.callsign.ifBlank { ctx.getString(R.string.callsign_default) }
    Gap(10.dp)
    val groups = Telegram.groups(o, callsign, Words.arr(ctx, R.array.zone_codes).toList(), { Words.gear(ctx, it, look.j.gear) })
    Text(groups.joinToString("  "), style = T.MonoSmall.copy(color = C.ink2))
    Gap(4.dp)
    val zones = Words.arr(ctx, R.array.zones)
    val levels = Words.arr(ctx, R.array.rain_levels)
    Text(
        Words.arr(ctx, R.array.beaufort)[o.wind] + " · " + Words.arr(ctx, R.array.okta)[o.cloud] + " · " + Words.arr(ctx, R.array.vis)[o.vis] +
            o.rain.joinToString("") { " · " + zones[it.zone] + ": " + levels[it.level] },
        style = T.Small,
    )
    Gap(8.dp)
    if (!ask) Btn(stringResource(R.string.obs_delete), { ask = true }, kind = Kind.GHOST)
    else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Btn(stringResource(R.string.obs_delete_yes), { c.write { delete(o.id) }; onGone() })
        Btn(stringResource(R.string.cancel), { ask = false }, kind = Kind.QUIET)
    }
}

@Composable
private fun Barogram(look: Look, from: LocalDate, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    val lowMark = stringResource(R.string.low_mark)
    val highMark = stringResource(R.string.high_mark)
    val paper = C.surface
    val grid = C.line
    val pencil = C.pencil
    val storm = C.storm
    val fair = C.fair
    val ice = C.ice
    val accent = C.accent
    val muted = C.muted
    val n = WINDOW
    val to = from.plusDays(n - 1L)
    // Days are numbered 1..n along the tape.
    fun idx(d: LocalDate) = (d.toEpochDay() - from.toEpochDay()).toInt() + 1
    val days = look.days.filter { it.date >= from && it.date <= to }.associateBy { idx(it.date) }
    val fronts = look.fronts.filter { it.to >= from && it.from <= to }
    val tapeByDay = look.tape.groupBy { java.time.Instant.ofEpochMilli(it.at).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }
        .filterKeys { it >= from && it <= to }.mapKeys { idx(it.key) }.mapValues { e -> e.value.map { it.hpa }.average().toFloat() }
    Canvas(modifier.clip(Round).border(1.dp, grid, Round)) {
        drawRect(paper)
        val top = 26.dp.toPx()
        val bottom = size.height - 22.dp.toPx()
        val left = 8.dp.toPx()
        val right = size.width - 8.dp.toPx()
        fun x(d: Float) = left + (right - left) * (d - 1) / (n - 1).coerceAtLeast(1)
        fun y(f: Float) = bottom - (bottom - top) * f
        for (d in 1..n) drawLine(grid.copy(alpha = if (from.plusDays(d - 1L).dayOfMonth == 1) 1f else .4f), Offset(x(d.toFloat()), top), Offset(x(d.toFloat()), bottom), 1f)
        for (k in 0..4) drawLine(grid.copy(alpha = .6f), Offset(left, y(k / 4f)), Offset(right, y(k / 4f)), 1f)
        // Lows and highs.
        for (f in fronts) {
            val a = idx(maxOf(f.from, from)).toFloat()
            val b = idx(minOf(f.to, to)).toFloat()
            val col = if (f.low) storm else fair
            drawRect(col.copy(alpha = .16f), Offset(x(a) - 6f, top), Size(x(b) - x(a) + 12f, bottom - top))
            val label = if (f.low) "$lowMark ${f.name}" else highMark
            val st = TextStyle(fontFamily = Mono, fontSize = 11.sp, color = if (f.low) storm else fair)
            val m = measurer.measure(label, st)
            drawText(m, topLeft = Offset((x(a) - 4f).coerceAtMost(right - m.size.width), 5.dp.toPx()))
        }
        // The low line: where it's a cyclone.
        drawLine(storm.copy(alpha = .5f), Offset(left, y(Weather.LOW)), Offset(right, y(Weather.LOW)), 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        // The pressure, scaled to the month.
        if (tapeByDay.size >= 2) {
            val lo = tapeByDay.values.min() - 2f
            val hi = tapeByDay.values.max() + 2f
            val p = Path()
            var started = false
            for (d in 1..n) {
                val v = tapeByDay[d] ?: continue
                val px = x(d.toFloat()); val py = y((v - lo) / (hi - lo))
                if (!started) { p.moveTo(px, py); started = true } else p.lineTo(px, py)
            }
            drawPath(p, ice.copy(alpha = .8f), style = Stroke(1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))))
        }
        // The pencil line of fair weather.
        val p = Path()
        var prev: Int? = null
        for (d in 1..n) {
            val day = days[d] ?: continue
            val px = x(d.toFloat()); val py = y(day.fair)
            if (prev == null || d - prev > 2) p.moveTo(px, py) else p.lineTo(px, py)
            prev = d
        }
        drawPath(p, pencil, style = Stroke(2.4.dp.toPx()))
        for ((d, day) in days) {
            drawCircle(pencil, 2.6.dp.toPx(), Offset(x(d.toFloat()), y(day.fair)))
            if (day.pain >= 2) precip(Offset(x(d.toFloat()), bottom - 7.dp.toPx()), 9.dp.toPx(), day.pain, if (day.pain == 3) accent else ice, 1.2.dp.toPx())
        }
        for (d in 1..n) {
            val date = from.plusDays(d - 1L)
            if (date.dayOfMonth % 5 != 0 && date.dayOfMonth != 1) continue
            val m = measurer.measure("${date.dayOfMonth}", TextStyle(fontFamily = Mono, fontSize = 10.sp, color = muted))
            drawText(m, topLeft = Offset((x(d.toFloat()) - m.size.width / 2).coerceIn(0f, size.width - m.size.width), bottom + 4.dp.toPx()))
        }
    }
}
