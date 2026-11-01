package app.overwinter.data

import app.overwinter.domain.Baro
import app.overwinter.domain.Forecast
import app.overwinter.domain.Gear
import app.overwinter.domain.Journal
import app.overwinter.domain.Obs
import app.overwinter.domain.Rain
import app.overwinter.domain.Rare
import app.overwinter.domain.Seen
import app.overwinter.domain.Sky
import app.overwinter.domain.Slot
import app.overwinter.domain.Verdict
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

/** The station journal and the barograph tape, as two JSON files. Small enough to read whole. */
class Store(private val dir: File) {
    private val journalFile get() = File(dir, "journal.json")
    private val baroFile get() = File(dir, "baro.json")

    init { dir.mkdirs() }

    @Synchronized
    fun load(): Journal = runCatching { if (journalFile.exists()) parse(JSONObject(journalFile.readText())) else Journal.EMPTY }
        .getOrElse { Journal.EMPTY }

    @Synchronized
    fun save(j: Journal) = write(journalFile, json(j).toString())

    @Synchronized
    fun baro(): List<Baro> = runCatching {
        if (!baroFile.exists()) return emptyList()
        val a = JSONArray(baroFile.readText())
        (0 until a.length()).map { a.getJSONArray(it).let { p -> Baro(p.getLong(0), p.getDouble(1).toFloat()) } }
    }.getOrElse { emptyList() }

    /** Adds a reading and keeps the last 120 days. */
    @Synchronized
    fun addBaro(b: Baro) = putBaro(baro() + b)

    @Synchronized
    fun putBaro(list: List<Baro>) {
        val keep = list.filter { it.at > System.currentTimeMillis() - 120 * 86_400_000L }.sortedBy { it.at }
        val a = JSONArray()
        keep.forEach { a.put(JSONArray().put(it.at).put(it.hpa.toDouble())) }
        write(baroFile, a.toString())
    }

    private fun write(f: File, text: String) {
        val tmp = File(f.parentFile, f.name + ".tmp")
        tmp.writeText(text)
        if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f) }
    }

    companion object {
        fun json(j: Journal): JSONObject = JSONObject().apply {
            put("v", 1)
            put("obs", JSONArray().apply { j.obs.forEach { put(obs(it)) } })
            put("forecasts", JSONArray().apply {
                j.forecasts.forEach { put(JSONObject().put("day", it.day.toString()).put("mine", it.mine.name).put("station", it.station.name).put("at", it.at)) }
            })
            put("seen", JSONArray().apply { j.seen.forEach { put(JSONObject().put("kind", it.kind.name).put("at", it.at)) } })
            put("verdicts", JSONObject().apply { j.verdicts.forEach { (k, v) -> put(k, v.name) } })
            put("gear", JSONArray().apply {
                j.gear.forEach { put(JSONObject().put("id", it.id).put("icon", it.icon).put("name", it.name).put("builtin", it.builtin).put("on", it.on)) }
            })
        }

        fun obs(o: Obs): JSONObject = JSONObject().apply {
            put("id", o.id); put("at", o.at); put("slot", o.slot.name)
            put("wind", o.wind); put("cloud", o.cloud); put("vis", o.vis)
            put("rain", JSONArray().apply { o.rain.forEach { put(JSONArray().put(it.zone).put(it.level)) } })
            o.sleep?.let { put("sleep", it.toDouble()) }
            put("gear", JSONArray(o.gear.toList()))
            if (o.note.isNotEmpty()) put("note", o.note)
            o.hpa?.let { put("hpa", it.toDouble()) }
        }

        private fun <T> JSONArray?.list(f: (Int) -> T?): List<T> = if (this == null) emptyList() else (0 until length()).mapNotNull { runCatching { f(it) }.getOrNull() }

        fun parse(o: JSONObject): Journal = Journal(
            obs = o.optJSONArray("obs").list { i -> readObs(o.getJSONArray("obs").getJSONObject(i)) },
            forecasts = o.optJSONArray("forecasts").list { i ->
                val f = o.getJSONArray("forecasts").getJSONObject(i)
                Forecast(LocalDate.parse(f.getString("day")), Sky.valueOf(f.getString("mine")), Sky.valueOf(f.getString("station")), f.optLong("at"))
            },
            seen = o.optJSONArray("seen").list { i ->
                val s = o.getJSONArray("seen").getJSONObject(i)
                Seen(Rare.valueOf(s.getString("kind")), s.getLong("at"))
            },
            verdicts = o.optJSONObject("verdicts")?.let { v -> v.keys().asSequence().mapNotNull { k -> runCatching { k to Verdict.valueOf(v.getString(k)) }.getOrNull() }.toMap() } ?: emptyMap(),
            gear = o.optJSONArray("gear")?.list { i ->
                val g = o.getJSONArray("gear").getJSONObject(i)
                Gear(g.getString("id"), g.optString("icon", "•"), g.optString("name"), g.optBoolean("builtin"), g.optBoolean("on", true))
            }?.takeIf { it.isNotEmpty() } ?: Journal.EMPTY.gear,
        )

        fun readObs(o: JSONObject): Obs = Obs(
            id = o.getString("id"),
            at = o.getLong("at"),
            slot = Slot.valueOf(o.getString("slot")),
            wind = o.getInt("wind"),
            cloud = o.getInt("cloud"),
            vis = o.optInt("vis", 2),
            rain = o.optJSONArray("rain").list { i -> o.getJSONArray("rain").getJSONArray(i).let { Rain(it.getInt(0), it.getInt(1)) } },
            sleep = if (o.has("sleep")) o.getDouble("sleep").toFloat() else null,
            gear = o.optJSONArray("gear").list { i -> o.getJSONArray("gear").getString(i) }.toSet(),
            note = o.optString("note"),
            hpa = if (o.has("hpa")) o.getDouble("hpa").toFloat() else null,
        )
    }
}
