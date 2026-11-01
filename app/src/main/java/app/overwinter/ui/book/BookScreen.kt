package app.overwinter.ui.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Look
import app.overwinter.data.Settings
import app.overwinter.domain.Rule
import app.overwinter.domain.Station
import app.overwinter.domain.Verdict
import app.overwinter.domain.Weather
import app.overwinter.report.Report
import app.overwinter.ui.Nav
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Chip
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.Kind
import app.overwinter.ui.components.Ornament
import app.overwinter.ui.components.Toast
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.station.Progress
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import kotlinx.coroutines.launch

/** The observer's manual: what the station noticed, how the forecasts did, the shifts, the report for a doctor. */
@Composable
fun BookScreen(look: Look, s: Settings, @Suppress("UNUSED_PARAMETER") nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    var showStruck by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.book_t), sub = stringResource(R.string.book_sub))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Gap(4.dp)
            Text(stringResource(R.string.book_b), style = T.Small)
            Gap(14.dp)
            val kept = look.kept
            if (kept.isEmpty()) {
                Card(Modifier.fillMaxWidth(), color = C.surface2) {
                    Text(stringResource(R.string.book_empty), style = T.Body)
                    Gap(10.dp)
                    val n = look.days.size
                    Text(stringResource(R.string.book_days, n), style = T.MonoSmall)
                    Gap(6.dp)
                    Progress(n / 21f)
                }
            }
            kept.forEachIndexed { i, r -> RuleCard(i + 1, r, look) { v -> c.write { verdict(r.key, v) } }; Gap(10.dp) }
            if (look.struck.isNotEmpty()) {
                Text(
                    stringResource(R.string.book_struck, look.struck.size),
                    style = T.Label.copy(color = C.muted),
                    modifier = Modifier.clickable { showStruck = !showStruck }.padding(vertical = 6.dp),
                )
                if (showStruck) for (r in look.struck) {
                    Text(Words.rule(ctx, r, look.j.gear), style = T.Small.copy(textDecoration = TextDecoration.LineThrough))
                    Text(stringResource(R.string.book_restore), style = T.Label.copy(color = C.accent), modifier = Modifier.clickable { c.write { verdict(r.key, Verdict.NEW) } }.padding(vertical = 4.dp))
                    Gap(6.dp)
                }
            }
            Gap(10.dp)
            Ornament()
            Gap(14.dp)

            // Forecasts.
            Caps(stringResource(R.string.score_t))
            Gap(6.dp)
            val sc = look.score
            if (sc.total == 0) Text(stringResource(R.string.score_none), style = T.Small)
            else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(Modifier.weight(1f)) {
                        Text("${sc.minePct}%", style = T.Number.copy(color = C.accent))
                        Text(stringResource(R.string.score_mine), style = T.Small)
                    }
                    Card(Modifier.weight(1f)) {
                        Text("${sc.stationPct}%", style = T.Number)
                        Text(stringResource(R.string.score_station), style = T.Small)
                    }
                }
                Gap(6.dp)
                Text(ctx.resources.getQuantityString(R.plurals.forecasts, sc.total, sc.total), style = T.Small)
            }
            Gap(18.dp)

            // Shifts.
            Caps(stringResource(R.string.shifts_t))
            Gap(6.dp)
            Card(Modifier.fillMaxWidth()) {
                val sh = look.shift
                Text(stringResource(R.string.shift_now, sh.n, sh.day, Station.SHIFT), style = T.Strong)
                Gap(6.dp)
                Progress(sh.day / Station.SHIFT.toFloat())
                for (old in Station.shifts(s.startDate, look.today).reversed()) {
                    Gap(12.dp)
                    val ds = look.days.filter { it.date >= old.from && it.date <= old.to }
                    val obs = ds.sumOf { it.obs.size }
                    val lows = look.fronts.count { it.low && it.from >= old.from && it.from <= old.to }
                    val fair = if (ds.isEmpty()) 0 else (ds.count { it.fair >= Weather.LOW } * 100 / ds.size)
                    Text(stringResource(R.string.shift_old, old.n, Words.dateShort(ctx, old.from), Words.dateShort(ctx, old.to)), style = T.Strong)
                    Text(stringResource(R.string.shift_sum, obs, lows, fair), style = T.Small)
                }
            }
            Gap(18.dp)

            // The doctor.
            Caps(stringResource(R.string.report_t))
            Gap(6.dp)
            Card(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.report_b), style = T.Small.copy(color = C.ink2))
                Gap(10.dp)
                var days by remember { mutableIntStateOf(30) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (d in listOf(14, 30, 90)) Chip(ctx.resources.getQuantityString(R.plurals.days, d, d), days == d, { days = d })
                }
                Gap(12.dp)
                Btn(stringResource(R.string.report_make), {
                    scope.launch {
                        val f = Report.make(ctx, look, s, days)
                        if (f == null) Toast.show(ctx.getString(R.string.report_empty))
                        else runCatching { ctx.startActivity(c.shareIntent(f, "application/pdf").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    }
                }, Modifier.fillMaxWidth(), kind = Kind.QUIET)
            }
            Gap(24.dp)
        }
    }
}

@Composable
private fun RuleCard(n: Int, r: Rule, look: Look, onVerdict: (Verdict) -> Unit) {
    val ctx = LocalContext.current
    val v = look.j.verdicts[r.key] ?: Verdict.NEW
    Card(Modifier.fillMaxWidth(), color = if (v == Verdict.KEPT) C.surface else C.surface2) {
        Row {
            Text("§ $n", style = T.Sign.copy(color = if (r.good) C.aurora else C.accent))
            Gap(0.dp)
        }
        Gap(4.dp)
        Text(Words.rule(ctx, r, look.j.gear), style = T.Body)
        Gap(4.dp)
        Text(Words.evidence(ctx, r), style = T.MonoSmall)
        Gap(10.dp)
        if (v == Verdict.KEPT) {
            Row {
                Text(stringResource(R.string.rule_kept), style = T.Label.copy(color = C.aurora), modifier = Modifier.weight(1f))
                Text(stringResource(R.string.rule_strike), style = T.Label.copy(color = C.muted), modifier = Modifier.clickable { onVerdict(Verdict.STRUCK) })
            }
        } else Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Btn(stringResource(R.string.rule_keep), { onVerdict(Verdict.KEPT) }, Modifier.weight(1f))
            Btn(stringResource(R.string.rule_strike), { onVerdict(Verdict.STRUCK) }, Modifier.weight(1f), kind = Kind.GHOST)
        }
    }
}
