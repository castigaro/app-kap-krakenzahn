package com.appsonar.kapkrakenzahn.world

import com.appsonar.kapkrakenzahn.api.DuelApi
import kotlin.random.Random

/**
 * Fest verdrahtete Beleidigungs-/Konter-Paare für das Duell der spitzen
 * Zungen ohne API-Key (oder bei API-Fehlern). Immer der erste Konter
 * trifft; die Reihenfolge wird pro Runde durchgemischt, damit die richtige
 * Antwort nicht vorhersehbar an Position 0 steht.
 */
object FallbackDuel {

    private data class Paar(val beleidigung: String, val treffer: String)

    private val PAARE = listOf(
        Paar(
            "Du kämpfst ja mit Worten wie eine seekranke Landratte!",
            "Und du stinkst so nach Fisch, dass selbst die Möwen kehrtmachen.",
        ),
        Paar(
            "Ich habe Kombüsenschaben gesehen, die mehr Mumm hatten als du!",
            "Kein Wunder — die haben ja auch von dir gelernt, wie man wegläuft.",
        ),
        Paar(
            "Deine Zunge ist so stumpf wie ein rostiger Ankerhaken!",
            "Deine dafür so lang, dass du dauernd drüber stolperst.",
        ),
        Paar(
            "Nach dieser Runde weinst du nach deiner Mama!",
            "Sie wohnt gleich neben deinem letzten bisschen Würde — also gar nicht weit.",
        ),
        Paar(
            "Ich bin der gefürchtetste Pirat der sieben Pfützen!",
            "Pfützen, ja — tiefer bist du mit deinem Kahn nie gekommen.",
        ),
        Paar(
            "Dein Punsch schmeckt wie Bilgenwasser!",
            "Immerhin trinke ich ihn nicht literweise wie du, du wandelndes Fass.",
        ),
        Paar(
            "Ich schneide dich in Stücke, so klein wie Ködergarnelen!",
            "Mit welchem Messer? Dein letztes hast du im Duell verloren — gegen einen Hummer.",
        ),
        Paar(
            "Niemand hat je mein Wortgefecht überlebt!",
            "Kein Wunder, alle sind vor Langeweile eingeschlafen.",
        ),
    )

    /** Baut aus einem Paar eine Runde mit gemischter Konter-Reihenfolge. */
    fun round(runde: Int, seed: Long): DuelApi.DuelRound {
        val rnd = Random(seed * 131 + runde)
        val paar = PAARE[rnd.nextInt(PAARE.size)]
        val fehler = FEHLKONTER.shuffled(rnd).take(2)
        val optionen = (listOf(paar.treffer) + fehler).shuffled(rnd)
        return DuelApi.DuelRound(
            beleidigung = paar.beleidigung,
            kontern = optionen,
            richtig = optionen.indexOf(paar.treffer),
        )
    }

    private val FEHLKONTER = listOf(
        "Ähm... selber doof!",
        "Das nimmst du sofort zurück, sonst... äh... sonst was!",
        "Meine Großmutter kontert besser als ich, aber egal!",
        "Ich habe gerade keine gute Antwort parat.",
        "Pass bloß auf, sonst hole ich meinen großen Bruder!",
        "Warte, das ergibt gar keinen Sinn.",
    )
}
