package app.overwinter.ui.observe

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import app.overwinter.domain.Telegram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/** The telegraph key: a soft 700 Hz tone keyed in Morse. */
object Key {
    private const val RATE = 22_050
    private const val DOT_MS = 60

    suspend fun play(text: String, maxMs: Int = 4_500) = withContext(Dispatchers.Default) {
        val units = Telegram.morse(text)
        val pcm = ArrayList<Short>()
        var ms = 0
        for (u in units) {
            val len = kotlin.math.abs(u) * DOT_MS
            if (ms + len > maxMs) break
            val n = RATE * len / 1000
            for (i in 0 until n) {
                val v = if (u > 0) {
                    // A short ramp at both ends keeps the key from clicking.
                    val env = minOf(1.0, i / 90.0, (n - i) / 90.0)
                    (sin(2 * PI * 700 * i / RATE) * env * 0.35 * Short.MAX_VALUE).toInt().toShort()
                } else 0
                pcm.add(v)
            }
            ms += len
        }
        if (pcm.isEmpty()) return@withContext
        val data = pcm.toShortArray()
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(data.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        }.getOrNull() ?: return@withContext
        runCatching {
            track.write(data, 0, data.size)
            track.play()
            kotlinx.coroutines.delay(ms.toLong() + 200)
        }
        runCatching { track.stop() }
        track.release()
    }
}
