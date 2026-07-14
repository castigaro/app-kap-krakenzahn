package com.castigaro.kapkrakenzahn.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import com.castigaro.kapkrakenzahn.data.AppSettings
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.NpcId
import java.util.Locale

/**
 * Sprachausgabe der Figuren per Android Text-to-Speech — wie bei StoryTeller,
 * hier zusätzlich mit figurenspezifischer Tonhöhe/Geschwindigkeit, damit sich
 * die Stimmen unterscheiden. Abschaltbar über [AppSettings].
 */
class Speaker(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
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
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
