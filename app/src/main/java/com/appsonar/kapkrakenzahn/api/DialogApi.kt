package com.appsonar.kapkrakenzahn.api

import android.content.Context
import com.appsonar.kapkrakenzahn.data.DialogMessage
import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.game.StateDigest
import com.appsonar.kapkrakenzahn.world.AllowedEffect
import com.appsonar.kapkrakenzahn.world.Effect
import com.appsonar.kapkrakenzahn.world.GameWorld
import com.appsonar.kapkrakenzahn.world.NpcId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Die KI-gestützten Gespräche mit den Figuren. Der Systemprompt enthält die
 * Charakterkarte, den verdichteten Spielstand und die Whitelist der Effekte,
 * die diese Figur gerade zugestehen darf. Das Modell antwortet strikt als
 * JSON mit NPC-Text, 2–4 Antwortoptionen (der Spieler tippt nie frei) und
 * optionalen Effekt-Ids. Alles außerhalb der Whitelist wird verworfen — die
 * KI kann Hinweise enthüllen und freigeschaltete Übergaben auslösen, aber
 * die Spiellogik nie korrumpieren.
 */
object DialogApi {

    /** Ein Gesprächszug: NPC-Rede, feste Optionen, angewandte Effekte. */
    class DialogTurn(
        val sag: String,
        val optionen: List<String>,
        val effects: List<Effect>,
        val beendet: Boolean,
    )

    /**
     * Erzeugt den nächsten Gesprächszug. Bei [gewaehlteOption] == null die
     * Begrüßung. Wirft [LlmClient.ApiException], wenn kein Key aktiv ist
     * oder der Anbieter einen Fehler liefert — die UI weicht dann auf den
     * geskripteten Ersatz-Dialog aus.
     */
    suspend fun turn(
        context: Context,
        npcId: NpcId,
        state: GameState,
        gewaehlteOption: String?,
    ): DialogTurn = withContext(Dispatchers.IO) {
        val allowed = GameWorld.allowedEffectsFor(state, npcId)
        val system = buildSystemPrompt(npcId, state, allowed)
        val turns = buildTurns(state, npcId, gewaehlteOption)

        val raw = LlmClient.chat(context, system, turns)
        parseTurn(raw, state, npcId, allowed)
    }

    /** Baut die abwechselnde Nutzer/Assistent-Historie für den Chat-Call. */
    internal fun buildTurns(
        state: GameState,
        npcId: NpcId,
        gewaehlteOption: String?,
    ): List<LlmClient.Turn> {
        val history = StateDigest.history(state, npcId)
        val turns = mutableListOf<LlmClient.Turn>()
        history.forEach { msg ->
            val role = if (msg.role == DialogMessage.ROLE_PLAYER) {
                LlmClient.Turn.ROLE_USER
            } else {
                LlmClient.Turn.ROLE_ASSISTANT
            }
            turns.add(LlmClient.Turn(role, msg.text))
        }
        // Die Spieler-Instruktion bzw. gewählte Option als letzter Nutzerzug.
        val userText = gewaehlteOption
            ?: "(Die Heldin Jonna tritt an dich heran und beginnt das Gespräch.)"
        turns.add(LlmClient.Turn(LlmClient.Turn.ROLE_USER, userText))
        return turns
    }

