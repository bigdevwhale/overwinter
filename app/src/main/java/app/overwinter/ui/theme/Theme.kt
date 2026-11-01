package app.overwinter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.overwinter.R

/** Light: the polar day, snow and a red-orange station. Dark: the polar night, a warm window and the aurora. */
@Immutable
data class Palette(
    val dark: Boolean,
    val bg: Color,
    val bg2: Color,
    val surface: Color,
    val surface2: Color,
    val line: Color,
    val ink: Color,
    val ink2: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val ice: Color,
    /** The violet of a copying pencil, for the hand-written journal. */
    val pencil: Color,
    val aurora: Color,
    val storm: Color,
    val fair: Color,
    val snow: Color,
    val scrim: Color,
)

val Light = Palette(
    dark = false,
    bg = Color(0xFFEDF2F5), bg2 = Color(0xFFDDE7EE), surface = Color(0xFFFBFDFE), surface2 = Color(0xFFE6EDF2), line = Color(0xFFC9D6E0),
    ink = Color(0xFF15253A), ink2 = Color(0xFF3B4D63), muted = Color(0xFF74859A),
    accent = Color(0xFFCF4426), onAccent = Color(0xFFFFF6F0), ice = Color(0xFF3A78AE),
    pencil = Color(0xFF4A3D9C), aurora = Color(0xFF2FA57A), storm = Color(0xFF5B6B86), fair = Color(0xFFE09A1C),
    snow = Color(0xFFFFFFFF), scrim = Color(0x9915253A),
)

val Dark = Palette(
    dark = true,
    bg = Color(0xFF0A1322), bg2 = Color(0xFF111D33), surface = Color(0xFF14223B), surface2 = Color(0xFF1B2C49), line = Color(0x334E7AB0),
    ink = Color(0xFFE5EDF6), ink2 = Color(0xFFB8C7D9), muted = Color(0xFF7D8FA8),
    accent = Color(0xFFFF8152), onAccent = Color(0xFF241005), ice = Color(0xFF8DB9E8),
    pencil = Color(0xFFB9ACFF), aurora = Color(0xFF6CEFB4), storm = Color(0xFF7F90AE), fair = Color(0xFFFFC65A),
    snow = Color(0xFFDCE6F2), scrim = Color(0xCC03070E),
)

object C {
    var p by mutableStateOf(Dark)
    val dark get() = p.dark
    val bg get() = p.bg
    val bg2 get() = p.bg2
    val surface get() = p.surface
    val surface2 get() = p.surface2
    val line get() = p.line
    val ink get() = p.ink
    val ink2 get() = p.ink2
    val muted get() = p.muted
    val accent get() = p.accent
    val onAccent get() = p.onAccent
    val ice get() = p.ice
    val pencil get() = p.pencil
    val aurora get() = p.aurora
    val storm get() = p.storm
    val fair get() = p.fair
    val snow get() = p.snow
    val scrim get() = p.scrim
}

/** PT Sans Narrow by ParaType (SIL OFL): stencilled station signs. */
val SignFont = FontFamily(Font(R.font.pt_sans_narrow_bold, FontWeight.Bold))

/** PT Serif by ParaType (SIL OFL): the text. */
val Serif = FontFamily(
    Font(R.font.pt_serif_regular, FontWeight.Normal),
    Font(R.font.pt_serif_bold, FontWeight.Bold),
    Font(R.font.pt_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

/** PT Mono by ParaType (SIL OFL): the telegraph tape and the instruments. */
val Mono = FontFamily(Font(R.font.pt_mono))

/** Caveat by Impallari Type (SIL OFL): the watch journal, in copying pencil. */
val Hand = FontFamily(Font(R.font.caveat))

object T {
    val Hero get() = TextStyle(fontFamily = SignFont, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 42.sp, letterSpacing = 1.sp, color = C.ink)
    val Title get() = TextStyle(fontFamily = SignFont, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 31.sp, letterSpacing = .6.sp, color = C.ink)
    val Sign get() = TextStyle(fontFamily = SignFont, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 25.sp, letterSpacing = .5.sp, color = C.ink)
    val Heading get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, color = C.ink)
    val Body get() = TextStyle(fontFamily = Serif, fontSize = 15.5.sp, lineHeight = 22.sp, color = C.ink)
    val Strong get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 15.5.sp, lineHeight = 21.sp, color = C.ink)
    val Italic get() = TextStyle(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 23.sp, color = C.ink2)
    val Small get() = TextStyle(fontFamily = Serif, fontSize = 13.sp, lineHeight = 18.sp, color = C.muted)
    val Label get() = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 17.sp, color = C.ink2)
    val Caps get() = TextStyle(fontFamily = SignFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 2.sp, color = C.accent)
    val Button get() = TextStyle(fontFamily = SignFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 20.sp, letterSpacing = 1.sp)
    val Tape get() = TextStyle(fontFamily = Mono, fontSize = 17.sp, lineHeight = 24.sp, letterSpacing = 1.sp, color = C.ink)
    val MonoSmall get() = TextStyle(fontFamily = Mono, fontSize = 12.sp, lineHeight = 16.sp, color = C.muted)
    val Number get() = TextStyle(fontFamily = Mono, fontSize = 30.sp, lineHeight = 34.sp, color = C.ink)
    val Hand get() = TextStyle(fontFamily = app.overwinter.ui.theme.Hand, fontSize = 22.sp, lineHeight = 25.sp, color = C.pencil)
}

/** 0: as the system, 1: polar day, 2: polar night. */
@Composable
fun OverwinterTheme(mode: Int = 0, content: @Composable () -> Unit) {
    val dark = when (mode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }
    val p = if (dark) Dark else Light
    if (C.p != p) C.p = p
    val view = androidx.compose.ui.platform.LocalView.current
    SideEffect {
        if (C.p != p) C.p = p
        (view.context as? android.app.Activity)?.window?.let { w ->
            androidx.core.view.WindowCompat.getInsetsController(w, view).apply { isAppearanceLightStatusBars = !dark; isAppearanceLightNavigationBars = !dark }
        }
    }
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.bg, surface = p.surface, onSurface = p.ink, onBackground = p.ink, outline = p.line, error = p.accent)
        else lightColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.bg, surface = p.surface, onSurface = p.ink, onBackground = p.ink, outline = p.line, error = p.accent),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
