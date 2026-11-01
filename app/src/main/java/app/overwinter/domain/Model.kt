package app.overwinter.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The three synoptic terms of the day: morning, afternoon, evening. */
enum class Slot { MORNING, DAY, EVENING }

/** Pain as precipitation over a part of the body: 1 drizzle, 2 rain, 3 thunderstorm. */
data class Rain(val zone: Int, val level: Int)

object Zones {
    const val HEAD = 0
    const val THROAT = 1
    const val CHEST = 2
    const val BELLY = 3
    const val BACK = 4
    const val ARMS = 5
    const val LEGS = 6
    const val ALL = 7
    const val COUNT = 8
}

/**
 * One observation: what the instruments of the body read at a synoptic term.
 * Wind is energy on the Beaufort scale (0 calm … 12 hurricane), cloud is how heavy the mood is in oktas
 * (0 clear … 8 overcast), visibility is how clear the head is (0 fog … 3 excellent).
 */
data class Obs(
    val id: String,
    val at: Long,
    val slot: Slot,
    val wind: Int,
    val cloud: Int,
    val vis: Int,
    val rain: List<Rain> = emptyList(),
    /** Hours slept, asked at the first term of the day. */
    val sleep: Float? = null,
    /** Ids of the instruments ticked: coffee, a workout, a pill. */
    val gear: Set<String> = emptySet(),
    val note: String = "",
    /** Air pressure at the moment, if the phone has a barometer. */
    val hpa: Float? = null,
) {
    fun day(zone: ZoneId = ZoneId.systemDefault()): LocalDate = Instant.ofEpochMilli(at).atZone(zone).toLocalDate()
    val pain: Int get() = rain.maxOfOrNull { it.level } ?: 0
}

/** An instrument in the weather box: a thing worth watching. Built-in ones have a key and a translated name. */
data class Gear(val id: String, val icon: String, val name: String = "", val builtin: Boolean = false, val on: Boolean = true)

object Gears {
    val DEFAULT = listOf(
        Gear("coffee", "☕", builtin = true),
        Gear("alcohol", "🍷", builtin = true),
        Gear("workout", "🏋", builtin = true),
        Gear("walk", "🚶", builtin = true),
        Gear("pill", "💊", builtin = true),
        Gear("stress", "⚡", builtin = true),
        Gear("screen", "📱", builtin = true),
        Gear("sweets", "🍰", builtin = true),
        Gear("cycle", "🌙", builtin = true, on = false),
    )
}

/** Three kinds of weather a forecast can call. */
enum class Sky { CLEAR, MIXED, STORM }

/** A forecast for one day: mine, made in the evening, and the station's, made at the same moment. */
data class Forecast(val day: LocalDate, val mine: Sky, val station: Sky, val at: Long)

enum class Rare { AURORA, HALO, PARHELION, FOX, BEAR, OWL, DEER, ICEBREAKER, GREEN_RAY, RADIOGRAM }

data class Seen(val kind: Rare, val at: Long)

/** Whether I agreed with a rule the station noticed. */
enum class Verdict { NEW, KEPT, STRUCK }

data class Journal(
    val obs: List<Obs> = emptyList(),
    val forecasts: List<Forecast> = emptyList(),
    val seen: List<Seen> = emptyList(),
    val verdicts: Map<String, Verdict> = emptyMap(),
    val gear: List<Gear> = Gears.DEFAULT,
) {
    companion object { val EMPTY = Journal() }
}

/** A barometer reading. */
data class Baro(val at: Long, val hpa: Float)
