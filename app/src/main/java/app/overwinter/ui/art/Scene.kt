package app.overwinter.ui.art

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import app.overwinter.domain.Building
import app.overwinter.domain.Rare
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

/** What the scene shows: the station as built, the weather of the last term, the snow since, the guests. */
data class SceneState(
    val built: Set<Building>,
    val wind: Int,
    val cloud: Int,
    val drift: Int,
    val guests: Set<Rare>,
    val night: Boolean,
)

private object Pal {
    val red = Color(0xFFD2452B)
    val redDark = Color(0xFF9C2F1C)
    val wood = Color(0xFF7A5434)
    val woodDark = Color(0xFF52361F)
    val window = Color(0xFFFFD27A)
    val white = Color(0xFFF7FAFC)
    val steel = Color(0xFF55657A)
    val glass = Color(0xFFBFE3F2)
    val green = Color(0xFF4E9A5B)
    val hull = Color(0xFF2B3442)
}

@Composable
fun StationScene(s: SceneState, modifier: Modifier, still: Boolean = false) {
    val inf = rememberInfiniteTransition(label = "scene")
    val t by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Restart), label = "t")
    Canvas(modifier) { scene(s, if (still) 0.3f else t) }
}

fun DrawScope.scene(s: SceneState, t: Float) {
    val w = size.width
    val h = size.height
    val ground = h * .72f
    val u = min(w / 360f, h / 260f)

    // Sky.
    val sky = if (s.night) listOf(Color(0xFF050B18), Color(0xFF0D1E3A), Color(0xFF1C3358)) else listOf(Color(0xFF9CC3E4), Color(0xFFC7DEEF), Color(0xFFE9F1F7))
    drawRect(Brush.verticalGradient(sky, 0f, ground))
    if (s.night) {
        for (i in 0 until 60) {
            val x = ((i * 73 + 11) % 360) / 360f * w
            val y = ((i * 37 + 5) % 150) / 150f * ground * .8f
            val tw = .4f + .6f * abs(sin((t * 2 * PI + i).toFloat()))
            drawCircle(Color.White.copy(alpha = .25f + .5f * tw * (1f - s.cloud / 10f)), (if (i % 7 == 0) 1.6f else 1f) * u, Offset(x, y))
        }
    } else {
        drawCircle(Color(0xFFFFF3D2).copy(alpha = .9f), 22f * u, Offset(w * .82f, ground * .28f))
        drawCircle(Color(0xFFFFF3D2).copy(alpha = .25f), 34f * u, Offset(w * .82f, ground * .28f))
    }
    if (Rare.AURORA in s.guests) aurora(w, ground, u, t, s.night)

    // Far hills and the sea strip.
    val sea = ground * .86f
    drawRect(if (s.night) Color(0xFF16294A) else Color(0xFF7FA6C6), Offset(0f, sea), Size(w * .45f, ground - sea))
    if (Building.ICEBREAKER in s.built) icebreaker(Offset(w * (.08f + .05f * sin((t * 2 * PI).toFloat())), sea + 2f * u), u)
    val hills = Path().apply {
        moveTo(w * .38f, ground)
        cubicTo(w * .5f, sea - 30f * u, w * .62f, sea - 8f * u, w * .72f, sea - 20f * u)
        cubicTo(w * .82f, sea - 36f * u, w * .94f, sea - 10f * u, w, sea - 14f * u)
        lineTo(w, ground); close()
    }
    drawPath(hills, if (s.night) Color(0xFF2A3E5E) else Color(0xFFD5E2EC))
    if (Rare.BEAR in s.guests) bear(Offset(w * .66f, sea - 10f * u), u * .55f, s.night)

    // Clouds drift with the wind.
    val n = s.cloud
    for (i in 0 until n) {
        val speed = .3f + s.wind / 12f
        val x = ((i * 0.37f + t * speed) % 1.3f - .15f) * w
        val y = ground * (.12f + (i % 4) * .1f)
        cloud(Offset(x, y), u * (0.8f + (i % 3) * .25f), if (s.night) Color(0xFF3A4C6C) else Color.White, alpha = .55f + n * .04f)
    }

    // Ground.
    drawRect(Brush.verticalGradient(if (s.night) listOf(Color(0xFF3B5277), Color(0xFF1E2E4A)) else listOf(Color(0xFFF7FBFE), Color(0xFFD7E5F0)), ground, h), Offset(0f, ground), Size(w, h - ground))

    val b = s.built
    // Back row.
    if (Building.DOME in b) dome(Offset(w * .1f, ground), u, s.night)
    if (Building.BEACON in b) beacon(Offset(w * .2f, ground), u, t, s.night)
    if (Building.RADIO in b) radio(Offset(w * .3f, ground), u, s.night)
    if (Building.GREENHOUSE in b) greenhouse(Offset(w * .6f, ground - 4f * u), u, s.night)
    if (Building.BANYA in b) banya(Offset(w * .7f, ground), u, t, s.night)
    if (Building.WINDMILL in b) windmill(Offset(w * .92f, ground), u, t, s.wind)
    if (Building.MAST in b) mast(Offset(w * .82f, ground), u, t, s.wind, Rare.OWL in s.guests)
    // The hut is always there.
    hut(Offset(w * .46f, ground + 4f * u), u, t, s.night, s.wind)
    if (Building.SCREEN in b) screen(Offset(w * .25f, ground + 12f * u), u)
    if (Building.VANE in b) vane(Offset(w * .15f, ground + 14f * u), u, t, s.wind)
    if (Building.HELIPAD in b) helipad(Offset(w * .78f, ground + 34f * u), u)
    if (Building.DOG in b) dog(Offset(w * .6f, ground + 30f * u), u, t)
    if (Rare.FOX in s.guests) fox(Offset(w * .36f, ground + 40f * u), u)
    if (Rare.DEER in s.guests) tracks(Offset(w * .05f, ground + 48f * u), u, w)
    if (Building.BALLOON in b) balloon(Offset(w * .55f, ground - (40f + 150f * t) * u), u, (1f - t).coerceIn(0f, 1f))

    // Snow that gathered while nobody observed.
    if (s.drift > 0) {
        val k = (s.drift / 7f).coerceAtMost(1f)
        val drift = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h - 10f * u * (1 + k * 2))
            cubicTo(w * .3f, h - 40f * u * k - 8f * u, w * .5f, h - 58f * u * k, w * .62f, h - 30f * u * k - 6f * u)
            cubicTo(w * .8f, h - 10f * u, w * .9f, h - 22f * u * k, w, h - 12f * u)
            lineTo(w, h); close()
        }
        drawPath(drift, if (s.night) Color(0xFFCAD8EA) else Color.White)
    }

    // Snowfall, slanted by the wind.
    val flakes = (s.wind * 6 + if (s.cloud >= 6) 25 else 0).coerceAtMost(90)
    val slant = s.wind / 12f
    for (i in 0 until flakes) {
        val fx = ((i * 97 % 360) / 360f + t * (0.4f + slant * 2f) + i * .013f) % 1f
        val fy = ((i * 53 % 260) / 260f + t * (1.5f + (i % 3) * .5f)) % 1f
        drawCircle(Color.White.copy(alpha = .8f), (1f + (i % 3) * .6f) * u, Offset(fx * w - slant * fy * 60f * u, fy * h))
    }
}

