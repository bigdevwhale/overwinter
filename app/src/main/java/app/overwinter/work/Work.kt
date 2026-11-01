package app.overwinter.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.overwinter.MainActivity
import app.overwinter.R
import app.overwinter.container
import app.overwinter.data.Barometer
import app.overwinter.data.Settings
import app.overwinter.domain.Baro
import app.overwinter.domain.Slot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** The call to the next synoptic term, and the hourly barograph. */
object Work {
    private const val CHANNEL = "terms"
    private const val TERM = "term"
    private const val BARO = "baro"

    /** The next term after [now] and its slot. */
    fun nextTerm(hours: List<Int>, now: LocalDateTime): Pair<LocalDateTime, Slot> {
        for (d in 0..1) {
            val date = now.toLocalDate().plusDays(d.toLong())
            hours.forEachIndexed { i, h ->
                val t = date.atTime(h, 0)
                if (t.isAfter(now)) return t to Slot.entries[i]
            }
        }
        return now.toLocalDate().plusDays(1).atTime(hours[0], 0) to Slot.MORNING
    }

    fun plan(ctx: Context, s: Settings) {
        val wm = runCatching { WorkManager.getInstance(ctx) }.getOrNull() ?: return
        if (!s.onboarded || !s.remind) {
            wm.cancelUniqueWork(TERM)
        } else {
            val (at, slot) = nextTerm(s.hours, LocalDateTime.now())
            val wait = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
            val req = OneTimeWorkRequestBuilder<TermWorker>()
                .setInitialDelay(wait.coerceAtLeast(0), TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("slot" to slot.name, "day" to at.toLocalDate().toEpochDay()))
                .build()
            wm.enqueueUniqueWork(TERM, ExistingWorkPolicy.REPLACE, req)
        }
        if (s.onboarded && Barometer.has(ctx)) {
            wm.enqueueUniquePeriodicWork(BARO, ExistingPeriodicWorkPolicy.KEEP, PeriodicWorkRequestBuilder<BaroWorker>(1, TimeUnit.HOURS).build())
        }
    }

    fun post(ctx: Context, slot: Slot) {
        if (ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, ctx.getString(R.string.n_channel), NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(ctx, 7,
            Intent(ctx, MainActivity::class.java).putExtra(MainActivity.OBSERVE, true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val title = ctx.getString(when (slot) { Slot.MORNING -> R.string.n_morning; Slot.DAY -> R.string.n_day; Slot.EVENING -> R.string.n_evening })
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(title)
            .setContentText(ctx.getString(R.string.n_body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(ctx).notify(7, n) }
    }
}

class TermWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val c = applicationContext.container
        val s = c.settings.now()
        val slot = runCatching { Slot.valueOf(inputData.getString("slot").orEmpty()) }.getOrDefault(Slot.MORNING)
        val day = LocalDate.ofEpochDay(inputData.getLong("day", LocalDate.now().toEpochDay()))
        // Already observed at this term (or later in the day): no need to call.
        val done = c.store.load().obs.any { it.day() == day && it.slot.ordinal >= slot.ordinal }
        if (s.remind && !done) Work.post(applicationContext, slot)
        Work.plan(applicationContext, s)
        return Result.success()
    }
}

class BaroWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val v = Barometer.read(applicationContext) ?: return Result.success()
        val c = applicationContext.container
        c.store.addBaro(Baro(System.currentTimeMillis(), v))
        c.refreshBaro()
        return Result.success()
    }
}
