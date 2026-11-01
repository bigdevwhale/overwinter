package app.overwinter.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.overwinter.domain.Sky
import app.overwinter.domain.Zones
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** The meteorologist's own marks: the station circle, the wind barb, rain, the thunderstorm. */
object Marks {
    /** Beaufort to knots, the middle of each force. */
    private val KNOTS = intArrayOf(0, 2, 5, 9, 13, 19, 24, 30, 37, 44, 52, 60, 70)

    /** The cloud cover: a circle filled by eighths, clockwise from the top. */
    fun DrawScope.okta(c: Offset, r: Float, okta: Int, ink: Color, width: Float) {
        if (okta >= 8) drawCircle(ink, r, c)
        else if (okta > 0) drawArc(ink, -90f, 360f * okta / 8f, true, Offset(c.x - r, c.y - r), Size(r * 2, r * 2))
        drawCircle(ink, r, c, style = Stroke(width))
    }

    /** The wind barb: a shaft out of the circle with half and full feathers and pennants; calm is a second ring. */
    fun DrawScope.barb(c: Offset, r: Float, beaufort: Int, ink: Color, width: Float, len: Float = r * 3.4f, angle: Float = -135f) {
        val knots = KNOTS[beaufort.coerceIn(0, 12)]
        if (knots < 3) { drawCircle(ink, r * 1.45f, c, style = Stroke(width)); return }
        val a = Math.toRadians(angle.toDouble())
        val dir = Offset(cos(a).toFloat(), sin(a).toFloat())
        val start = c + dir * r
        val end = c + dir * (r + len)
        drawLine(ink, start, end, width)
        // Feathers lean clockwise from the shaft.
        val side = Offset(-dir.y, dir.x) * -1f
        val step = len * 0.16f
        var left = ((knots + 2) / 5) * 5
        var pos = 0f
        fun at(p: Float) = end - dir * p
        while (left >= 50) {
            val p = Path().apply {
                moveTo(at(pos).x, at(pos).y)
                val tip = at(pos + step * .5f) + side * len * .36f
                lineTo(tip.x, tip.y)
                lineTo(at(pos + step).x, at(pos + step).y)
                close()
            }
            drawPath(p, ink)
            pos += step * 1.3f
            left -= 50
        }
        while (left >= 10) {
            drawLine(ink, at(pos), at(pos) + side * len * .36f + dir * step * .5f, width)
            pos += step
            left -= 10
        }
        if (left >= 5) {
            if (pos == 0f) pos = step
            drawLine(ink, at(pos), at(pos) + side * len * .2f + dir * step * .28f, width)
        }
    }

    /** Pain marks: drizzle commas, rain dots, the thunderstorm's bolt. */
    fun DrawScope.precip(c: Offset, s: Float, level: Int, ink: Color, width: Float) {
        when (level) {
            1 -> for (dx in listOf(-0.35f, 0.35f)) {
                val p = c + Offset(dx * s, 0f)
                drawCircle(ink, s * .16f, p)
                drawLine(ink, p + Offset(s * .12f, 0f), p + Offset(-s * .06f, s * .32f), width)
            }
            2 -> for (o in listOf(Offset(-.32f, .18f), Offset(.32f, .18f), Offset(0f, -.28f))) drawCircle(ink, s * .17f, c + o * s)
            3 -> {
                // The thunderstorm: a bolt hanging from a bracket.
                val p = Path().apply {
                    moveTo(c.x - s * .5f, c.y - s * .5f)
                    lineTo(c.x + s * .35f, c.y - s * .5f)
                    lineTo(c.x + s * .35f, c.y + s * .05f)
                    lineTo(c.x - s * .05f, c.y - s * .1f)
                    lineTo(c.x + s * .1f, c.y + s * .55f)
                }
                drawPath(p, ink, style = Stroke(width * 1.2f))
                val head = Path().apply {
                    moveTo(c.x + s * .1f, c.y + s * .6f)
                    lineTo(c.x - s * .06f, c.y + s * .36f)
                    lineTo(c.x + s * .2f, c.y + s * .4f)
                    close()
                }
                drawPath(head, ink)
            }
        }
    }

    fun DrawScope.sky(c: Offset, r: Float, sky: Sky, ink: Color, sun: Color, width: Float) {
        when (sky) {
            Sky.CLEAR -> {
                drawCircle(sun, r * .7f, c)
                for (i in 0 until 8) {
                    val a = Math.toRadians(i * 45.0)
                    val d = Offset(cos(a).toFloat(), sin(a).toFloat())
                    drawLine(sun, c + d * r * .95f, c + d * r * 1.35f, width)
                }
            }
            Sky.MIXED -> okta(c, r, 4, ink, width)
            Sky.STORM -> {
                okta(c, r * .8f, 8, ink, width)
                precip(c + Offset(r * .9f, r * .7f), r * .9f, 3, sun, width)
            }
        }
    }
}