private fun DrawScope.aurora(w: Float, ground: Float, u: Float, t: Float, night: Boolean) {
    val greens = listOf(Color(0x006CEFB4), Color(0xAA6CEFB4), Color(0x336CB4EF), Color(0x00000000))
    for (band in 0 until 3) {
        val p = Path()
        val base = ground * (.18f + band * .1f)
        p.moveTo(0f, base)
        var x = 0f
        while (x <= w) {
            val y = base + sin((x / w * 3 + t * 2 + band).toDouble() * PI).toFloat() * 14f * u
            p.lineTo(x, y)
            x += 8f
        }
        p.lineTo(w, base + 70f * u); p.lineTo(0f, base + 70f * u); p.close()
        drawPath(p, Brush.verticalGradient(greens, base - 20f * u, base + 70f * u), alpha = if (night) .7f - band * .15f else .35f)
    }
}

private fun DrawScope.cloud(c: Offset, u: Float, col: Color, alpha: Float) {
    val a = alpha.coerceAtMost(.95f)
    drawOval(col.copy(alpha = a), Offset(c.x - 30f * u, c.y - 8f * u), Size(60f * u, 18f * u))
    drawCircle(col.copy(alpha = a), 12f * u, Offset(c.x - 8f * u, c.y - 6f * u))
    drawCircle(col.copy(alpha = a), 9f * u, Offset(c.x + 10f * u, c.y - 4f * u))
}

