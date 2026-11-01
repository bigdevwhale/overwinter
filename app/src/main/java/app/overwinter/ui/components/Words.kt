package app.overwinter.ui.components

import android.content.Context
import app.overwinter.R
import app.overwinter.domain.Cause
import app.overwinter.domain.Gear
import app.overwinter.domain.Gears
import app.overwinter.domain.Metric
import app.overwinter.domain.Rule
import app.overwinter.locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** The names of things, in the app's language. */
object Words {
    private val builtinIds = Gears.DEFAULT.map { it.id }

    fun arr(ctx: Context, id: Int): Array<String> = ctx.resources.getStringArray(id)

    fun gear(ctx: Context, g: Gear): String =
        if (g.builtin) arr(ctx, R.array.gear_builtin).getOrElse(builtinIds.indexOf(g.id)) { g.id } else g.name

    fun gear(ctx: Context, id: String, all: List<Gear>): String = all.firstOrNull { it.id == id }?.let { gear(ctx, it) } ?: id

    fun num(ctx: Context, v: Float, digits: Int = 1): String = String.format(ctx.locale, "%.${digits}f", v)

    fun date(ctx: Context, d: LocalDate): String = d.format(DateTimeFormatter.ofPattern(ctx.getString(R.string.date_pattern), ctx.locale))

    fun dateShort(ctx: Context, d: LocalDate): String = d.format(DateTimeFormatter.ofPattern(ctx.getString(R.string.date_short), ctx.locale))

    fun time(ctx: Context, at: Long): String =
        Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(ctx.locale))

    fun hour(h: Int): String = "%02d:00".format(h)

    /** A rule as a sentence: who, then what happens. */
    fun rule(ctx: Context, r: Rule, gear: List<Gear>): String {
        val who = when (r.cause) {
            // The night after a day is "that night"; everything else shows the next day.
            Cause.GEAR -> ctx.getString(R.string.r_after_gear, gear(ctx, r.gear.orEmpty(), gear)) +
                if (r.metric == Metric.SLEEP) "" else " " + ctx.getString(R.string.r_next_day)
            Cause.SHORT_SLEEP -> ctx.getString(R.string.r_after_short)
            Cause.PRESSURE_DROP -> ctx.getString(R.string.r_when_drop)
        }
        val a = r.a
        val b = r.b
        val what = when (r.metric) {
            Metric.WIND -> ctx.getString(if (r.good) R.string.r_wind_more else R.string.r_wind_less, num(ctx, a), num(ctx, b))
            Metric.CLOUD -> ctx.getString(if (r.good) R.string.r_cloud_less else R.string.r_cloud_more, num(ctx, a), num(ctx, b))
            Metric.PAIN -> ctx.getString(if (r.good) R.string.r_pain_less else R.string.r_pain_more, (a * 100).toInt().toString(), (b * 100).toInt().toString())
            Metric.SLEEP -> ctx.getString(if (r.good) R.string.r_sleep_more else R.string.r_sleep_less, num(ctx, a), num(ctx, b))
        }
        return "$who $what"
    }

    fun evidence(ctx: Context, r: Rule): String = ctx.getString(R.string.r_evidence, r.nA, r.nB)
}
