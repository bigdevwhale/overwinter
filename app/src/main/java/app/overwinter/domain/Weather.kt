package app.overwinter.domain

import java.time.LocalDate
import java.time.ZoneId

/** A day's weather, averaged over its terms. */
data class Day(
    val date: LocalDate,
    val obs: List<Obs>,
    val wind: Float,
    val cloud: Float,
    val vis: Float,
    /** The strongest pain of the day, 0..3. */
    val pain: Int,
    val sleep: Float?,
    val gear: Set<String>,
    val hpa: Float?,
) {
    /** How fair the day was, 0 (a storm) … 1 (clear and windy in the good sense). */
    val fair: Float get() = Weather.fair(wind, cloud, pain)
    val sky: Sky get() = Weather.sky(fair)
}

/** A run of bad days gets a name, as cyclones do; a run of good ones is a high. */
data class Front(val low: Boolean, val from: LocalDate, val to: LocalDate, val name: String, val ongoing: Boolean) {
    val days: Int get() = (to.toEpochDay() - from.toEpochDay() + 1).toInt()
}

object Weather {
    const val LOW = 0.42f
    const val HIGH = 0.66f

    fun fair(wind: Float, cloud: Float, pain: Int): Float =
        (0.40f * (1f - cloud / 8f) + 0.35f * (wind / 12f).coerceAtMost(1f) + 0.25f * (1f - pain / 3f)).coerceIn(0f, 1f)

    fun sky(fair: Float): Sky = when {
        fair >= 0.6f -> Sky.CLEAR
        fair >= LOW -> Sky.MIXED
        else -> Sky.STORM
    }

    fun days(obs: List<Obs>, zone: ZoneId = ZoneId.systemDefault()): List<Day> =
        obs.groupBy { it.day(zone) }.toSortedMap().map { (d, list) -> day(d, list.sortedBy { it.at }) }

    fun day(date: LocalDate, list: List<Obs>): Day = Day(
        date = date,
        obs = list,
        wind = list.map { it.wind }.average().toFloat(),
        cloud = list.map { it.cloud }.average().toFloat(),
        vis = list.map { it.vis }.average().toFloat(),
        pain = list.maxOfOrNull { it.pain } ?: 0,
        sleep = list.firstNotNullOfOrNull { it.sleep },
        gear = list.flatMap { it.gear }.toSet(),
        hpa = list.mapNotNull { it.hpa }.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
    )

    /**
     * Cyclones: two or more bad days in a row (a missing day doesn't break a run). Highs: three or more good ones.
     * Cyclones get names in the order of the year, as the weather services do.
     */
    fun fronts(days: List<Day>, names: List<String>, today: LocalDate): List<Front> {
        val out = ArrayList<Front>()
        fun runs(test: (Day) -> Boolean, min: Int, low: Boolean) {
            var start: Day? = null
            var last: Day? = null
            fun close() {
                val s = start ?: return
                val l = last ?: return
                if (l.date.toEpochDay() - s.date.toEpochDay() + 1 >= min) {
                    out += Front(low, s.date, l.date, "", ongoing = l.date >= today.minusDays(1))
                }
            }
            for (d in days) {
                val gapOk = last == null || d.date.toEpochDay() - last!!.date.toEpochDay() <= 2
                if (test(d) && gapOk) {
                    if (start == null) start = d
                    last = d
                } else {
                    close()
                    start = if (test(d)) d else null
                    last = if (test(d)) d else null
                }
            }
            close()
        }
        runs({ it.fair < LOW }, 2, true)
        runs({ it.fair >= HIGH }, 3, false)
        out.sortBy { it.from }
        // Names restart every year, cyclones only.
        val counter = HashMap<Int, Int>()
        return out.map { f ->
            if (!f.low) f else {
                val n = counter.getOrDefault(f.from.year, 0)
                counter[f.from.year] = n + 1
                f.copy(name = names[n % names.size] + if (n >= names.size) " ${n / names.size + 1}" else "")
            }
        }
    }
}
