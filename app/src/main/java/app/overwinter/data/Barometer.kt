package app.overwinter.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import app.overwinter.domain.Baro
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** The phone's own barometer, if it has one. Nothing leaves the phone. */
object Barometer {
    fun has(ctx: Context): Boolean = ctx.getSystemService(SensorManager::class.java)?.getDefaultSensor(Sensor.TYPE_PRESSURE) != null

    /** One reading: the median of a few samples over a second or so. */
    suspend fun read(ctx: Context): Float? {
        val sm = ctx.getSystemService(SensorManager::class.java) ?: return null
        val sensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE) ?: return null
        return withTimeoutOrNull(4_000) {
            suspendCancellableCoroutine { cont ->
                val got = ArrayList<Float>()
                val l = object : SensorEventListener {
                    override fun onSensorChanged(e: SensorEvent) {
                        val v = e.values.firstOrNull() ?: return
                        if (v < 800f || v > 1100f) return
                        got += v
                        if (got.size >= 5 && cont.isActive) {
                            sm.unregisterListener(this)
                            cont.resume(got.sorted()[got.size / 2])
                        }
                    }
                    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
                }
                sm.registerListener(l, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                cont.invokeOnCancellation { sm.unregisterListener(l) }
            }
        }
    }

    /** The reading closest to [at], within an hour and a half. */
    fun near(tape: List<Baro>, at: Long): Float? =
        tape.minByOrNull { kotlin.math.abs(it.at - at) }?.takeIf { kotlin.math.abs(it.at - at) <= 90 * 60_000L }?.hpa

    /** How much the pressure changed over [hours] up to [at]; negative is falling. */
    fun change(tape: List<Baro>, at: Long, hours: Int): Float? {
        val now = near(tape, at) ?: return null
        val then = near(tape, at - hours * 3_600_000L) ?: return null
        return now - then
    }
}
