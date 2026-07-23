package com.appsonar.kapkrakenzahn.api

import android.content.Context
import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.game.Variation
import com.appsonar.kapkrakenzahn.world.GameWorld
import com.appsonar.kapkrakenzahn.world.NpcId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Das „Duell der spitzen Zungen" — ein KI-generiertes Wortgefecht im Stil
 * klassischer Adventure-Wortduelle. Pro Runde liefert das Modell eine
 * Beleidigung des Gegners und drei mögliche Konter der Heldin, von denen
 * genau einer trifft. Die App wertet den gewählten Konter deterministisch aus.
 */
object DuelApi {

    /** Eine Duell-Runde: gegnerische Beleidigung, Konter-Auswahl, Treffer-Index. */
    class DuelRound(
        val beleidigung: String,
        val kontern: List<String>,
        val richtig: Int,
    )

    suspend fun round(
        context: Context,
        state: GameState,
        bisher: List<String>,
    ): DuelRound = withContext(Dispatchers.IO) {
        val system = buildSystemPrompt(state)
        val turns = listOf(
            LlmClient.Turn(
                LlmClient.Turn.ROLE_USER,
                if (bisher.isEmpty()) {
                    "Beginne das Wortduell mit deiner ersten Beleidigung."
                } else {
                    "Bisheriger Verlauf:\n" + bisher.joinToString("\n") +
                        "\nMach mit der nächsten Beleidigung weiter."
                },
            ),
        )
        val raw = LlmClient.chat(context, system, turns)
        parseRound(raw)
    }

    internal fun buildSystemPrompt(state: GameState): String {
        val thema = Variation(state.seed).duellThema
        val rasmus = GameWorld.npc(NpcId.RASMUS)
        return """
            Du inszenierst ein „Duell der spitzen Zungen" (ein humorvolles
            Wortgefecht im Stil klassischer Piraten-Adventures) zwischen dem
            Piraten ${rasmus.name} und der Heldin Jonna. Auf Deutsch,
            familienfreundlich, mit reichlich piratigem Wortwitz. Kein echter
            Kampf — es geht nur um spitze Zungen. Erfinde keine Bezüge zu real
            existierenden Spielen, Filmen, Büchern, Marken oder deren Figuren —
            die Spielwelt ist eigenständig.

            Thema dieser Runde: $thema.

            Ablauf: Rasmus wirft Jonna eine freche Beleidigung an den Kopf.
            Du lieferst drei mögliche Konter für Jonna. GENAU EINER davon ist
            eine schlagfertige, thematisch passende Retourkutsche, die Rasmus
            sprachlos macht; die beiden anderen klingen zwar frech, gehen aber
            am Thema vorbei oder sind lahm.

            Antworte AUSSCHLIESSLICH als JSON in exakt dieser Form:
            {
              "beleidigung": "<Rasmus' Beleidigung, 1-2 Sätze>",
              "kontern": ["<Konter 1>", "<Konter 2>", "<Konter 3>"],
              "richtig": <Index 0, 1 oder 2 des treffenden Konters>
            }

            Variiere die Position des richtigen Konters. Kein weiterer Text.
        """.trimIndent()
    }

    internal fun parseRound(raw: String): DuelRound {
        val json = LlmClient.extractJson(raw)
        val beleidigung = json.optString("beleidigung").trim().ifBlank {
            "Du kämpfst wie ein Milchbrötchen!"
        }
        val kontern = mutableListOf<String>()
        json.optJSONArray("kontern")?.let { arr ->
            for (i in 0 until arr.length()) {
                val k = arr.optString(i).trim()
                if (k.isNotEmpty()) kontern.add(k)
            }
        }
        var richtig = json.optInt("richtig", 0)
        if (richtig !in kontern.indices) richtig = 0
        return DuelRound(beleidigung, kontern, richtig)
    }
}
