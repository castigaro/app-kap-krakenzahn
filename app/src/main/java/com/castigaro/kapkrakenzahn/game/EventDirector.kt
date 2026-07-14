package com.castigaro.kapkrakenzahn.game

import com.castigaro.kapkrakenzahn.data.ActiveEvent
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.world.FlagId
import com.castigaro.kapkrakenzahn.world.LocationId
import kotlin.random.Random

/**
 * Würfelt bei Ortswechseln kleine Zufallsereignisse an und lässt laufende
 * wieder abklingen. Deterministisch aus Seed + Schrittzahl — derselbe
 * Spielverlauf erzeugt dieselben Ereignisse, verschiedene Durchläufe
 * unterscheiden sich.
 */
object EventDirector {

    private const val FIELO_CHANCE = 0.15f
    private const val STURM_CHANCE = 0.08f
    private const val SPRINGFLUT_CHANCE = 0.08f
    private const val FIELO_DAUER = 6
    private const val STURM_DAUER = 4
    private const val SPRINGFLUT_DAUER = 3

    /** Orte, an denen der Händler Fielo auftauchen kann. */
    private val FIELO_ORTE = setOf(
        LocationId.HAFEN, LocationId.MARKT, LocationId.STRAND, LocationId.TAVERNE,
    )

    fun onLocationChange(state: GameState): String? {
        val abgeklungen = tick(state)
        val neu = maybeTrigger(state)
        return neu ?: abgeklungen
    }

    /** Zählt laufende Ereignisse herunter; meldet, wenn eines endet. */
    fun tick(state: GameState): String? {
        var meldung: String? = null
        val iterator = state.activeEvents.iterator()
        while (iterator.hasNext()) {
            val event = iterator.next()
            event.restSchritte -= 1
            if (event.restSchritte <= 0) {
                iterator.remove()
                meldung = when (event.typ) {
                    ActiveEvent.EVENT_STURM -> "Der Sturm über den Klippen verzieht sich — der Pfad ist wieder frei."
                    ActiveEvent.EVENT_SPRINGFLUT -> "Die Springflut ebbt ab. Das Meer atmet wieder ruhig."
                    ActiveEvent.EVENT_FIELO -> "Fielo packt seinen Bauchladen zusammen und zieht pfeifend weiter."
                    else -> meldung
                }
            }
        }
        return meldung
    }

    private fun maybeTrigger(state: GameState): String? {
        if (state.finished) return null
        val rnd = Random(state.seed * 31 + state.stepCount)

        val fieloAktiv = state.activeEvents.any { it.typ == ActiveEvent.EVENT_FIELO }
        if (!fieloAktiv && state.locationId in FIELO_ORTE && rnd.nextFloat() < FIELO_CHANCE) {
            state.activeEvents.add(
                ActiveEvent(ActiveEvent.EVENT_FIELO, FIELO_DAUER, state.locationId),
            )
            return "Am Wegrand baut plötzlich ein Händler seinen Bauchladen auf: Fielo ist am Kap — und heute angeblich in Geschenklaune."
        }

        val sturmAktiv = state.activeEvents.any { it.typ == ActiveEvent.EVENT_STURM }
        val spielerImSturmgebiet = state.locationId == LocationId.KLIPPEN ||
            state.locationId == LocationId.LEUCHTTURM
        if (!sturmAktiv && !spielerImSturmgebiet && rnd.nextFloat() < STURM_CHANCE) {
            state.activeEvents.add(
                ActiveEvent(ActiveEvent.EVENT_STURM, STURM_DAUER, LocationId.KLIPPEN),
            )
            return "Dunkle Wolken jagen heran — ein Sturm peitscht über den Klippenpfad. Da oben hält sich jetzt niemand auf den Beinen."
        }

        // Springflut nur würfeln, wenn sie etwas ändert (Höhle schon
        // zugänglich) und der Spieler nicht gerade drinsteht.
        val flutAktiv = state.activeEvents.any { it.typ == ActiveEvent.EVENT_SPRINGFLUT }
        if (!flutAktiv && state.has(FlagId.HOEHLE_ZUGANG) &&
            state.locationId != LocationId.HOEHLE && rnd.nextFloat() < SPRINGFLUT_CHANCE
        ) {
            state.activeEvents.add(
                ActiveEvent(ActiveEvent.EVENT_SPRINGFLUT, SPRINGFLUT_DAUER, LocationId.HOEHLE),
            )
            return "Eine Springflut rollt heran! Der Höhlenschlund gurgelt und schluckt Wasser — vorerst kommt da niemand hinein."
        }

        return null
    }
}
