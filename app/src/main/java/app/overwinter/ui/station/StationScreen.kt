package app.overwinter.ui.station

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Look
import app.overwinter.data.Settings
import app.overwinter.domain.Rare
import app.overwinter.domain.Sky
import app.overwinter.domain.Slot
import app.overwinter.ui.Nav
import app.overwinter.ui.Overlay
import app.overwinter.ui.art.SceneState
import app.overwinter.ui.art.SkyMark
import app.overwinter.ui.art.StationMark
import app.overwinter.ui.art.StationScene
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.IconBtn
import app.overwinter.ui.components.Kind
import app.overwinter.ui.components.Round
import app.overwinter.ui.components.Small
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object Terms {
    /** The term an observation made at [now] belongs to: the last one begun (an hour early counts). */
    fun current(hours: List<Int>, now: LocalDateTime): Slot =
        Slot.entries.lastOrNull { now.hour + 1 >= hours[it.ordinal] } ?: Slot.MORNING
}

@Composable
fun StationScreen(look: Look, s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val now = Instant.ofEpochMilli(look.now).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val slot = Terms.current(s.hours, now)
    val todayObs = look.todayDay?.obs.orEmpty()
    val due = now.hour + 1 >= s.hours[slot.ordinal] && todayObs.none { it.slot == slot }
    val last = look.j.obs.maxByOrNull { it.at }
    val recent = look.j.seen.filter { look.now - it.at < 3 * 86_400_000L }.map { it.kind }.toSet()
    val callsign = s.callsign.ifBlank { stringResource(R.string.callsign_default) }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            stringResource(R.string.station_x, callsign),
            sub = stringResource(R.string.shift_day, look.shift.n, look.shift.day),
            right = { IconBtn(Icons.Outlined.Settings, { nav.open(Overlay.Settings) }, desc = stringResource(R.string.settings)) },
        )
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Gap(4.dp)
            StationScene(
                SceneState(look.built.toSet(), last?.wind ?: 3, last?.cloud ?: 2, look.drift, recent, C.dark),
                Modifier.fillMaxWidth().height(250.dp).clip(Round).border(1.dp, C.line, Round),
            )
            Gap(12.dp)

            val cyclone = look.fronts.lastOrNull { it.low && it.ongoing }
            if (look.drift >= 2) {
                Note(stringResource(R.string.drift_t), stringResource(R.string.drift_b, ctx.resources.getQuantityString(R.plurals.days, look.drift, look.drift)))
                Gap(12.dp)
            } else if (cyclone != null) {
                Note(stringResource(R.string.cyclone_now, cyclone.name), stringResource(R.string.cyclone_now_b, ctx.resources.getQuantityString(R.plurals.days, cyclone.days, cyclone.days)))
                Gap(12.dp)
            }

            // The term.
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.term))
                Gap(6.dp)
                val names = Words.arr(ctx, R.array.slot_terms)
                if (due) {
                    Text(stringResource(R.string.term_due, names[slot.ordinal], Words.hour(s.hours[slot.ordinal])), style = T.Heading)
                    Gap(4.dp)
                    Text(stringResource(R.string.term_due_b), style = T.Small)
                    Gap(12.dp)
                    Btn(stringResource(R.string.term_start), { nav.open(Overlay.Observe) }, Modifier.fillMaxWidth())
                } else {
                    val nextIdx = Slot.entries.firstOrNull { s.hours[it.ordinal] > now.hour + 1 && todayObs.none { o -> o.slot == it } }
                    Text(
                        if (nextIdx != null) stringResource(R.string.term_next, names[nextIdx.ordinal], Words.hour(s.hours[nextIdx.ordinal]))
                        else stringResource(R.string.term_tomorrow, Words.hour(s.hours[0])),
                        style = T.Heading,
                    )
                    Gap(12.dp)
                    Btn(stringResource(R.string.term_extra), { nav.open(Overlay.Observe) }, Modifier.fillMaxWidth(), kind = Kind.GHOST)
                }
                Gap(14.dp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val short = Words.arr(ctx, R.array.slots)
                    for (sl in Slot.entries) {
                        val o = todayObs.lastOrNull { it.slot == sl }
                        Column(
                            Modifier.weight(1f).clip(Small).border(1.dp, C.line, Small).padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(short[sl.ordinal], style = T.Label)
                            Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                                if (o != null) StationMark(o.wind, o.cloud, C.ink, Modifier.size(54.dp))
                                else Text("—", style = T.Heading.copy(color = C.muted))
                            }
                            Text(o?.let { Words.time(ctx, it.at) } ?: Words.hour(s.hours[sl.ordinal]), style = T.MonoSmall)
                        }
                    }
                }
            }
            Gap(12.dp)

            // The barometer.
            if (look.hpa != null || look.tape.isNotEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Caps(stringResource(R.string.baro))
                    Gap(6.dp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(look.hpa?.let { Words.num(ctx, it) } ?: "—", style = T.Number)
                        Text(" " + stringResource(R.string.hpa), style = T.Small, modifier = Modifier.padding(bottom = 4.dp))
                        Box(Modifier.weight(1f))
                        look.tendency3?.let {
                            Text((if (it > .3f) "↗ " else if (it < -.3f) "↘ " else "→ ") + stringResource(R.string.baro_3h, signed(ctx, it)), style = T.MonoSmall)
                        }
                    }
                    look.change24?.let { Text(stringResource(R.string.baro_24h, signed(ctx, it)), style = T.Small) }
                    look.pressureWarning?.let {
                        Gap(8.dp)
                        Text(stringResource(R.string.baro_warn), style = T.Strong.copy(color = C.accent))
                    }
                }
                Gap(12.dp)
            }

            // Forecasts.
            Forecasts(look, now)
            Gap(12.dp)

            // The station and its keeper.
            Card(Modifier.fillMaxWidth()) {
                Caps(stringResource(R.string.keeper))
                Gap(6.dp)
                Text(Words.arr(ctx, R.array.ranks)[look.rank.ordinal], style = T.Sign)
                Text(ctx.resources.getQuantityString(R.plurals.terms, look.terms, look.terms), style = T.Small)
                Gap(10.dp)
                val nb = look.next
                if (nb != null) {
                    val left = nb.terms - look.terms
                    Text(stringResource(R.string.next_building, Words.arr(ctx, R.array.buildings)[nb.ordinal], ctx.resources.getQuantityString(R.plurals.terms, left, left)), style = T.Body)
                    Gap(8.dp)
                    val prev = look.built.lastOrNull()?.terms ?: 0
                    Progress(((look.terms - prev).toFloat() / (nb.terms - prev).coerceAtLeast(1)))
                } else Text(stringResource(R.string.all_built), style = T.Body)
                Gap(12.dp)
                Text(look.built.joinToString(" · ") { Words.arr(ctx, R.array.buildings)[it.ordinal] }, style = T.Small)
                Gap(12.dp)
                val kinds = look.j.seen.map { it.kind }.toSet()
                Btn(stringResource(R.string.rare_open, kinds.size, Rare.entries.size), { nav.open(Overlay.Rare) }, Modifier.fillMaxWidth(), kind = Kind.QUIET)
            }
            Gap(24.dp)
        }
    }
}

