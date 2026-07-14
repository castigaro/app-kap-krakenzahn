package com.castigaro.kapkrakenzahn.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.castigaro.kapkrakenzahn.data.AppSettings
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.NpcId
import java.util.Locale

/**
 * Sprachausgabe der Figuren per Android Text-to-Speech — wie bei StoryTeller,
 * hier zusätzlich mit figurenspezifischer Tonhöhe/Geschwindigkeit, damit sich
 * die Stimmen unterscheiden. Abschaltbar über [AppSettings].
 *
 * [onSpeaking] meldet Sprech-Beginn und -Ende auf dem Main-Thread, damit
 * Ambiente und Musik währenddessen geduckt werden können.
 */
class Speaker(context: Context, private val onSpeaking: ((Boolean) -> Unit)? = null) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val main = Handler(Looper.getMainLooper())

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = emitSpeaking(true)
                    override fun onDone(utteranceId: String?) = emitSpeaking(false)
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = emitSpeaking(false)
                    override fun onStop(utteranceId: String?, interrupted: Boolean) =
                        emitSpeaking(false)
                })
                ready = true
            }
        }
    }

    /** Spricht Text in der Stimme einer Figur (null = neutrale Erzählstimme). */
    fun speak(context: Context, text: String, npc: NpcId? = null) {
        if (!ready || !AppSettings.isTtsEnabled(context)) return
        val t = tts ?: return
        if (npc != null) {
            val figur = GameWorld.npc(npc)
            t.setPitch(figur.voicePitch)
            t.setSpeechRate(figur.voiceRate)
        } else {
            t.setPitch(1f)
            t.setSpeechRate(1f)
        }
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kk")
    }

    fun stop() {
        tts?.stop()
        emitSpeaking(false)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
        emitSpeaking(false)
    }

    /** Die TTS-Callbacks kommen auf einem Hintergrund-Thread. */
    private fun emitSpeaking(speaking: Boolean) {
        main.post { onSpeaking?.invoke(speaking) }
    }
}