/** The station circle with its barb: a whole observation in one mark. */
@Composable
fun StationMark(wind: Int, cloud: Int, ink: Color, modifier: Modifier) {
    Canvas(modifier) {
        val r = min(size.width, size.height) * .16f
        val c = Offset(size.width * .62f, size.height * .62f)
        with(Marks) {
            okta(c, r, cloud, ink, r * .22f)
            barb(c, r, wind, ink, r * .2f, len = r * 2.6f)
        }
    }
}

@Composable
fun SkyMark(sky: Sky, ink: Color, sun: Color, modifier: Modifier) {
    Canvas(modifier) {
        val r = min(size.width, size.height) * .3f
        with(Marks) { sky(Offset(size.width / 2, size.height / 2), r, sky, ink, sun, r * .16f) }
    }
}

/** The body as a plain figure for the pain map, and where each zone sits on it (in a 100×160 box). */
object Body {
    val zones: Map<Int, Rect> = mapOf(
        Zones.HEAD to Rect(38f, 2f, 62f, 28f),
        Zones.THROAT to Rect(42f, 28f, 58f, 37f),
        Zones.CHEST to Rect(32f, 37f, 68f, 62f),
        Zones.BELLY to Rect(34f, 62f, 66f, 86f),
        Zones.ARMS to Rect(14f, 40f, 30f, 92f),
        Zones.LEGS to Rect(34f, 92f, 66f, 156f),
    )

    fun DrawScope.figure(ink: Color, fill: Color) {
        val k = min(size.width / 100f, size.height / 160f)
        val ox = (size.width - 100f * k) / 2
        fun o(x: Float, y: Float) = Offset(ox + x * k, y * k)
        drawCircle(fill, 11f * k, o(50f, 15f))
        drawCircle(ink, 11f * k, o(50f, 15f), style = Stroke(1.6f * k))
        val torso = Path().apply {
            moveTo(o(44f, 28f).x, o(44f, 28f).y)
            lineTo(o(56f, 28f).x, o(56f, 28f).y)
            lineTo(o(57f, 34f).x, o(57f, 34f).y)
            cubicTo(o(66f, 35f).x, o(66f, 35f).y, o(70f, 38f).x, o(70f, 38f).y, o(71f, 44f).x, o(71f, 44f).y)
            lineTo(o(80f, 88f).x, o(80f, 88f).y)
            lineTo(o(74f, 90f).x, o(74f, 90f).y)
            lineTo(o(67f, 56f).x, o(67f, 56f).y)
            lineTo(o(66f, 90f).x, o(66f, 90f).y)
            lineTo(o(63f, 155f).x, o(63f, 155f).y)
            lineTo(o(53f, 155f).x, o(53f, 155f).y)
            lineTo(o(50f, 100f).x, o(50f, 100f).y)
            lineTo(o(47f, 155f).x, o(47f, 155f).y)
            lineTo(o(37f, 155f).x, o(37f, 155f).y)
            lineTo(o(34f, 90f).x, o(34f, 90f).y)
            lineTo(o(33f, 56f).x, o(33f, 56f).y)
            lineTo(o(26f, 90f).x, o(26f, 90f).y)
            lineTo(o(20f, 88f).x, o(20f, 88f).y)
            lineTo(o(29f, 44f).x, o(29f, 44f).y)
            cubicTo(o(30f, 38f).x, o(30f, 38f).y, o(34f, 35f).x, o(34f, 35f).y, o(43f, 34f).x, o(43f, 34f).y)
            close()
        }
        drawPath(torso, fill)
        drawPath(torso, ink, style = Stroke(1.6f * k))
    }

    /** The centre of a zone in canvas pixels; arms are marked on both sides. */
    fun DrawScope.centres(zone: Int): List<Offset> {
        val k = min(size.width / 100f, size.height / 160f)
        val ox = (size.width - 100f * k) / 2
        val r = zones[zone] ?: return emptyList()
        val c = Offset(ox + r.center.x * k, r.center.y * k)
        return if (zone == Zones.ARMS) listOf(c, Offset(ox + (100f - r.center.x) * k, r.center.y * k)) else listOf(c)
    }

    /** Which zone a tap at [p] falls in. */
    fun hit(p: Offset, w: Float, h: Float): Int? {
        val k = min(w / 100f, h / 160f)
        val ox = (w - 100f * k) / 2
        val x = (p.x - ox) / k
        val y = p.y / k
        val mx = if (x > 50f) 100f - x else x
        return zones.entries.firstOrNull { (z, r) -> if (z == Zones.ARMS) r.inflate(3f).contains(Offset(mx, y)) else r.inflate(2f).contains(Offset(x, y)) }?.key
    }
}
