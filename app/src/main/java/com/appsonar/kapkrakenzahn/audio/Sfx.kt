package com.appsonar.kapkrakenzahn.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.appsonar.kapkrakenzahn.data.AppSettings
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Kurze Soundeffekte — komplett synthetisiert (keine Audio-Assets). Jeder
 * Clip wird einmal als PCM erzeugt und über einen frischen AudioTrack im
 * Static-Mode abgespielt. Abschaltbar über [AppSettings].
 */
object Sfx {

    private const val SAMPLE_RATE = 22050

    /** Spielt einen benannten Effekt (siehe Effect.PlaySfx-Namen). */
    fun play(context: Context, name: String) {
        if (!AppSettings.isSfxEnabled(context)) return
        val pcm = clips[name] ?: return
        runCatching { emit(pcm, AppSettings.sfxVolume(context)) }
    }

    private val clips: Map<String, ShortArray> by lazy {
        mapOf(
            "blip" to sine(880.0, 70, 0.35),
            "pickup" to arpeggio(listOf(660.0, 880.0, 1180.0), 60),
            "tuer" to sweep(180.0, 90.0, 320, 0.4),
            "wasser" to noiseSweep(300, 0.25),
            "erfolg" to arpeggio(listOf(523.0, 659.0, 784.0, 1046.0), 90),
            "finale" to arpeggio(listOf(523.0, 659.0, 784.0, 1046.0, 1318.0), 140),
            "fehler" to sweep(300.0, 120.0, 200, 0.35),
        )
    }

    // ---- Synthese-Helfer ---------------------------------------------------

    private fun sine(freq: Double, ms: Int, gain: Double): ShortArray {
        val n = SAMPLE_RATE * ms / 1000
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = envelope(i, n)
            out[i] = (sin(2 * PI * freq * t) * env * gain * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun arpeggio(freqs: List<Double>, msEach: Int): ShortArray {
        val parts = freqs.map { sine(it, msEach, 0.35) }
        val total = parts.sumOf { it.size }
        val out = ShortArray(total)
        var pos = 0
        parts.forEach { part ->
            part.copyInto(out, pos)
            pos += part.size
        }
        return out
    }

    private fun sweep(fromHz: Double, toHz: Double, ms: Int, gain: Double): ShortArray {
        val n = SAMPLE_RATE * ms / 1000
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val freq = fromHz + (toHz - fromHz) * frac
            phase += 2 * PI * freq / SAMPLE_RATE
            val env = envelope(i, n)
            out[i] = (sin(phase) * env * gain * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private fun noiseSweep(ms: Int, gain: Double): ShortArray {
        val n = SAMPLE_RATE * ms / 1000
        val out = ShortArray(n)
        var last = 0.0
        val rnd = java.util.Random(1)
        for (i in 0 until n) {
            // Tiefpass-gefiltertes Rauschen ergibt ein sanftes Plätschern.
            val white = rnd.nextDouble() * 2 - 1
            last = last * 0.92 + white * 0.08
            val env = envelope(i, n)
            out[i] = (last * env * gain * Short.MAX_VALUE * 4).toInt()
                .coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Sanftes An- und Abschwellen, damit es nicht knackt. */
    private fun envelope(i: Int, n: Int): Double {
        val attack = (n * 0.05).toInt().coerceAtLeast(1)
        val a = if (i < attack) i.toDouble() / attack else 1.0
        val decay = exp(-3.0 * i / n)
        return a * decay
    }

    private fun emit(pcm: ShortArray, gain: Float) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(pcm, 0, pcm.size)
        track.setVolume(gain)
        track.setNotificationMarkerPosition(pcm.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(t: AudioTrack?) {
                runCatching { t?.release() }
            }

            override fun onPeriodicNotification(t: AudioTrack?) {}
        })
        track.play()
    }
}
