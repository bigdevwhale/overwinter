package app.overwinter.domain

import java.time.LocalDate
import kotlin.math.abs

enum class Metric(val threshold: Float, val higherIsBetter: Boolean) {
    WIND(1.5f, true),
    CLOUD(1.2f, false),
    /** Share of days with rain or a storm somewhere, 0..1. */
    PAIN(0.25f, false),
    SLEEP(0.7f, true),
}

enum class Cause { GEAR, SHORT_SLEEP, PRESSURE_DROP }

/**
 * A pattern the station noticed: with [cause], [metric] averages [a]; without it, [b].
 * [gear] names the instrument for gear rules; [lag] is 1 when the effect shows the next day.
 */
data class Rule(
    val cause: Cause,
    val gear: String?,
    val metric: Metric,
    val a: Float,
    val b: Float,
    val nA: Int,
    val nB: Int,
    val lag: Int,
    /** The same difference in "fair weather" terms, for the station's forecast. */
    val dFair: Float,
) {
    val key: String get() = listOfNotNull(cause.name, gear, metric.name).joinToString(":")
    val good: Boolean get() = if (metric.higherIsBetter) a > b else a < b
    val strength: Float get() = abs(a - b) / metric.threshold
}

object Rules {
    const val MIN = 4
    const val DROP = 4f

    private fun value(d: Day, m: Metric): Float? = when (m) {
        Metric.WIND -> d.wind
        Metric.CLOUD -> d.cloud
        Metric.PAIN -> if (d.pain >= 2) 1f else 0f
        Metric.SLEEP -> d.sleep
    }

    private fun compare(cause: Cause, gear: String?, lag: Int, with: List<Day>, without: List<Day>, metrics: List<Metric>): Rule? {
        if (with.size < MIN || without.size < MIN) return null
        var best: Rule? = null
        for (m in metrics) {
            val va = with.mapNotNull { value(it, m) }
            val vb = without.mapNotNull { value(it, m) }
            if (va.size < MIN || vb.size < MIN) continue
            val a = va.average().toFloat()
            val b = vb.average().toFloat()
            if (abs(a - b) < m.threshold) continue
            val dFair = with.map { it.fair }.average().toFloat() - without.map { it.fair }.average().toFloat()
            val r = Rule(cause, gear, m, a, b, va.size, vb.size, lag, dFair)
            if (best == null || r.strength > best.strength) best = r
        }
        return best
    }

    /**
     * Everything the journal says with at least [MIN] days on each side.
     * [drops] tells, for a day, how much the pressure fell over the 24 hours before its first term.
     */
    fun find(days: List<Day>, gear: List<Gear>, drops: (Day) -> Float?): List<Rule> {
        val byDate = days.associateBy { it.date }
        val out = ArrayList<Rule>()
        // An instrument ticked today, and tomorrow's weather.
        for (g in gear) {
            val pairs = days.mapNotNull { d -> byDate[d.date.plusDays(1)]?.let { d to it } }
            val with = pairs.filter { g.id in it.first.gear }.map { it.second }
            val without = pairs.filter { g.id !in it.first.gear }.map { it.second }
            compare(Cause.GEAR, g.id, 1, with, without, listOf(Metric.WIND, Metric.CLOUD, Metric.PAIN, Metric.SLEEP))?.let { out += it }
        }
        // Short nights and the day after them.
        val slept = days.filter { it.sleep != null }
        compare(Cause.SHORT_SLEEP, null, 0, slept.filter { it.sleep!! < 6f }, slept.filter { it.sleep!! >= 7f }, listOf(Metric.WIND, Metric.CLOUD, Metric.PAIN))
            ?.let { out += it }
        // Falling pressure and pain.
        val measured = days.mapNotNull { d -> drops(d)?.let { d to it } }
        compare(Cause.PRESSURE_DROP, null, 0, measured.filter { it.second >= DROP }.map { it.first },
            measured.filter { it.second < DROP / 2 }.map { it.first }, listOf(Metric.PAIN, Metric.WIND, Metric.CLOUD))
            ?.let { out += it }
        return out.sortedByDescending { it.strength }
    }

    /**
     * The station's forecast for [day]: the fair weather of the last two weeks, moved by the kept and new rules
     * that apply ([gearToday] ticked the day before, the pressure falling now).
     */
    fun forecast(days: List<Day>, rules: List<Rule>, verdicts: Map<String, Verdict>, gearToday: Set<String>, falling: Boolean, day: LocalDate): Sky {
        val recent = days.filter { it.date < day && it.date >= day.minusDays(14) }
        var f = if (recent.isEmpty()) 0.55f else recent.map { it.fair }.average().toFloat()
        for (r in rules) {
            if (verdicts[r.key] == Verdict.STRUCK) continue
            val applies = when (r.cause) {
                Cause.GEAR -> r.gear in gearToday
                Cause.PRESSURE_DROP -> falling
                Cause.SHORT_SLEEP -> false
            }
            if (applies) f += r.dFair * 0.8f
        }
        return Weather.sky(f)
    }
}
