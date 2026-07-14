package com.castigaro.kapkrakenzahn.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.castigaro.kapkrakenzahn.data.AppSettings
import com.castigaro.kapkrakenzahn.world.LocationId
import kotlin.math.PI
import kotlin.math.sin

/**
 * Geloopte Hintergrund-Ambiente je Schauplatz — synthetisiert, ohne
 * Audio-Assets. Ein einziger AudioTrack im Static-Loop-Mode spielt einen
 * kurzen, nahtlosen Klangteppich; beim Ortswechsel wird er ausgetauscht.
 * Abschaltbar über [AppSettings].
 */
class Ambience(private val context: Context) {

    private var track: AudioTrack? = null
    private var current: LocationId? = null
    private var ducked = false

    /** Wechselt die Ambiente auf den Ort (nichts tun, wenn schon aktiv). */
    fun switchTo(location: LocationId) {
        if (!AppSettings.isAmbienceEnabled(context)) {
            stop()
            return
        }
        if (location == current && track != null) {
            applyVolume()
            return
        }
        stop()
        current = location
        val pcm = loopFor(location)
        runCatching { startLoop(pcm) }
    }

    /** Senkt die Lautstärke ab, solange gesprochen wird (Ducking). */
    fun duck(on: Boolean) {
        if (ducked == on) return
        ducked = on
        applyVolume()
    }

    /** Wendet die eingestellte Lautstärke auf den laufenden Track an. */
    fun applyVolume() {
        val gain = AppSettings.ambienceVolume(context) * (if (ducked) DUCK_FACTOR else 1f)
        runCatching { track?.setVolume(gain) }
    }

    fun stop() {
        runCatching {
            track?.pause()
            track?.flush()
            track?.release()
        }
        track = null
        current = null
    }

    private fun startLoop(pcm: ShortArray) {
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
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
        t.write(pcm, 0, pcm.size)
        t.setLoopPoints(0, pcm.size, -1)
        track = t
        applyVolume()
        t.play()
    }

    private fun loopFor(location: LocationId): ShortArray = when (location) {
        LocationId.HAFEN, LocationId.STRAND -> waves()
        LocationId.KLIPPEN, LocationId.LEUCHTTURM -> wind()
        LocationId.TAVERNE -> murmur()
        LocationId.MARKT -> murmur()
        LocationId.DSCHUNGEL -> jungle()
        LocationId.HOEHLE -> caveDrip()
    }

    // ---- Synthese ----------------------------------------------------------

    /** Zwei Sekunden nahtloses Wellenrauschen (moduliertes Tiefpass-Rauschen). */
    private fun waves(): ShortArray = filteredLoop(coeff = 0.94, gain = 0.6, lfoHz = 0.25)

    private fun wind(): ShortArray = filteredLoop(coeff = 0.97, gain = 0.7, lfoHz = 0.4)

    private fun murmur(): ShortArray = filteredLoop(coeff = 0.90, gain = 0.4, lfoHz = 0.0)

    private fun jungle(): ShortArray {
        val n = SAMPLE_RATE * 2
        val out = ShortArray(n)
        var last = 0.0
        val rnd = java.util.Random(7)
        for (i in 0 until n) {
            last = last * 0.9 + (rnd.nextDouble() * 2 - 1) * 0.1
            var s = last * 0.25
            // Gelegentliches Vogel-Zirpen.
            if (i % (SAMPLE_RATE / 2) < 900) {
                val t = (i % 900).toDouble() / SAMPLE_RATE
                s += sin(2 * PI * 2200 * t) * 0.15 * (1 - (i % 900) / 900.0)
            }
            out[i] = (s * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    private fun caveDrip(): ShortArray {
        val n = SAMPLE_RATE * 3
        val out = ShortArray(n)
        for (i in 0 until n) {
            var s = 0.0
            // Alle ~0,8 s ein hallender Tropfen.
            val period = (SAMPLE_RATE * 0.8).toInt()
            val pos = i % period
            if (pos < 1200) {
                val t = pos.toDouble() / SAMPLE_RATE
                s = sin(2 * PI * 1400 * t) * 0.4 * Math.exp(-12.0 * pos / 1200)
            }
            out[i] = (s * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /**
     * Zwei Sekunden tiefpass-gefiltertes Rauschen, optional mit einem
     * langsamen Lautstärke-LFO (Brandungsschwellen, Windböen). [coeff] steuert
     * die Dumpfheit (höher = tiefer/gedämpfter).
     */
    private fun filteredLoop(coeff: Double, gain: Double, lfoHz: Double): ShortArray {
        val n = SAMPLE_RATE * 2
        val out = ShortArray(n)
        val rnd = java.util.Random(42)
        var filter = 0.0
        for (i in 0 until n) {
            val white = rnd.nextDouble() * 2 - 1
            filter = filter * coeff + white * (1 - coeff)
            val lfo = if (lfoHz > 0.0) 0.5 + 0.5 * sin(2 * PI * lfoHz * i / SAMPLE_RATE) else 1.0
            // Verstärkung, damit das stark gedämpfte Signal hörbar bleibt.
            val v = filter * lfo * gain * 6
            out[i] = (v * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    companion object {
        private const val SAMPLE_RATE = 22050

        /** Faktor, auf den die Lautstärke während der Sprachausgabe sinkt. */
        private const val DUCK_FACTOR = 0.25f
    }
}
