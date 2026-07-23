package com.appsonar.kapkrakenzahn.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.appsonar.kapkrakenzahn.data.AppSettings
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Sanfte Hintergrundmusik — wie Ambiente und Soundeffekte komplett
 * synthetisiert, ohne Audio-Assets. Ein ruhiger 8-Takt-Walzer in C-Dur
 * (getragene Akkordflächen, Bass auf der Eins, sparsame pentatonische
 * Melodie) läuft als nahtloser Loop. Lautstärke und Schalter über
 * [AppSettings]; während der Sprachausgabe wird geduckt.
 */
class Music(private val context: Context) {

    private var track: AudioTrack? = null
    private var ducked = false

    /** Startet den Loop (bzw. wendet nur die Lautstärke an, wenn er läuft). */
    fun start() {
        if (!AppSettings.isMusicEnabled(context)) {
            stop()
            return
        }
        if (track != null) {
            applyVolume()
            return
        }
        runCatching { startLoop() }
    }

    fun stop() {
        runCatching {
            track?.pause()
            track?.flush()
            track?.release()
        }
        track = null
    }

    /** Senkt die Lautstärke ab, solange gesprochen wird (Ducking). */
    fun duck(on: Boolean) {
        if (ducked == on) return
        ducked = on
        applyVolume()
    }

    /** Wendet die eingestellte Lautstärke auf den laufenden Track an. */
    fun applyVolume() {
        val gain = AppSettings.musicVolume(context) * (if (ducked) DUCK_FACTOR else 1f)
        runCatching { track?.setVolume(gain) }
    }

    private fun startLoop() {
        val pcm = LOOP
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

    companion object {
        private const val SAMPLE_RATE = 22050

        /** Faktor, auf den die Lautstärke während der Sprachausgabe sinkt. */
        private const val DUCK_FACTOR = 0.2f

        // Ein 3/4-Takt bei 90 bpm dauert zwei Sekunden; der Loop hat acht Takte.
        private const val BEAT = SAMPLE_RATE * 2 / 3
        private const val BAR = BEAT * 3
        private const val BARS = 8

        /** Akkordfolge C — a-Moll — F — G, je zwei Takte (Pad-Frequenzen). */
        private val CHORDS = listOf(
            doubleArrayOf(261.63, 329.63, 392.00), // C-Dur
            doubleArrayOf(220.00, 261.63, 329.63), // a-Moll
            doubleArrayOf(174.61, 220.00, 261.63), // F-Dur
            doubleArrayOf(196.00, 246.94, 293.66), // G-Dur
        )

        /** Grundton des Basses je Takt. */
        private val BASS = doubleArrayOf(
            130.81, 130.81, 110.00, 110.00, 87.31, 87.31, 98.00, 98.00,
        )

        /** Melodie: ein Ton je Schlag, 0.0 = Pause (C-Dur-Pentatonik). */
        private val MELODY = doubleArrayOf(
            329.63, 392.00, 0.0, // Takt 1
            440.00, 392.00, 329.63, // Takt 2
            523.25, 0.0, 440.00, // Takt 3
            392.00, 329.63, 0.0, // Takt 4
            440.00, 523.25, 0.0, // Takt 5
            587.33, 523.25, 440.00, // Takt 6
            392.00, 0.0, 329.63, // Takt 7
            587.33, 392.00, 0.0, // Takt 8
        )

        /** Der fertige 16-Sekunden-Loop, einmalig synthetisiert. */
        private val LOOP: ShortArray by lazy { render() }

        private fun render(): ShortArray {
            val n = BAR * BARS
            val out = DoubleArray(n)
            renderPads(out)
            renderBass(out)
            renderMelody(out)
            return ShortArray(n) { i ->
                (out[i] * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
            }
        }

        /** Getragene Akkordflächen, je zwei Takte, mit weichem An-/Abschwellen. */
        private fun renderPads(out: DoubleArray) {
            val seg = BAR * 2
            val ramp = SAMPLE_RATE / 2 // 0,5 s Ein- und Ausblenden
            for (c in CHORDS.indices) {
                val start = c * seg
                for (i in 0 until seg) {
                    val env = min(1.0, min(i.toDouble() / ramp, (seg - 1 - i).toDouble() / ramp))
                    val t = i.toDouble() / SAMPLE_RATE
                    var s = 0.0
                    for (freq in CHORDS[c]) s += sin(2 * PI * freq * t)
                    out[start + i] += s * 0.045 * env
                }
            }
        }

        /** Ein weicher Basston auf der Eins jedes Takts. */
        private fun renderBass(out: DoubleArray) {
            for (bar in 0 until BARS) {
                val start = bar * BAR
                val freq = BASS[bar]
                for (i in 0 until BAR) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = attack(i) * exp(-2.5 * i / BAR)
                    out[start + i] += sin(2 * PI * freq * t) * 0.16 * env
                }
            }
        }

        /** Sparsame gezupfte Melodie, ein Ton (oder Pause) je Schlag. */
        private fun renderMelody(out: DoubleArray) {
            for (beat in MELODY.indices) {
                val freq = MELODY[beat]
                if (freq == 0.0) continue
                val start = beat * BEAT
                for (i in 0 until BEAT) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = attack(i) * exp(-4.0 * i / BEAT)
                    // Grundton plus leiser Oktav-Oberton für einen wärmeren Klang.
                    val s = sin(2 * PI * freq * t) + 0.35 * sin(2 * PI * freq * 2 * t)
                    out[start + i] += s * 0.12 * env
                }
            }
        }

        /** Kurzes Einschwingen gegen Knackser. */
        private fun attack(i: Int): Double {
            val a = SAMPLE_RATE / 200 // 5 ms
            return if (i < a) i.toDouble() / a else 1.0
        }
    }
}
