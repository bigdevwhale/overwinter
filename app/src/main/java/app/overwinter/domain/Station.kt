package app.overwinter.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** What stands on the station, in the order it's built: each one comes with a number of terms observed. */
enum class Building(val terms: Int) {
    HUT(0), SCREEN(1), VANE(3), MAST(7), RADIO(14), WINDMILL(21), DOG(30), BANYA(45),
    BALLOON(60), GREENHOUSE(90), BEACON(120), DOME(180), HELIPAD(270), ICEBREAKER(365),
}

enum class Rank { OBSERVER, SENIOR, TECHNICIAN, FORECASTER, CHIEF }

/** How well forecasts came true. */
data class Score(val total: Int, val mine: Int, val station: Int) {
    val minePct: Int get() = if (total == 0) 0 else mine * 100 / total
    val stationPct: Int get() = if (total == 0) 0 else station * 100 / total
}

/** A 90-day shift on the station. */
data class Shift(val n: Int, val from: LocalDate, val to: LocalDate, val day: Int, val done: Boolean)

object Station {
    const val SHIFT = 90

    fun built(terms: Int): List<Building> = Building.entries.filter { terms >= it.terms }
    fun next(terms: Int): Building? = Building.entries.firstOrNull { terms < it.terms }

    fun rank(terms: Int, score: Score): Rank = when {
        terms >= 365 && score.total >= 30 && score.minePct >= 65 -> Rank.CHIEF
        terms >= 180 && score.total >= 10 && score.minePct >= 60 -> Rank.FORECASTER
        terms >= 90 -> Rank.TECHNICIAN
        terms >= 30 -> Rank.SENIOR
        else -> Rank.OBSERVER
    }

    fun score(forecasts: List<Forecast>, days: List<Day>, today: LocalDate): Score {
        val byDate = days.associateBy { it.date }
        var total = 0; var mine = 0; var station = 0
        for (f in forecasts) {
            val d = byDate[f.day] ?: continue
            // A day is judged once it's over, or once its evening term is in.
            if (f.day >= today && d.obs.none { it.slot == Slot.EVENING }) continue
            total++
            if (f.mine == d.sky) mine++
            if (f.station == d.sky) station++
        }
        return Score(total, mine, station)
    }

    fun shift(started: LocalDate, today: LocalDate): Shift {
        val days = (today.toEpochDay() - started.toEpochDay()).toInt().coerceAtLeast(0)
        val n = days / SHIFT
        val from = started.plusDays((n * SHIFT).toLong())
        return Shift(n + 1, from, from.plusDays(SHIFT - 1L), days - n * SHIFT + 1, false)
    }

    fun shifts(started: LocalDate, today: LocalDate): List<Shift> {
        val cur = shift(started, today)
        return (1 until cur.n).map { i ->
            val from = started.plusDays(((i - 1) * SHIFT).toLong())
            Shift(i, from, from.plusDays(SHIFT - 1L), SHIFT, true)
        }
    }

    /** Days since the last term: the snow that gathered on the station. */
    fun drift(obs: List<Obs>, now: Long): Int {
        val last = obs.maxOfOrNull { it.at } ?: return 0
        return ((now - last) / 86_400_000L).toInt()
    }

    /**
     * The rare sights a new observation brings. Each is rare by its nature, not by luck alone;
     * the random ones are seeded by the observation so they're the same on every replay.
     */
    fun rare(o: Obs, before: List<Obs>, fronts: List<Front>, started: LocalDate, keptRule: Boolean, seen: List<Seen>, zone: ZoneId = ZoneId.systemDefault()): List<Rare> {
        val out = ArrayList<Rare>()
        val day = o.day(zone)
        val hour = Instant.ofEpochMilli(o.at).atZone(zone).hour
        val lastAt = before.maxOfOrNull { it.at }
        fun seenSince(k: Rare, days: Long) = seen.any { it.kind == k && it.at > o.at - days * 86_400_000L }
        // The aurora: the first fair term after a cyclone has passed.
        val passed = fronts.lastOrNull { it.low && it.to < day && it.to >= day.minusDays(4) }
        if (passed != null && Weather.fair(o.wind.toFloat(), o.cloud.toFloat(), o.pain) >= Weather.LOW && !seenSince(Rare.AURORA, 5)) out += Rare.AURORA
        if (o.slot == Slot.MORNING && o.cloud <= 1 && o.wind >= 5 && !seenSince(Rare.HALO, 7)) out += Rare.HALO
        if (o.cloud == 0 && o.wind >= 8 && o.pain == 0 && !seenSince(Rare.PARHELION, 14)) out += Rare.PARHELION
        if (hour < 5 && !seenSince(Rare.OWL, 10)) out += Rare.OWL
        if (lastAt != null && o.at - lastAt >= 3 * 86_400_000L) out += Rare.DEER
        val stationDay = day.toEpochDay() - started.toEpochDay()
        if (stationDay > 0 && stationDay % 30 == 0L && before.none { it.day(zone) == day }) out += Rare.ICEBREAKER
        val high = fronts.lastOrNull { !it.low && it.ongoing }
        if (high != null && high.days >= 5 && !seenSince(Rare.GREEN_RAY, 20)) out += Rare.GREEN_RAY
        if (keptRule) out += Rare.RADIOGRAM
        val h = (o.id.hashCode().toLong() and 0x7fffffff)
        if (o.slot == Slot.EVENING && h % 23 == 0L && !seenSince(Rare.FOX, 5)) out += Rare.FOX
        if (h % 71 == 7L && !seenSince(Rare.BEAR, 20)) out += Rare.BEAR
        return out
    }
}
