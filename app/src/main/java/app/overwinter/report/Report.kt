package app.overwinter.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import app.overwinter.R
import app.overwinter.data.Look
import app.overwinter.data.Settings
import app.overwinter.domain.Day
import app.overwinter.ui.components.Words
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * A plain report for a doctor: no polar metaphors, just the scales, the pain map, sleep,
 * what was taken, the patterns I kept, and the days in a table.
 */
object Report {
    private const val W = 595
    private const val H = 842
    private const val M = 40f

    suspend fun make(ctx: Context, look: Look, s: Settings, period: Int): File? = withContext(Dispatchers.IO) {
        val from = look.today.minusDays(period - 1L)
        val days = look.days.filter { it.date >= from && it.date <= look.today }
        if (days.isEmpty()) return@withContext null
        val serif = runCatching { ResourcesCompat.getFont(ctx, R.font.pt_serif_regular) }.getOrNull() ?: Typeface.SERIF
        val bold = runCatching { ResourcesCompat.getFont(ctx, R.font.pt_serif_bold) }.getOrNull() ?: Typeface.DEFAULT_BOLD
        val mono = runCatching { ResourcesCompat.getFont(ctx, R.font.pt_mono) }.getOrNull() ?: Typeface.MONOSPACE
        val ink = Color.rgb(21, 37, 58)
        val muted = Color.rgb(116, 133, 154)
        val accent = Color.rgb(207, 68, 38)
        val blue = Color.rgb(58, 120, 174)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = serif; textSize = 10.5f; color = ink }
        val head = Paint(text).apply { typeface = bold; textSize = 17f }
        val sub = Paint(text).apply { typeface = bold; textSize = 12f }
        val small = Paint(text).apply { textSize = 8.5f; color = muted }
        val cell = Paint(text).apply { typeface = mono; textSize = 8f }
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(201, 214, 224); strokeWidth = .7f }

        val doc = PdfDocument()
        var pageNo = 0
        var page: PdfDocument.Page? = null
        var cv: Canvas? = null
        var y = 0f
        fun newPage() {
            page?.let { doc.finishPage(it) }
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            cv = page!!.canvas
            y = M
            cv!!.drawText(ctx.getString(R.string.pdf_footer, pageNo), M, H - 20f, small)
        }
        fun need(h: Float) { if (y + h > H - 40f) newPage() }
        fun wrap(t: String, p: Paint, width: Float = W - 2 * M) {
            val words = t.split(" ")
            var cur = ""
            for (w in words) {
                val next = if (cur.isEmpty()) w else "$cur $w"
                if (p.measureText(next) > width && cur.isNotEmpty()) {
                    need(p.textSize * 1.4f); cv!!.drawText(cur, M, y + p.textSize, p); y += p.textSize * 1.4f; cur = w
                } else cur = next
            }
            if (cur.isNotEmpty()) { need(p.textSize * 1.4f); cv!!.drawText(cur, M, y + p.textSize, p); y += p.textSize * 1.4f }
        }

        newPage()
        val name = s.callsign.ifBlank { ctx.getString(R.string.callsign_default) }
        wrap(ctx.getString(R.string.pdf_title), head)
        wrap(ctx.getString(R.string.pdf_period, Words.date(ctx, from), Words.date(ctx, look.today), name), small)
        y += 8f
        wrap(ctx.getString(R.string.pdf_scales), small)
        y += 10f

        // Summary.
        wrap(ctx.getString(R.string.pdf_summary), sub)
        val obs = days.flatMap { it.obs }
        fun avg(f: (Day) -> Float?) = days.mapNotNull(f).takeIf { it.isNotEmpty() }?.average()?.toFloat()
        wrap(ctx.getString(R.string.pdf_days, days.size, period, obs.size), text)
        avg { it.wind }?.let { wrap(ctx.getString(R.string.pdf_energy, Words.num(ctx, it)), text) }
        avg { it.cloud }?.let { wrap(ctx.getString(R.string.pdf_mood, Words.num(ctx, it)), text) }
        avg { it.vis }?.let { wrap(ctx.getString(R.string.pdf_clarity, Words.num(ctx, it)), text) }
        avg { it.sleep }?.let { a ->
            val short = days.count { (it.sleep ?: 99f) < 6f }
            wrap(ctx.getString(R.string.pdf_sleep, Words.num(ctx, a), short), text)
        }
        y += 8f

        // Pain by zone.
        wrap(ctx.getString(R.string.pdf_pain), sub)
        val zones = Words.arr(ctx, R.array.zones_plain)
        var any = false
        for (z in zones.indices) {
            val byLevel = (1..3).map { l -> days.count { d -> d.obs.any { o -> o.rain.any { it.zone == z && it.level == l } } } }
            if (byLevel.sum() == 0) continue
            any = true
            wrap(ctx.getString(R.string.pdf_pain_zone, zones[z], byLevel.sum(), byLevel[0], byLevel[1], byLevel[2]), text)
        }
        if (!any) wrap(ctx.getString(R.string.pdf_pain_none), text)
        y += 8f

        // A chart: energy and mood day by day, pain marked.
        need(150f)
        wrap(ctx.getString(R.string.pdf_chart), sub)
        val top = y + 6f
        val bottom = top + 110f
        val c = cv!!
        val left = M; val right = W - M
        for (k in 0..4) c.drawLine(left, top + (bottom - top) * k / 4, right, top + (bottom - top) * k / 4, line)
        fun x(d: Day) = left + (right - left) * ((d.date.toEpochDay() - from.toEpochDay()).toFloat() / (period - 1).coerceAtLeast(1))
        val pe = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; strokeWidth = 1.6f; style = Paint.Style.STROKE }
        val pm = Paint(pe).apply { color = blue }
        var prev: Day? = null
        for (d in days) {
            val p = prev
            if (p != null && d.date.toEpochDay() - p.date.toEpochDay() <= 2) {
                c.drawLine(x(p), bottom - (bottom - top) * p.wind / 12f, x(d), bottom - (bottom - top) * d.wind / 12f, pe)
                c.drawLine(x(p), bottom - (bottom - top) * (1 - p.cloud / 8f), x(d), bottom - (bottom - top) * (1 - d.cloud / 8f), pm)
            }
            if (d.pain >= 2) c.drawCircle(x(d), bottom + 6f, if (d.pain == 3) 3f else 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink })
            prev = d
        }
        y = bottom + 14f
        wrap(ctx.getString(R.string.pdf_chart_legend), small)
        y += 8f

        // What was taken or done.
        val gearCounts = look.j.gear.map { g -> g to days.count { g.id in it.gear } }.filter { it.second > 0 }
        if (gearCounts.isNotEmpty()) {
            wrap(ctx.getString(R.string.pdf_gear), sub)
            wrap(gearCounts.joinToString(" · ") { Words.gear(ctx, it.first) + ": " + it.second }, text)
            y += 8f
        }

        // Patterns I kept or haven't judged yet.
        if (look.kept.isNotEmpty()) {
            wrap(ctx.getString(R.string.pdf_rules), sub)
            for (r in look.kept) wrap("• " + Words.rule(ctx, r, look.j.gear) + " (" + Words.evidence(ctx, r) + ")", text)
            y += 8f
        }

        // The days.
        need(40f)
        wrap(ctx.getString(R.string.pdf_table), sub)
        val cols = floatArrayOf(0f, 62f, 98f, 134f, 170f, 262f, 296f)
        val heads = Words.arr(ctx, R.array.pdf_cols)
        fun row(vals: List<String>, p: Paint) {
            need(13f)
            for (i in vals.indices) {
                val maxW = (if (i + 1 < cols.size) cols[i + 1] - cols[i] else W - 2 * M - cols[i]) - 4f
                var v = vals[i]
                while (v.isNotEmpty() && p.measureText(v) > maxW) v = v.dropLast(2) + "…"
                cv!!.drawText(v, M + cols[i], y + 9f, p)
            }
            y += 12f
            cv!!.drawLine(M, y, W - M, y, line)
        }
        row(heads.toList(), Paint(cell).apply { typeface = bold })
        for (d in days.reversed()) {
            row(listOf(
                Words.dateShort(ctx, d.date),
                Words.num(ctx, d.wind),
                Words.num(ctx, d.cloud),
                Words.num(ctx, d.vis),
                d.obs.flatMap { it.rain }.groupBy { it.zone }.map { (z, l) -> zones[z] + " " + l.maxOf { it.level } }.joinToString(", ").ifEmpty { "—" },
                d.sleep?.let { Words.num(ctx, it) } ?: "—",
                (d.gear.map { Words.gear(ctx, it, look.j.gear) } + d.obs.map { it.note }.filter { it.isNotEmpty() }).joinToString("; ").ifEmpty { "" },
            ), cell)
        }
        page?.let { doc.finishPage(it) }
        val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
        val f = File(dir, ctx.getString(R.string.pdf_file, look.today.toString()) + ".pdf")
        f.outputStream().use { doc.writeTo(it) }
        doc.close()
        f
    }
}