    internal fun buildSystemPrompt(
        npcId: NpcId,
        state: GameState,
        allowed: List<AllowedEffect>,
    ): String {
        val npc = GameWorld.npc(npcId)
        val effektKatalog = if (allowed.isEmpty()) {
            "(keine — diese Figur kann gerade nichts weitergeben oder auslösen)"
        } else {
            allowed.joinToString("\n") { "- \"${it.typ}:${it.id}\" → ${it.beschreibung}" }
        }
        return """
            Du spielst eine Figur in einem deutschsprachigen Piraten-Adventure
            („Kap Krakenzahn", im Stil klassischer Piraten-Adventures der 90er).
            Bleibe konsequent in der Rolle und antworte auf Deutsch.

            DEINE ROLLE — ${npc.name}:
            ${npc.charakterkarte}

            SPIELSTAND (nur dein Wissen als Figur; verrate Rätsellösungen nur,
            wenn es unten ausdrücklich erlaubt ist):
            ${StateDigest.build(state)}

            ERLAUBTE ZUGESTÄNDNISSE (nur diese darfst du auslösen; ihre Ids
            gehören in das Feld "effekte"; nutze ein Zugeständnis nur, wenn es
            im Gespräch inhaltlich passt und die Heldin es sich verdient hat):
            $effektKatalog

            ANTWORTFORMAT — antworte AUSSCHLIESSLICH mit einem JSON-Objekt in
            exakt dieser Form, ohne weiteren Text:
            {
              "sag": "<deine Rede als Figur, 1-3 Sätze, lebendig und in Rolle>",
              "optionen": ["<2 bis 4 kurze Repliken AUS SICHT DER HELDIN JONNA>"],
              "effekte": ["<0 oder mehr Ids aus der Liste oben, exakt wie dort>"]
            }

            REGELN:
            - Der Spieler kann NUR aus deinen "optionen" wählen — biete immer
              2 bis 4 sinnvolle, unterschiedliche Repliken an, die letzte ist
              stets eine Abschiedsfloskel.
            - Die Optionen sind Sätze, die JONNA sagt, nicht du.
            - Nutze "effekte" nur mit Ids aus der Liste oben. Erfinde niemals
              neue Ids und gib niemals ein Zugeständnis, das nicht gelistet ist.
            - Enthält "effekte" eine Abschieds-Situation nicht: lass das Feld
              leer. Endet das Gespräch (Jonna verabschiedet sich), darf die
              Option gewählt werden — du musst nichts Besonderes tun.
            - Halte alles familienfreundlich.
            - Erfinde keine Bezüge zu real existierenden Spielen, Filmen,
              Büchern, Marken oder deren Figuren — die Spielwelt ist
              eigenständig.
        """.trimIndent()
    }

    /**
     * Parst die Modellantwort und filtert die Effekte gegen die Whitelist.
     * Der letzte Optionseintrag gilt als Abschied; wählt der Spieler ihn,
     * endet das Gespräch (das entscheidet die UI, nicht das Modell).
     */
    internal fun parseTurn(
        raw: String,
        state: GameState,
        npcId: NpcId,
        allowed: List<AllowedEffect>,
    ): DialogTurn {
        val json = LlmClient.extractJson(raw)
        val sag = json.optString("sag").trim().ifBlank { "…" }

        val optionen = mutableListOf<String>()
        json.optJSONArray("optionen")?.let { arr ->
            for (i in 0 until arr.length()) {
                val opt = arr.optString(i).trim()
                if (opt.isNotEmpty()) optionen.add(opt)
            }
        }
        if (optionen.isEmpty()) optionen.add("Tschüss.")

        val effects = mutableListOf<Effect>()
        json.optJSONArray("effekte")?.let { arr ->
            for (i in 0 until arr.length()) {
                val id = arr.optString(i).trim()
                resolveWhitelisted(id, state, npcId, allowed)?.let { effects.addAll(it) }
            }
        }
        val geerzwungen = GameWorld.enforceAmulettTausch(state, effects)

        return DialogTurn(sag = sag, optionen = optionen, effects = geerzwungen, beendet = false)
    }

    /**
     * Löst eine vom Modell genannte Effekt-Id gegen die Whitelist auf.
     * Akzeptiert „typ:id" ebenso wie eine nackte Id. Nicht gelistete oder
     * bereits ungültige Zugeständnisse (Bedingung nicht mehr erfüllt) →
     * null (verworfen).
     */
    private fun resolveWhitelisted(
        rawId: String,
        state: GameState,
        npcId: NpcId,
        allowed: List<AllowedEffect>,
    ): List<Effect>? {
        val id = rawId.substringAfter(':').trim()
        val match = allowed.firstOrNull { it.id == id && it.condition(state) } ?: return null
        return GameWorld.toEffects(state, npcId, match)
    }

    /** Baut aus einer JSON-Antwort direkt das reine JSON (für Tests). */
    internal fun rawJson(raw: String): JSONObject = LlmClient.extractJson(raw)
}