private fun signed(ctx: android.content.Context, v: Float) = (if (v > 0) "+" else if (v < 0) "−" else "") + Words.num(ctx, kotlin.math.abs(v))

@Composable
private fun Note(title: String, body: String) {
    Card(Modifier.fillMaxWidth(), color = C.surface2) {
        Text(title, style = T.Heading)
        Gap(4.dp)
        Text(body, style = T.Small.copy(color = C.ink2))
    }
}

@Composable
fun Progress(f: Float) {
    Box(Modifier.fillMaxWidth().height(8.dp).clip(Small).border(1.dp, C.line, Small)) {
        Box(Modifier.fillMaxWidth(f.coerceIn(0.02f, 1f)).height(8.dp).clip(Small).background(C.accent))
    }
}

@Composable
private fun Forecasts(look: Look, now: LocalDateTime) {
    val ctx = LocalContext.current
    val c = ctx.container
    val skies = Words.arr(ctx, R.array.sky)
    Card(Modifier.fillMaxWidth()) {
        Caps(stringResource(R.string.forecast))
        Gap(6.dp)
        // How yesterday's forecast for today did, once today has its evening term or is over.
        val tf = look.todayForecast
        val td = look.todayDay
        if (tf != null && td != null) {
            Text(stringResource(R.string.fc_today, skies[tf.mine.ordinal], skies[td.sky.ordinal]), style = T.Body)
            Text(stringResource(if (tf.mine == td.sky) R.string.fc_hit else R.string.fc_miss), style = T.Small.copy(color = if (tf.mine == td.sky) C.aurora else C.muted))
            Gap(10.dp)
        }
        val tm = look.tomorrowForecast
        if (tm != null) {
            Text(stringResource(R.string.fc_tomorrow_mine, skies[tm.mine.ordinal]), style = T.Body)
            Text(stringResource(R.string.fc_tomorrow_station, skies[tm.station.ordinal]), style = T.Small)
        } else if (now.hour >= 17) {
            Text(stringResource(R.string.fc_ask), style = T.Heading)
            Gap(10.dp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (sky in Sky.entries) {
                    Column(
                        Modifier.weight(1f).clip(Small).border(1.dp, C.line, Small)
                            .clickable { c.write { forecast(sky, look.stationForecast(look.today.plusDays(1))) } }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        SkyMark(sky, C.ink, C.fair, Modifier.size(40.dp))
                        Text(skies[sky.ordinal], style = T.Label, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            Text(stringResource(R.string.fc_later), style = T.Small)
        }
        if (look.score.total > 0) {
            Gap(10.dp)
            Text(stringResource(R.string.fc_score, look.score.minePct, look.score.total), style = T.MonoSmall)
        }
    }
}
