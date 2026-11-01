package app.overwinter

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import app.overwinter.data.Barometer
import app.overwinter.data.DemoSeed
import app.overwinter.data.Settings
import app.overwinter.data.SettingsStore
import app.overwinter.data.Store
import app.overwinter.domain.Baro
import app.overwinter.domain.Forecast
import app.overwinter.domain.Gear
import app.overwinter.domain.Journal
import app.overwinter.domain.Obs
import app.overwinter.domain.Rare
import app.overwinter.domain.Seen
import app.overwinter.domain.Sky
import app.overwinter.domain.Station
import app.overwinter.domain.Verdict
import app.overwinter.domain.Weather
import app.overwinter.work.Work
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

class AppContainer(context: Context) {
    val app: Context = context.applicationContext
    val settings = SettingsStore(app)
    val store = Store(File(app.filesDir, "station"))
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val ready = MutableStateFlow(false)
    val journal = MutableStateFlow(Journal.EMPTY)
    val tape = MutableStateFlow<List<Baro>>(emptyList())
    /** A notification asked to start a term. */
    val observe = MutableStateFlow(false)

    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        journal.value = store.load()
        tape.value = store.baro()
    }

    suspend fun refreshBaro() = withContext(Dispatchers.IO) { tape.value = store.baro() }

    private suspend fun edit(change: (Journal) -> Journal) = withContext(Dispatchers.IO) {
        val j = change(store.load())
        store.save(j)
        journal.value = j
    }

    fun names(): List<String> = app.resources.getStringArray(R.array.cyclones).toList()

    /** Reads the barometer now and puts it on the tape. */
    suspend fun readBaro(): Float? {
        val v = Barometer.read(app) ?: return null
        withContext(Dispatchers.IO) { store.addBaro(Baro(System.currentTimeMillis(), v)) }
        refreshBaro()
        return v
    }

    /** Files an observation; returns the rare sights it brought. */
    suspend fun observe(draft: Obs, s: Settings): List<Rare> {
        val hpa = draft.hpa ?: readBaro()
        val o = draft.copy(id = draft.id.ifEmpty { UUID.randomUUID().toString().replace("-", "").take(16) }, hpa = hpa)
        var found = emptyList<Rare>()
        edit { j ->
            val today = o.day()
            val days = Weather.days(j.obs + o)
            val fronts = Weather.fronts(days, names(), today)
            found = Station.rare(o, j.obs, fronts, s.startDate, false, j.seen)
            j.copy(obs = j.obs + o, seen = j.seen + found.map { Seen(it, o.at) })
        }
        return found
    }

    suspend fun delete(id: String) = edit { j -> j.copy(obs = j.obs.filterNot { it.id == id }) }

    /** My evening forecast for tomorrow; the station makes its own at the same moment. */
    suspend fun forecast(mine: Sky, station: Sky) = edit { j ->
        val day = LocalDate.now().plusDays(1)
        j.copy(forecasts = j.forecasts.filterNot { it.day == day } + Forecast(day, mine, station, System.currentTimeMillis()))
    }

    /** Keeping a rule for the first time brings a radiogram from the mainland. */
    suspend fun verdict(key: String, v: Verdict) = edit { j ->
        val first = v == Verdict.KEPT && j.verdicts.values.none { it == Verdict.KEPT }
        j.copy(
            verdicts = j.verdicts + (key to v),
            seen = if (first) j.seen + Seen(Rare.RADIOGRAM, System.currentTimeMillis()) else j.seen,
        )
    }

    suspend fun gear(list: List<Gear>) = edit { it.copy(gear = list) }

    fun replan() = write { Work.plan(app, settings.now()) }

    // ---- sharing a report ----

    fun shareIntent(f: File, mime: String): Intent {
        val uri = FileProvider.getUriForFile(app, "${app.packageName}.files", f)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            null,
        )
    }
}

class OverwinterApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.scope.launch {
            try {
                if (BuildConfig.DEMO) DemoSeed.seedIfEmpty(this@OverwinterApp, container)
                container.refresh()
                Work.plan(this@OverwinterApp, container.settings.now())
            } finally {
                container.ready.value = true
            }
        }
    }
}

val Context.container: AppContainer get() = (applicationContext as OverwinterApp).container

val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"

val Context.locale: Locale get() = resources.configuration.locales[0]
