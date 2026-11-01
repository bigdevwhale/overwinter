package app.overwinter.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** The station's name in its telegrams. */
    val callsign: String = "",
    /** 0: as the system, 1: polar day, 2: polar night. */
    val theme: Int = 0,
    /** The hours of the three terms. */
    val hours: List<Int> = listOf(9, 15, 21),
    val remind: Boolean = true,
    /** Morse when a telegram is sent. */
    val sound: Boolean = true,
    /** The day the station opened, as an epoch day; the shifts count from it. */
    val started: Long = LocalDate.now().toEpochDay(),
) {
    val startDate: LocalDate get() = LocalDate.ofEpochDay(started)
}

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read() = Settings(
        onboarded = this[ONBOARDED] ?: false,
        callsign = this[CALLSIGN] ?: "",
        theme = this[THEME] ?: 0,
        hours = this[HOURS]?.split(",")?.mapNotNull { it.toIntOrNull() }?.takeIf { it.size == 3 } ?: listOf(9, 15, 21),
        remind = this[REMIND] ?: true,
        sound = this[SOUND] ?: true,
        started = this[STARTED] ?: LocalDate.now().toEpochDay(),
    )

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            it[CALLSIGN] = s.callsign
            it[THEME] = s.theme
            it[HOURS] = s.hours.joinToString(",")
            it[REMIND] = s.remind
            it[SOUND] = s.sound
            it[STARTED] = s.started
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val CALLSIGN = stringPreferencesKey("callsign")
        val THEME = intPreferencesKey("theme")
        val HOURS = stringPreferencesKey("hours")
        val REMIND = booleanPreferencesKey("remind")
        val SOUND = booleanPreferencesKey("sound")
        val STARTED = longPreferencesKey("started")
    }
}
