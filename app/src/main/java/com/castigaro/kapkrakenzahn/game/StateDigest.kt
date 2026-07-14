package com.castigaro.kapkrakenzahn.game

import com.castigaro.kapkrakenzahn.data.DialogMessage
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.Hints
import com.castigaro.kapkrakenzahn.world.NpcId

/**
 * Verdichtet den Spielstand zu einem deutschen Lagebericht für den
 * LLM-Prompt: Was ist gelöst, was fehlt, was trägt die Heldin bei sich,
 * was weiß sie schon — plus die jüngste Gesprächshistorie mit der Figur.
 */
object StateDigest {

    /** Mehr Verlauf schicken wir nicht mit — ältere Beiträge fallen weg. */
    const val MAX_HISTORY = 20

    fun build(state: GameState): String {
        val v = GameWorld.variation(state)
        val geloest = GameWorld.puzzles.filter { GameWorld.isSolved(state, it.id) }
        val offen = GameWorld.puzzles.filterNot { GameWorld.isSolved(state, it.id) }

        return buildString {
            appendLine(
                "Spielziel: Jonna Klabauter will das erloschene Leuchtfeuer von Kap " +
                    "Krakenzahn neu entzünden. Dazu braucht sie das Krakenauge aus der " +
                    "Krakenhöhle und Zugang zum Leuchtturm.",
            )
            appendLine("Aktueller Ort: ${GameWorld.location(state.locationId).name}.")
            appendLine(
                if (state.inventory.isEmpty()) "Inventar: leer."
                else "Inventar: " + state.inventory.joinToString { GameWorld.itemName(it) } + ".",
            )
            if (geloest.isNotEmpty()) {
                appendLine("Bereits gelöst:")
                geloest.forEach { puzzle ->
                    val weg = GameWorld.solvedPath(state, puzzle.id)?.beschreibung ?: ""
                    appendLine("- ${puzzle.titel} ($weg)")
                }
            }
            if (offen.isNotEmpty()) {
                appendLine("Noch offen:")
                offen.forEach { appendLine("- ${it.titel}: ${it.beschreibung}") }
            }
            if (state.revealedHints.isNotEmpty()) {
                appendLine("Was Jonna bereits erfahren hat:")
                state.revealedHints.forEach { appendLine("- ${Hints.text(it, v)}") }
            }
            if (state.activeEvents.isNotEmpty()) {
                appendLine(
                    "Gerade am Kap: " + state.activeEvents.joinToString { it.typ } + ".",
                )
            }
        }.trim()
    }

    /** Gekappte Gesprächshistorie mit einer Figur. */
    fun history(state: GameState, npc: NpcId): List<DialogMessage> {
        val all = state.historyFor(npc)
        return if (all.size <= MAX_HISTORY) all.toList() else all.takeLast(MAX_HISTORY)
    }
}
