package com.castigaro.kapkrakenzahn.game

import com.castigaro.kapkrakenzahn.world.HintId
import com.castigaro.kapkrakenzahn.world.ItemId
import com.castigaro.kapkrakenzahn.world.NpcId
import kotlin.random.Random

/**
 * Die pro Spieldurchlauf gewürfelte Abwechslung. Wird NIE gespeichert —
 * nur der Seed liegt im Spielstand, aus dem sich die Variation jederzeit
 * deterministisch rekonstruieren lässt. Wichtig: die Ziehungen erfolgen in
 * fester Reihenfolge, damit derselbe Seed immer dieselbe Welt ergibt.
 */
class Variation(seed: Long) {

    private val rnd = Random(seed)

    /** Rasmus' Medaillon liegt am Strand-Treibgut oder im Dschungel-Gebüsch. */
    val medaillonAmStrand: Boolean = rnd.nextBoolean()

    /** Der Handspiegel steckt im Tavernen-Regal oder in Bos Trödelkiste. */
    val spiegelInTaverne: Boolean = rnd.nextBoolean()

    /** Den Feuerstein hat Bo — oder er liegt zwischen den Strand-Felsen. */
    val feuersteinBeiBo: Boolean = rnd.nextBoolean()

    /** Den Enterhaken hat Bo — oder er hängt im Wrack am Strand. */
    val enterhakenBeiBo: Boolean = rnd.nextBoolean()

    /** Das Muschelhorn liegt am Markt-Trödel oder im Strandsand. */
    val muschelhornAmMarkt: Boolean = rnd.nextBoolean()

    /** Welches der drei Fragensets der Wächter stellt (0..2). */
    val raetselSet: Int = rnd.nextInt(3)

    /** Themenfärbung für das Duell der spitzen Zungen. */
    val duellThema: String = DUELL_THEMEN[rnd.nextInt(DUELL_THEMEN.size)]

    /** Was der fahrende Händler Fielo verschenkt, falls er auftaucht. */
    val fieloStock: ItemId = FIELO_WAREN[rnd.nextInt(FIELO_WAREN.size)]

    /**
     * Wer welchen Wissens-Hinweis kennt. Fest verdrahtet sind nur
     * [HintId.HINT_SEITENPFAD] (Mara) und [HintId.HINT_ERSATZSCHLUESSEL]
     * (Krächz) — der Rest wird reihum auf die Dorfbewohner verteilt.
     */
    val hintOwners: Map<HintId, NpcId> = buildMap {
        put(HintId.HINT_SEITENPFAD, NpcId.MARA)
        put(HintId.HINT_ERSATZSCHLUESSEL, NpcId.KRAECHZ)
        val verteilbar = listOf(
            HintId.HINT_PUNSCH_REZEPT,
            HintId.HINT_MEDAILLON_ORT,
            HintId.HINT_QUALLEN_LICHT,
            HintId.HINT_KRAKEN_MUSIK,
            HintId.HINT_GEZEITEN_AMULETT,
            HintId.HINT_RAETSEL_A,
            HintId.HINT_RAETSEL_B,
            HintId.HINT_RAETSEL_C,
        )
        val owners = DORFBEWOHNER.shuffled(rnd)
        verteilbar.forEachIndexed { i, hint -> put(hint, owners[i % owners.size]) }
    }

    /** Krächz plappert zusätzlich einen zufälligen fremden Hinweis nach. */
    val kraechzPlappert: HintId = hintOwners.keys
        .filter { hintOwners[it] != NpcId.KRAECHZ }
        .let { it[rnd.nextInt(it.size)] }

    fun hintsOf(npc: NpcId): Set<HintId> {
        val eigene = hintOwners.filterValues { it == npc }.keys
        return if (npc == NpcId.KRAECHZ) eigene + kraechzPlappert else eigene
    }

    companion object {
        private val DORFBEWOHNER = listOf(
            NpcId.GRIET, NpcId.RASMUS, NpcId.YOLA, NpcId.BO, NpcId.MARA, NpcId.KRAECHZ,
        )

        val DUELL_THEMEN = listOf(
            "Seemannsgarn und Angeberei",
            "Kochkunst und Kombüsenschmach",
            "Seeungeheuer und Aberglaube",
            "Ruhm, Feigheit und peinliche Niederlagen",
        )

        val FIELO_WAREN = listOf(
            ItemId.FEUERPFEFFER, ItemId.LATERNE, ItemId.MUSCHELHORN,
            ItemId.SEIL, ItemId.LAPPEN,
        )
    }
}
