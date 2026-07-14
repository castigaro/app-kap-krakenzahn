package com.castigaro.kapkrakenzahn.game

import com.castigaro.kapkrakenzahn.api.DuelApi

/**
 * Führt Buch über das „Duell der spitzen Zungen": Best-of-five, drei
 * gewonnene Runden bedeuten Sieg für die Heldin, drei verlorene Sieg für
 * Rasmus. Rein logisch, ohne UI und ohne Netzwerk — die Runden liefert die
 * [DuelApi] (bzw. der Fallback), die Auswertung passiert hier.
 */
class DuelController {

    var spielerPunkte = 0
        private set
    var gegnerPunkte = 0
        private set

    /** Bisheriger Verlauf als Text, den die KI als Kontext bekommt. */
    val verlauf = mutableListOf<String>()

    val runde: Int get() = spielerPunkte + gegnerPunkte + 1

    val vorbei: Boolean get() = spielerPunkte >= SIEGE || gegnerPunkte >= SIEGE
    val spielerGewinnt: Boolean get() = spielerPunkte >= SIEGE

    /**
     * Wertet die Wahl des Spielers gegen die aktuelle Runde aus, aktualisiert
     * den Punktestand und protokolliert den Schlagabtausch. Liefert true, wenn
     * der Konter saß.
     */
    fun bewerte(round: DuelApi.DuelRound, gewaehlterIndex: Int): Boolean {
        val richtig = gewaehlterIndex == round.richtig
        if (richtig) spielerPunkte++ else gegnerPunkte++
        val konter = round.kontern.getOrElse(gewaehlterIndex) { "…" }
        verlauf.add("Rasmus: ${round.beleidigung}")
        verlauf.add("Jonna: $konter")
        return richtig
    }

    companion object {
        const val SIEGE = 3
    }
}