private fun DrawScope.hut(base: Offset, u: Float, t: Float, night: Boolean, wind: Int) {
    val w = 84f * u; val hh = 42f * u
    val x = base.x - w / 2; val y = base.y - hh
    drawRect(Pal.red, Offset(x, y), Size(w, hh))
    for (i in 1..4) drawLine(Pal.redDark.copy(alpha = .5f), Offset(x, y + i * hh / 5), Offset(x + w, y + i * hh / 5), 1f * u)
    val roof = Path().apply { moveTo(x - 6f * u, y); lineTo(base.x, y - 24f * u); lineTo(x + w + 6f * u, y); close() }
    drawPath(roof, if (night) Color(0xFFDCE6F2) else Pal.white)
    drawPath(roof, Pal.redDark.copy(alpha = .4f), style = Stroke(1.2f * u))
    val win = if (night) Pal.window else Color(0xFFB9D5EA)
    drawRect(win, Offset(x + 10f * u, y + 12f * u), Size(16f * u, 14f * u))
    drawRect(win, Offset(x + w - 26f * u, y + 12f * u), Size(16f * u, 14f * u))
    if (night) drawCircle(Pal.window.copy(alpha = .18f), 30f * u, Offset(x + 18f * u, y + 19f * u))
    drawRect(Pal.woodDark, Offset(base.x - 7f * u, y + 16f * u), Size(14f * u, hh - 16f * u))
    // Chimney and smoke bent by the wind.
    drawRect(Pal.steel, Offset(x + w * .7f, y - 22f * u), Size(7f * u, 14f * u))
    for (i in 0 until 5) {
        val k = ((t * 3 + i * .2f) % 1f)
        val sx = x + w * .7f + 3.5f * u + k * (10f + wind * 6f) * u
        val sy = y - 24f * u - k * 34f * u
        drawCircle((if (night) Color(0xFF9FB0C8) else Color(0xFFB8C4D0)).copy(alpha = .5f * (1 - k)), (3f + k * 6f) * u, Offset(sx, sy))
    }
    // The station sign.
    drawRect(Pal.white, Offset(base.x - 16f * u, y + 3f * u), Size(32f * u, 7f * u))
    drawRect(Pal.redDark, Offset(base.x - 13f * u, y + 5.5f * u), Size(26f * u, 2f * u))
}

private fun DrawScope.screen(base: Offset, u: Float) {
    val w = 18f * u; val hh = 16f * u
    for (dx in listOf(-6f, 6f)) drawLine(Pal.white, Offset(base.x + dx * u, base.y), Offset(base.x + dx * u, base.y - 20f * u), 1.8f * u)
    drawRect(Pal.white, Offset(base.x - w / 2, base.y - 20f * u - hh), Size(w, hh))
    for (i in 1..4) drawLine(Color(0xFFB8C6D4), Offset(base.x - w / 2 + 2f * u, base.y - 20f * u - hh + i * hh / 5), Offset(base.x + w / 2 - 2f * u, base.y - 20f * u - hh + i * hh / 5), 1f * u)
    val roof = Path().apply { moveTo(base.x - w / 2 - 2f * u, base.y - 20f * u - hh); lineTo(base.x, base.y - 26f * u - hh); lineTo(base.x + w / 2 + 2f * u, base.y - 20f * u - hh); close() }
    drawPath(roof, Pal.white)
}

private fun DrawScope.vane(base: Offset, u: Float, t: Float, wind: Int) {
    val top = Offset(base.x, base.y - 52f * u)
    drawLine(Pal.steel, base, top, 2f * u)
    // The arrow points where the wind goes; the cups spin with it.
    drawLine(Pal.steel, top + Offset(-12f * u, 0f), top + Offset(12f * u, 0f), 1.6f * u)
    val head = Path().apply { moveTo(top.x + 14f * u, top.y); lineTo(top.x + 8f * u, top.y - 4f * u); lineTo(top.x + 8f * u, top.y + 4f * u); close() }
    drawPath(head, Pal.red)
    val spin = t * 360f * (wind + 1) * 2
    rotate(spin, top + Offset(0f, -10f * u)) {
        for (k in 0 until 3) {
            val a = k * 2 * PI / 3
            val p = top + Offset(0f, -10f * u) + Offset((kotlin.math.cos(a) * 8 * u).toFloat(), (sin(a) * 2.5 * u).toFloat())
            drawCircle(Pal.steel, 2.6f * u, p)
        }
    }
}

