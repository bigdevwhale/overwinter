package app.overwinter

import app.overwinter.data.Store
import app.overwinter.domain.Gear
import app.overwinter.domain.Journal
import app.overwinter.domain.Metric
import app.overwinter.domain.Obs
import app.overwinter.domain.Rain
import app.overwinter.domain.Rare
import app.overwinter.domain.Rules
import app.overwinter.domain.Slot
import app.overwinter.domain.Station
import app.overwinter.domain.Telegram
import app.overwinter.domain.Weather
import app.overwinter.domain.Zones
import app.overwinter.work.Work
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/** Robolectric for org.json; the rest is plain logic. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LogicTest {
    private val zone = ZoneOffset.UTC
    private val start = LocalDate.of(2026, 9, 1)
    private fun at(day: Int, hour: Int) = start.plusDays(day.toLong()).atTime(hour, 0).toInstant(zone).toEpochMilli()
    private fun obs(day: Int, wind: Int, cloud: Int, gear: Set<String> = emptySet(), pain: Int = 0, hour: Int = 9) =
        Obs("o$day-$hour", at(day, hour), Slot.MORNING, wind, cloud, 2, if (pain > 0) listOf(Rain(Zones.HEAD, pain)) else emptyList(), gear = gear)

    @Test fun cyclonesGetNamesInOrder() {
        val list = (0 until 20).map { d -> if (d in 3..5 || d in 12..14) obs(d, 1, 8, pain = 2) else obs(d, 6, 2) }
        val fronts = Weather.fronts(Weather.days(list, zone), listOf("Агата", "Борис"), start.plusDays(19)).filter { it.low }
        assertEquals(listOf("Агата", "Борис"), fronts.map { it.name })
        assertEquals(3, fronts[0].days)
    }

    @Test fun ruleFoundForCoffeeTheNextDay() {
        // Coffee on even days; the next (odd) day has weak wind.
        val list = (0 until 30).map { d ->
            val wind = if (d % 2 == 1) 2 else 7
            obs(d, wind, 3, if (d % 2 == 0) setOf("coffee") else emptySet())
        }
        val rules = Rules.find(Weather.days(list, zone), listOf(Gear("coffee", "☕", builtin = true))) { null }
        val r = rules.firstOrNull { it.gear == "coffee" }
        assertNotNull(r)
        assertEquals(Metric.WIND, r!!.metric)
        assertTrue(!r.good)
    }

    @Test fun telegramGroups() {
        val o = Obs("x", LocalDateTime.of(2026, 11, 1, 21, 5).toInstant(zone).toEpochMilli(), Slot.EVENING, 4, 4, 2,
            listOf(Rain(Zones.HEAD, 2)), sleep = 7.5f, gear = setOf("coffee"), hpa = 1013.2f)
        val g = Telegram.groups(o, "Тело", listOf("ГОЛ", "", "", "", "", "", "", ""), { "Кофе" }, zone)
        assertEquals(listOf("ТЕЛО", "01213", "10442", "2075/", "ГОЛ2", "40132", "= КОФ ="), g)
    }

    @Test fun morseOfSos() {
        assertEquals(listOf(1, -1, 1, -1, 1, -3, 3, -1, 3, -1, 3, -3, 1, -1, 1, -1, 1, -3), Telegram.morse("SOS"))
    }

    @Test fun nextTermRollsToTomorrow() {
        val (t, slot) = Work.nextTerm(listOf(9, 15, 21), LocalDateTime.of(2026, 11, 1, 22, 0))
        assertEquals(LocalDateTime.of(2026, 11, 2, 9, 0), t)
        assertEquals(Slot.MORNING, slot)
    }

    @Test fun buildingsAndRanks() {
        assertEquals(3, Station.built(3).size)
        assertEquals(app.overwinter.domain.Rank.SENIOR, Station.rank(30, app.overwinter.domain.Score(0, 0, 0)))
    }

    @Test fun deerAfterABreak() {
        val before = listOf(obs(0, 5, 3))
        val r = Station.rare(obs(5, 5, 3), before, emptyList(), start, false, emptyList(), zone)
        assertTrue(Rare.DEER in r)
    }

    @Test fun journalRoundTrip() {
        val j = Journal(obs = listOf(obs(1, 4, 5, setOf("pill"), pain = 3).copy(note = "тест", sleep = 6.5f, hpa = 1001f)))
        assertEquals(j, Store.parse(Store.json(j)))
    }
}
