package app.overwinter.ui.station

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.overwinter.R
import app.overwinter.data.Look
import app.overwinter.domain.Rare
import app.overwinter.ui.Nav
import app.overwinter.ui.components.Card
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.TopBar
import app.overwinter.ui.components.Words
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import java.time.Instant
import java.time.ZoneId

val Rare.icon: String get() = when (this) {
    Rare.AURORA -> "🌌"
    Rare.HALO -> "◎"
    Rare.PARHELION -> "☀"
    Rare.FOX -> "🦊"
    Rare.BEAR -> "🐻"
    Rare.OWL -> "🦉"
    Rare.DEER -> "🦌"
    Rare.ICEBREAKER -> "🚢"
    Rare.GREEN_RAY -> "💚"
    Rare.RADIOGRAM -> "📻"
}

/** The journal of rare phenomena: what has been seen from the station, and how to see the rest. */
@Composable
fun RarePage(look: Look, nav: Nav) {
    val ctx = LocalContext.current
    val names = Words.arr(ctx, R.array.rare_names)
    val texts = Words.arr(ctx, R.array.rare_texts)
    val hints = Words.arr(ctx, R.array.rare_hints)
    Column(Modifier.fillMaxSize()) {
        TopBar(stringResource(R.string.rare_t), onBack = { nav.close() })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.rare_b), style = T.Small)
            Gap(12.dp)
            for (k in Rare.entries) {
                val seen = look.j.seen.filter { it.kind == k }
                Card(Modifier.fillMaxWidth(), color = if (seen.isEmpty()) C.surface2 else C.surface) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (seen.isEmpty()) "?" else k.icon, style = T.Heading.copy(fontSize = 28.sp), modifier = Modifier.width(48.dp).alpha(if (seen.isEmpty()) .5f else 1f))
                        Column(Modifier.weight(1f)) {
                            Text(if (seen.isEmpty()) stringResource(R.string.rare_unknown) else names[k.ordinal], style = T.Strong)
                            Text(if (seen.isEmpty()) hints[k.ordinal] else texts[k.ordinal], style = T.Small)
                            if (seen.isNotEmpty()) {
                                val lastDay = Instant.ofEpochMilli(seen.maxOf { it.at }).atZone(ZoneId.systemDefault()).toLocalDate()
                                Text(stringResource(R.string.rare_seen, ctx.resources.getQuantityString(R.plurals.times, seen.size, seen.size), Words.date(ctx, lastDay)), style = T.MonoSmall.copy(color = C.ice))
                            }
                        }
                    }
                }
                Gap(8.dp)
            }
            Gap(16.dp)
        }
    }
}