private fun DrawScope.mast(base: Offset, u: Float, t: Float, wind: Int, owl: Boolean) {
    val top = Offset(base.x, base.y - 96f * u)
    drawLine(Pal.white, base, top, 2.4f * u)
    drawLine(Pal.steel.copy(alpha = .5f), top, base + Offset(-22f * u, 0f), 1f * u)
    drawLine(Pal.steel.copy(alpha = .5f), top, base + Offset(22f * u, 0f), 1f * u)
    // The flag: limp in a calm, straight out in a gale.
    val len = 26f * u
    val droop = (1f - wind / 12f) * 18f * u
    val wave = sin((t * 2 * PI * (2 + wind)).toDouble()).toFloat() * (2f + wind * .4f) * u
    val f = Path().apply {
        moveTo(top.x, top.y + 2f * u)
        quadraticTo(top.x + len * .5f, top.y + 2f * u + wave + droop * .3f, top.x + len, top.y + 2f * u + droop)
        lineTo(top.x + len, top.y + 16f * u + droop)
        quadraticTo(top.x + len * .5f, top.y + 16f * u - wave + droop * .3f, top.x, top.y + 16f * u)
        close()
    }
    drawPath(f, Pal.red)
    if (owl) {
        val o = top + Offset(-1f * u, -6f * u)
        drawOval(Color(0xFFF1F3F5), Offset(o.x - 5f * u, o.y - 7f * u), Size(10f * u, 13f * u))
        drawCircle(Color(0xFFF6C342), 1.4f * u, o + Offset(-2f * u, -3f * u))
        drawCircle(Color(0xFFF6C342), 1.4f * u, o + Offset(2f * u, -3f * u))
    }
}

private fun DrawScope.radio(base: Offset, u: Float, night: Boolean) {
    val hgt = 110f * u
    val top = Offset(base.x, base.y - hgt)
    val col = if (night) Color(0xFF9AAAC0) else Pal.steel
    drawLine(col, base + Offset(-7f * u, 0f), top, 1.4f * u)
    drawLine(col, base + Offset(7f * u, 0f), top, 1.4f * u)
    var y = base.y
    var i = 0
    while (y > top.y + 6f * u) {
        val k = (base.y - y) / hgt
        val half = 7f * u * (1 - k)
        val y2 = y - 9f * u
        val half2 = 7f * u * (1 - (base.y - y2) / hgt)
        drawLine(col, Offset(base.x - half, y), Offset(base.x + half2, y2), 1f * u)
        y = y2; i++
    }
    drawCircle(Pal.red, 2.4f * u, top)
    drawLine(col.copy(alpha = .5f), top, Offset(base.x + 100f * u, base.y - 40f * u), .8f * u)
}

private fun DrawScope.windmill(base: Offset, u: Float, t: Float, wind: Int) {
    val hub = Offset(base.x, base.y - 70f * u)
    drawLine(Pal.white, base + Offset(-6f * u, 0f), hub, 2f * u)
    drawLine(Pal.white, base + Offset(6f * u, 0f), hub, 2f * u)
    rotate(t * 360f * (1 + wind) * 1.5f, hub) {
        for (k in 0 until 3) rotate(k * 120f, hub) {
            drawRoundRect(Pal.white, Offset(hub.x - 2f * u, hub.y - 30f * u), Size(4f * u, 30f * u), CornerRadius(2f * u))
        }
    }
    drawCircle(Pal.steel, 3f * u, hub)
}

