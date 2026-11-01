package app.overwinter.data

import android.content.Context
import app.overwinter.AppContainer
import app.overwinter.domain.Baro
import app.overwinter.domain.Forecast
import app.overwinter.domain.Journal
import app.overwinter.domain.Obs
import app.overwinter.domain.Rain
import app.overwinter.domain.Rules
import app.overwinter.domain.Seen
import app.overwinter.domain.Sky
import app.overwinter.domain.Slot
import app.overwinter.domain.Station
import app.overwinter.domain.Verdict
import app.overwinter.domain.Weather
import app.overwinter.domain.Zones
import app.overwinter.ru
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.sin
import kotlin.random.Random

/**
 * Two months on a made-up station for the demo build and the screenshots: two cyclones, a high,
 * coffee late that steals the next day's wind, workouts that clear the sky, a pressure fall that brings headaches.
 */
object DemoSeed {
    const val DAYS = 64

    suspend fun seedIfEmpty(ctx: Context, c: AppContainer, now: LocalDateTime = LocalDateTime.now()) {
        if (c.store.load().obs.isNotEmpty()) return
        val ru = ctx.ru
        val zone = ZoneId.systemDefault()
        val today = now.toLocalDate()
        val start = today.minusDays(DAYS - 1L)
        val rnd = Random(7)
        fun ms(t: LocalDateTime) = t.atZone(zone).toInstant().toEpochMilli()

        // The pressure: slow waves, two deep lows.
        fun hpa(day: Double): Float {
            var p = 1014 + 7 * sin(day / 4.3) + 3 * sin(day / 1.7)
            for (centre in listOf(DAYS - 48.0, DAYS - 20.0)) p -= 17 * kotlin.math.exp(-((day - centre) * (day - centre)) / 5.0)
            return p.toFloat()
        }
        val tape = ArrayList<Baro>()
        var t = start.atStartOfDay()
        while (t.isBefore(now)) {
            val d = (t.toLocalDate().toEpochDay() - start.toEpochDay()) + t.hour / 24.0
            tape += Baro(ms(t), hpa(d) + (rnd.nextFloat() - .5f) * .6f)
            t = t.plusHours(1)
        }

        val notesRu = listOf(
            "Проспал до обеда, а голова всё равно ватная.", "Гуляли у реки, солнце пробилось к вечеру.", "Два кофе после шести. Зря.",
            "Отличная тренировка, даже лестница не страшна.", "Весь день мигрень, таблетка помогла только к ужину.", "Звонила мама, долго говорили.",
            "Первый снег. Шёл пешком до работы.", "Сдал отчёт — сразу будто ветер в паруса.",
        )
        val notesEn = listOf(
            "Slept till noon and the head is still cotton wool.", "Walked by the river, the sun came out by evening.", "Two coffees after six. A mistake.",
            "Great workout, even the stairs felt easy.", "Migraine all day, the pill only helped by supper.", "Mum called, we talked for ages.",
            "First snow. Walked all the way to work.", "Handed in the report, wind in the sails at once.",
        )
        val notes = if (ru) notesRu else notesEn

        val obs = ArrayList<Obs>()
        var prevGear = emptySet<String>()
        for (i in 0 until DAYS) {
            val date = start.plusDays(i.toLong())
            val cyclone = i in (DAYS - 50)..(DAYS - 46) || i in (DAYS - 22)..(DAYS - 19)
            val high = i in (DAYS - 36)..(DAYS - 31)
            val drop = (hpa(i - 1.0) - hpa(i.toDouble())) >= 4f
            var wind = 5 + rnd.nextInt(-1, 3)
            var cloud = 3 + rnd.nextInt(-1, 2)
            if (cyclone) { wind -= 3; cloud += 4 }
            if (high) { wind += 2; cloud -= 2 }
            if ("coffee" in prevGear) wind -= 2
            if ("alcohol" in prevGear) { wind -= 2; cloud += 1 }
            if ("workout" in prevGear) cloud -= 2
            var sleep = 7.5f + rnd.nextInt(-2, 3) * .5f
            if ("screen" in prevGear) sleep -= 1.5f
            if ("coffee" in prevGear) sleep -= .5f
            val headache = drop || "alcohol" in prevGear || (cyclone && rnd.nextInt(3) == 0)
            val gear = buildSet {
                if (rnd.nextInt(100) < 38) add("coffee")
                if (rnd.nextInt(100) < 30) add("workout")
                if (rnd.nextInt(100) < 45) add("walk")
                if (rnd.nextInt(100) < 11) add("alcohol")
                if (rnd.nextInt(100) < 33) add("screen")
                if (rnd.nextInt(100) < 18) add("stress")
                if (headache) add("pill")
            }
            for (slot in Slot.entries) {
                val hour = listOf(9, 15, 21)[slot.ordinal]
                val at = date.atTime(hour, rnd.nextInt(0, 40))
                if (!at.isBefore(now)) continue
                if (slot == Slot.DAY && rnd.nextInt(100) < 30) continue
                if (slot == Slot.EVENING && rnd.nextInt(100) < 15 && date != today) continue
                val drift = when (slot) { Slot.MORNING -> 0; Slot.DAY -> 1; Slot.EVENING -> -1 }
                val w = (wind + drift + rnd.nextInt(-1, 2)).coerceIn(0, 12)
                val cl = (cloud + rnd.nextInt(-1, 2)).coerceIn(0, 8)
                val rain = buildList {
                    if (headache && slot != Slot.EVENING) add(Rain(Zones.HEAD, if (drop && slot == Slot.MORNING) 3 else 2))
                    if (rnd.nextInt(100) < 7) add(Rain(Zones.BACK, 1))
                    if (cyclone && rnd.nextInt(100) < 25) add(Rain(Zones.THROAT, 1))
                }
                val g = when (slot) {
                    Slot.MORNING -> gear.filter { it in setOf("pill", "walk") }.toSet()
                    Slot.DAY -> gear.filter { it in setOf("coffee", "workout", "stress") }.toSet()
                    Slot.EVENING -> gear.filter { it in setOf("alcohol", "screen", "coffee") }.toSet()
                }
                obs += Obs(
                    id = "demo%03d%d".format(i, slot.ordinal), at = ms(at), slot = slot, wind = w, cloud = cl, vis = (if (cl >= 6) 1 else 2) + (if (w >= 8) 1 else 0).coerceAtMost(1),
                    rain = rain, sleep = if (slot == Slot.MORNING) sleep.coerceIn(3.5f, 10f) else null, gear = g,
                    note = if (rnd.nextInt(100) < 18) notes[rnd.nextInt(notes.size)] else "",
                    hpa = Barometer.near(tape, ms(at)),
                )
            }
            prevGear = gear
        }

        // Replay the station to see what rare sights the terms brought.
        val names = c.names()
        val seen = ArrayList<Seen>()
        val sorted = obs.sortedBy { it.at }
        for (k in sorted.indices) {
            val o = sorted[k]
            val before = sorted.subList(0, k)
            val fronts = Weather.fronts(Weather.days(before + o), names, o.day(zone))
            Station.rare(o, before, fronts, start, false, seen).forEach { seen += Seen(it, o.at) }
        }
        // Forecasts for the last three weeks: mine are right most of the time.
        val days = Weather.days(sorted).associateBy { it.date }
        val forecasts = (1..24).mapNotNull { back ->
            val d = today.minusDays(back.toLong())
            val actual = days[d]?.sky ?: return@mapNotNull null
            val miss = Sky.entries[(actual.ordinal + 1) % 3]
            Forecast(d, if (rnd.nextInt(100) < 68) actual else miss, if (rnd.nextInt(100) < 58) actual else miss, ms(d.minusDays(1).atTime(21, 30)))
        }
        val all = Weather.days(sorted)
        val rules = Rules.find(all, Journal.EMPTY.gear) { d -> Barometer.change(tape, d.obs.first().at, 24)?.let { -it } }
        val verdicts = rules.take(2).associate { it.key to Verdict.KEPT }
        if (verdicts.isNotEmpty()) seen += Seen(app.overwinter.domain.Rare.RADIOGRAM, ms(today.minusDays(6).atTime(21, 40)))

        c.store.putBaro(tape)
        c.store.save(Journal(obs = sorted, forecasts = forecasts, seen = seen, verdicts = verdicts))
        c.settings.update { it.copy(onboarded = true, callsign = if (ru) "Северная" else "North", started = start.toEpochDay()) }
    }
}
