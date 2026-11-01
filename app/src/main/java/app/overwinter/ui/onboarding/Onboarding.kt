package app.overwinter.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Settings
import app.overwinter.domain.Building
import app.overwinter.ui.art.SceneState
import app.overwinter.ui.art.StationMark
import app.overwinter.ui.art.StationScene
import app.overwinter.ui.components.Btn
import app.overwinter.ui.components.Caps
import app.overwinter.ui.components.Field
import app.overwinter.ui.components.Gap
import app.overwinter.ui.components.Kind
import app.overwinter.ui.components.Round
import app.overwinter.ui.components.room
import app.overwinter.ui.theme.C
import app.overwinter.ui.theme.T
import java.time.LocalDate

@Composable
fun Onboarding(s: Settings, start: Int = 0) {
    val ctx = LocalContext.current
    val c = ctx.container
    var page by remember { mutableIntStateOf(start) }
    var callsign by remember { mutableStateOf(s.callsign) }
    val notify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    Column(Modifier.fillMaxSize().background(C.bg).room().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Gap(18.dp)
            when (page) {
                0 -> {
                    StationScene(
                        SceneState(setOf(Building.HUT, Building.SCREEN, Building.VANE, Building.MAST, Building.DOG), 4, 2, 0, emptySet(), C.dark),
                        Modifier.fillMaxWidth().height(250.dp).clip(Round).border(1.dp, C.line, Round),
                    )
                    Gap(22.dp)
                    Text(stringResource(R.string.app_name), style = T.Hero)
                    Gap(8.dp)
                    Text(stringResource(R.string.ob1), style = T.Body)
                    Gap(10.dp)
                    Text(stringResource(R.string.ob1b), style = T.Italic)
                }
                1 -> {
                    Caps(stringResource(R.string.ob2_t))
                    Gap(10.dp)
                    Text(stringResource(R.string.ob2), style = T.Body)
                    Gap(16.dp)
                    for ((w, cl, r) in listOf(Triple(2, 7, R.string.ob2_a), Triple(6, 3, R.string.ob2_b), Triple(9, 0, R.string.ob2_c))) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StationMark(w, cl, C.ink, Modifier.size(64.dp))
                            Box(Modifier.width(10.dp))
                            Text(stringResource(r), style = T.Small.copy(color = C.ink2), modifier = Modifier.weight(1f))
                        }
                        Gap(8.dp)
                    }
                    Gap(8.dp)
                    Text(stringResource(R.string.ob2_rest), style = T.Body)
                    Gap(10.dp)
                    Text(stringResource(R.string.ob2_rare), style = T.Italic)
                }
                else -> {
                    Caps(stringResource(R.string.ob3_t))
                    Gap(10.dp)
                    Text(stringResource(R.string.ob3), style = T.Body)
                    Gap(14.dp)
                    Field(callsign, { callsign = it.take(12) }, Modifier.fillMaxWidth(), stringResource(R.string.callsign_default))
                    Gap(6.dp)
                    Text(stringResource(R.string.callsign_note), style = T.Small)
                    Gap(18.dp)
                    Text(stringResource(R.string.ob3_terms), style = T.Body)
                    Gap(10.dp)
                    Text(stringResource(R.string.ob3_private), style = T.Small)
                }
            }
            Gap(20.dp)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.Center) {
            for (i in 0..2) Box(Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(if (i == page) C.accent else C.line))
        }
        Gap(12.dp)
        Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (page > 0) Btn(stringResource(R.string.back), { page-- }, Modifier.weight(1f), kind = Kind.GHOST)
            if (page < 2) Btn(stringResource(R.string.next), { page++ }, Modifier.weight(1f))
            else Btn(stringResource(R.string.open_station), {
                if (android.os.Build.VERSION.SDK_INT >= 33) notify.launch(Manifest.permission.POST_NOTIFICATIONS)
                c.write {
                    settings.update { it.copy(onboarded = true, callsign = callsign.trim(), started = if (journal.value.obs.isEmpty()) LocalDate.now().toEpochDay() else it.started) }
                    replan()
                }
            }, Modifier.weight(1f))
        }
        Gap(16.dp)
    }
}