private fun DrawScope.dog(base: Offset, u: Float, t: Float) {
    val wag = sin((t * 2 * PI * 8).toDouble()).toFloat() * 4f * u
    val body = Color(0xFFE9E2D6)
    val dark = Color(0xFF7B6A58)
    drawOval(body, Offset(base.x - 12f * u, base.y - 12f * u), Size(24f * u, 11f * u))
    drawCircle(body, 6f * u, Offset(base.x + 13f * u, base.y - 14f * u))
    val ear = Path().apply { moveTo(base.x + 10f * u, base.y - 18f * u); lineTo(base.x + 12f * u, base.y - 24f * u); lineTo(base.x + 15f * u, base.y - 18f * u); close() }
    drawPath(ear, dark)
    drawCircle(dark, 1.2f * u, Offset(base.x + 15f * u, base.y - 15f * u))
    for (dx in listOf(-9f, -4f, 5f, 9f)) drawLine(body, Offset(base.x + dx * u, base.y - 3f * u), Offset(base.x + dx * u, base.y + 3f * u), 2.4f * u)
    drawLine(body, Offset(base.x - 12f * u, base.y - 9f * u), Offset(base.x - 19f * u, base.y - 16f * u + wag), 2.4f * u)
}

private fun DrawScope.banya(base: Offset, u: Float, t: Float, night: Boolean) {
    val w = 40f * u; val hh = 22f * u
    drawRect(Pal.wood, Offset(base.x - w / 2, base.y - hh), Size(w, hh))
    for (i in 1..3) drawLine(Pal.woodDark, Offset(base.x - w / 2, base.y - hh + i * hh / 4), Offset(base.x + w / 2, base.y - hh + i * hh / 4), 1f * u)
    val roof = Path().apply { moveTo(base.x - w / 2 - 4f * u, base.y - hh); lineTo(base.x, base.y - hh - 12f * u); lineTo(base.x + w / 2 + 4f * u, base.y - hh); close() }
    drawPath(roof, Pal.white)
    drawRect(if (night) Pal.window else Color(0xFF9DBAD0), Offset(base.x + 6f * u, base.y - hh + 6f * u), Size(8f * u, 7f * u))
    for (i in 0 until 3) {
        val k = ((t * 2 + i * .33f) % 1f)
        drawCircle(Color.White.copy(alpha = .45f * (1 - k)), (2f + k * 5f) * u, Offset(base.x - 8f * u + k * 6f * u, base.y - hh - 14f * u - k * 24f * u))
    }
}

private fun DrawScope.greenhouse(base: Offset, u: Float, night: Boolean) {
    val w = 36f * u
    drawArc(Pal.glass.copy(alpha = .8f), 180f, 180f, true, Offset(base.x - w / 2, base.y - 18f * u), Size(w, 36f * u))
    for (dx in listOf(-8f, 0f, 8f)) drawCircle(Pal.green, 4f * u, Offset(base.x + dx * u, base.y - 5f * u))
    if (night) drawArc(Color(0x55FFD27A), 180f, 180f, true, Offset(base.x - w / 2, base.y - 18f * u), Size(w, 36f * u))
}

private fun DrawScope.beacon(base: Offset, u: Float, t: Float, night: Boolean) {
    val hgt = 64f * u
    val p = Path().apply { moveTo(base.x - 9f * u, base.y); lineTo(base.x - 5f * u, base.y - hgt); lineTo(base.x + 5f * u, base.y - hgt); lineTo(base.x + 9f * u, base.y); close() }
    drawPath(p, Pal.white)
    for (i in 0..2) drawRect(Pal.red, Offset(base.x - 8f * u + i * .8f * u, base.y - (12f + i * 18f) * u), Size(16f * u - i * 1.6f * u, 7f * u))
    val on = sin((t * 2 * PI * 6).toDouble()) > 0
    drawCircle(if (on) Color(0xFFFFE08A) else Color(0xFF8A7A50), 4f * u, Offset(base.x, base.y - hgt - 4f * u))
    if (on && night) drawCircle(Color(0x44FFE08A), 18f * u, Offset(base.x, base.y - hgt - 4f * u))
}

private fun DrawScope.dome(base: Offset, u: Float, night: Boolean) {
    drawRect(Pal.white, Offset(base.x - 18f * u, base.y - 18f * u), Size(36f * u, 18f * u))
    drawArc(if (night) Color(0xFFB8C6D8) else Color(0xFFDCE4EC), 180f, 180f, true, Offset(base.x - 18f * u, base.y - 36f * u), Size(36f * u, 36f * u))
    drawRect(Pal.steel, Offset(base.x - 3f * u, base.y - 36f * u), Size(6f * u, 16f * u))
}

