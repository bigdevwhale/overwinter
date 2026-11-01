package app.overwinter.ui.observe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.overwinter.R
import app.overwinter.data.Look
import app.overwinter.data.Settings
import app.overwinter.domain.Obs
import app.overwinter.domain.Rare
import app.overwinter.domain.Telegram
import app.overwinter.ui.Nav
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.Small
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.station.icon
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** The telegram goes out on the tape, the key sounds, and the mainland answers. */
@Composable
fun SentScreen(o: Obs, rare: List<Rare>, look: Look, s: Settings, nav: Nav) {
    val ctx = LocalContext.current
    val callsign = s.callsign.ifBlank { stringResource(R.string.callsign_default) }
    val groups = remember(o) { Telegram.groups(o, callsign, Words.arr(ctx, R.array.zone_codes).toList(), { Words.gear(ctx, it, look.j.gear) }) }
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(o.id) {
        if (s.sound) launch { Key.play(groups.take(3).joinToString(" ")) }
        for (i in groups.indices) { delay(260); shown = i + 1 }
    }
    val done = shown >= groups.size
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.sent_t), onBack = { nav.close() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp)) {
            Gap(6.dp)
            // The paper tape.
            Column(
                Modifier.fillMaxWidth().clip(Small).background(androidx.compose.ui.graphics.Color(0xFFF3EBD6))
                    .border(1.dp, androidx.compose.ui.graphics.Color(0xFFD9C9A2), Small).padding(16.dp),
            ) {
                val ink = androidx.compose.ui.graphics.Color(0xFF2B2A4C)
                Text(stringResource(R.string.sent_head), style = T.MonoSmall.copy(color = ink.copy(alpha = .6f)))
                Gap(8.dp)
                groups.take(shown).chunked(3).forEach { line ->
                    Text(line.joinToString("   "), style = T.Tape.copy(color = ink))
                }
                if (!done) Text("▌", style = T.Tape.copy(color = ink))
            }
            Gap(10.dp)
            Text(stringResource(R.string.sent_legend), style = T.Small)
            AnimatedVisibility(done, enter = fadeIn()) {
                Column {
                    Gap(18.dp)
                    Text(stringResource(R.string.sent_ok), style = T.Sign)
                    Gap(4.dp)
                    Text(stringResource(R.string.sent_ok_b), style = T.Small)
                    if (o.note.isNotEmpty()) {
                        Gap(12.dp)
                        Text(o.note, style = T.Hand)
                    }
                    val names = Words.arr(ctx, R.array.rare_names)
                    val texts = Words.arr(ctx, R.array.rare_texts)
                    for (r in rare) {
                        Gap(12.dp)
                        Card(Modifier.fillMaxWidth(), color = C.surface2) {
                            Caps(stringResource(R.string.rare_new))
                            Gap(6.dp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(r.icon, style = T.Heading.copy(fontSize = 30.sp), modifier = Modifier.width(50.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(names[r.ordinal], style = T.Strong)
                                    Text(texts[r.ordinal], style = T.Small)
                                }
                            }
                        }
                    }
                    Gap(20.dp)
                    Btn(stringResource(R.string.done), { nav.close() }, Modifier.fillMaxWidth())
                    Gap(20.dp)
                }
            }
        }
    }
}
