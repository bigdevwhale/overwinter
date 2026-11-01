package app.overwinter.data

import app.overwinter.domain.Baro
import app.overwinter.domain.Day
import app.overwinter.domain.Front
import app.overwinter.domain.Journal
import app.overwinter.domain.Rule
import app.overwinter.domain.Rules
import app.overwinter.domain.Sky
import app.overwinter.domain.Station
import app.overwinter.domain.Verdict
import app.overwinter.domain.Weather
import java.time.LocalDate

/** Everything the screens read off the journal and the tape, worked out once per change. */
class Look(val j: Journal, val tape: List<Baro>, names: List<String>, val today: LocalDate, val now: Long, val started: LocalDate) {
    val days: List<Day> = Weather.days(j.obs)
    val byDate: Map<LocalDate, Day> = days.associateBy { it.date }
    val fronts: List<Front> = Weather.fronts(days, names, today)

    /** How much the pressure fell over the day before a day's first term (positive is a fall). */
    fun drop(d: Day): Float? = Barometer.change(tape, d.obs.first().at, 24)?.let { -it }

    val rules: List<Rule> = Rules.find(days, j.gear.filter { it.on }, ::drop)
    val kept: List<Rule> get() = rules.filter { j.verdicts[it.key] != Verdict.STRUCK }
    val struck: List<Rule> get() = rules.filter { j.verdicts[it.key] == Verdict.STRUCK }

    val terms: Int = j.obs.size
    val score = Station.score(j.forecasts, days, today)
    val rank = Station.rank(terms, score)
    val built = Station.built(terms)
    val next = Station.next(terms)
    val shift = Station.shift(started, today)
    val drift: Int = Station.drift(j.obs, now)
    val todayDay: Day? = byDate[today]

    val hpa: Float? = tape.lastOrNull()?.takeIf { now - it.at < 3 * 3_600_000L }?.hpa
    val tendency3: Float? = Barometer.change(tape, now, 3)
    val change24: Float? = Barometer.change(tape, now, 24)
    val falling: Boolean = (change24 ?: 0f) <= -Rules.DROP

    /** A pressure rule I didn't strike that says falling pressure brings pain. */
    val pressureWarning: Rule? = if (falling) kept.firstOrNull { it.cause == app.overwinter.domain.Cause.PRESSURE_DROP && !it.good } else null

    fun stationForecast(day: LocalDate): Sky =
        Rules.forecast(days, rules, j.verdicts, todayDay?.gear.orEmpty(), falling, day)

    val tomorrowForecast = j.forecasts.firstOrNull { it.day == today.plusDays(1) }
    val todayForecast = j.forecasts.firstOrNull { it.day == today }
    val yesterdayForecast = j.forecasts.firstOrNull { it.day == today.minusDays(1) }
}