private fun DrawScope.helipad(c: Offset, u: Float) {
    drawOval(Color(0x2255657A), Offset(c.x - 26f * u, c.y - 7f * u), Size(52f * u, 14f * u))
    drawOval(Pal.red, Offset(c.x - 26f * u, c.y - 7f * u), Size(52f * u, 14f * u), style = Stroke(1.5f * u))
    drawLine(Pal.red, c + Offset(-6f * u, -4f * u), c + Offset(-6f * u, 4f * u), 1.6f * u)
    drawLine(Pal.red, c + Offset(6f * u, -4f * u), c + Offset(6f * u, 4f * u), 1.6f * u)
    drawLine(Pal.red, c + Offset(-6f * u, 0f), c + Offset(6f * u, 0f), 1.6f * u)
}

private fun DrawScope.balloon(c: Offset, u: Float, alpha: Float) {
    drawCircle(Color.White.copy(alpha = alpha), 8f * u, c)
    drawLine(Pal.steel.copy(alpha = alpha), c + Offset(0f, 8f * u), c + Offset(0f, 22f * u), .8f * u)
    drawRect(Pal.red.copy(alpha = alpha), Offset(c.x - 2f * u, c.y + 22f * u), Size(4f * u, 4f * u))
}

private fun DrawScope.icebreaker(c: Offset, u: Float) {
    val hull = Path().apply { moveTo(c.x, c.y); lineTo(c.x + 40f * u, c.y); lineTo(c.x + 46f * u, c.y - 8f * u); lineTo(c.x - 4f * u, c.y - 8f * u); close() }
    drawPath(hull, Pal.hull)
    drawRect(Pal.red, Offset(c.x - 3f * u, c.y - 9f * u), Size(49f * u, 2f * u))
    drawRect(Pal.white, Offset(c.x + 10f * u, c.y - 16f * u), Size(18f * u, 8f * u))
    drawRect(Pal.red, Offset(c.x + 16f * u, c.y - 22f * u), Size(5f * u, 6f * u))
}

private fun DrawScope.bear(c: Offset, u: Float, night: Boolean) {
    val col = if (night) Color(0xFFDDE5EE) else Color(0xFFF4F1E6)
    drawOval(col, Offset(c.x - 16f * u, c.y - 12f * u), Size(32f * u, 14f * u))
    drawCircle(col, 6f * u, Offset(c.x + 18f * u, c.y - 8f * u))
    for (dx in listOf(-12f, -5f, 6f, 12f)) drawRect(col, Offset(c.x + dx * u, c.y - 2f * u), Size(4f * u, 8f * u))
    drawCircle(Color(0xFF222222), 1.2f * u, Offset(c.x + 23f * u, c.y - 8f * u))
}

private fun DrawScope.fox(c: Offset, u: Float) {
    val col = Color(0xFFFAFAF7)
    drawOval(col, Offset(c.x - 10f * u, c.y - 9f * u), Size(20f * u, 9f * u))
    drawCircle(col, 5f * u, Offset(c.x + 11f * u, c.y - 10f * u))
    val ears = Path().apply {
        moveTo(c.x + 8f * u, c.y - 13f * u); lineTo(c.x + 9f * u, c.y - 19f * u); lineTo(c.x + 12f * u, c.y - 14f * u)
        moveTo(c.x + 12f * u, c.y - 14f * u); lineTo(c.x + 15f * u, c.y - 19f * u); lineTo(c.x + 15f * u, c.y - 12f * u)
    }
    drawPath(ears, col)
    drawOval(col, Offset(c.x - 24f * u, c.y - 10f * u), Size(16f * u, 7f * u))
    drawCircle(Color(0xFF222222), 1f * u, Offset(c.x + 13f * u, c.y - 11f * u))
    drawCircle(Color(0xFF222222), 1.1f * u, Offset(c.x + 16f * u, c.y - 9f * u))
}

private fun DrawScope.tracks(start: Offset, u: Float, w: Float) {
    var x = start.x
    var i = 0
    while (x < w * .4f) {
        val y = start.y + (if (i % 2 == 0) -3f else 3f) * u
        translate(x, y) {
            drawOval(Color(0x553A5A80), Offset(-2f * u, -1.5f * u), Size(2f * u, 3.5f * u))
            drawOval(Color(0x553A5A80), Offset(.5f * u, -1.5f * u), Size(2f * u, 3.5f * u))
        }
        x += 12f * u; i++
    }
}
