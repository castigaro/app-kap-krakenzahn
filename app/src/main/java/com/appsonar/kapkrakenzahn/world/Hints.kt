package com.appsonar.kapkrakenzahn.world

import com.appsonar.kapkrakenzahn.game.Variation

/**
 * Die drei Fragensets des Wächters. Welches Set gilt, entscheidet die
 * [Variation] des Durchlaufs; die Antworten stecken in den
 * HINT_RAETSEL-Hinweisen, die bei den Dorfbewohnern zu erfragen sind.
 */
object Riddles {

    class Question(
        val frage: String,
        val antwort: String,
        val koeder: List<String>,
        val hint: HintId,
    )

    val SETS: List<List<Question>> = listOf(
        listOf(
            Question(
                frage = "Was gab der Krake dem ersten Wärter zum Pfand ihrer Freundschaft?",
                antwort = "Sein eigenes Auge",
                koeder = listOf("Eine schwarze Perle", "Seinen größten Zahn"),
                hint = HintId.HINT_RAETSEL_A,
            ),
            Question(
                frage = "Wie viele Stufen zählte Ansgar auf dem Weg zum Feuer?",
                antwort = "Einhundertelf",
                koeder = listOf("Neunundneunzig", "Zweihundertzwei"),
                hint = HintId.HINT_RAETSEL_B,
            ),
            Question(
                frage = "Welches Lied sang der Wärter dem Kraken in stürmischen Nächten?",
                antwort = "Das Lied der Gezeiten",
                koeder = listOf("Das Klabautermann-Gejohle", "Die Ballade vom trockenen Dock"),
                hint = HintId.HINT_RAETSEL_C,
            ),
        ),
        listOf(
            Question(
                frage = "Aus welchem Holz ist die Tür des Leuchtturms geschnitzt?",
                antwort = "Aus dem Wrack der Silbermöwe",
                koeder = listOf("Aus Dschungel-Eiche", "Aus altem Fassholz"),
                hint = HintId.HINT_RAETSEL_A,
            ),
            Question(
                frage = "Was versprach der Wärter dem Kraken für jedes Jahr?",
                antwort = "Eine Nacht der Dunkelheit",
                koeder = listOf("Ein Fass voll Punsch", "Einen Korb Stinkfisch"),
                hint = HintId.HINT_RAETSEL_B,
            ),
            Question(
                frage = "Nach welchem Stern richten sich die Kraken der Tiefe?",
                antwort = "Nach dem Tintenstern",
                koeder = listOf("Nach dem Polarstern", "Nach dem Möwenstern"),
                hint = HintId.HINT_RAETSEL_C,
            ),
        ),
        listOf(
            Question(
                frage = "Wer schmiedete einst die Fassung des Leuchtfeuers?",
                antwort = "Die Schmiedin Ilva",
                koeder = listOf("Der Trödelschmied Bo", "Ein namenloser Maat"),
                hint = HintId.HINT_RAETSEL_A,
            ),
            Question(
                frage = "Was fürchtet der Krake mehr als jeden Sturm?",
                antwort = "Die Stille",
                koeder = listOf("Das Tageslicht", "Heißen Feuerpfeffer"),
                hint = HintId.HINT_RAETSEL_B,
            ),
            Question(
                frage = "Womit besiegelten Wärter und Krake ihren alten Bund?",
                antwort = "Mit einem Tropfen Leuchtöl",
                koeder = listOf("Mit einem Handschlag", "Mit einem geteilten Punsch"),
                hint = HintId.HINT_RAETSEL_C,
            ),
        ),
    )

    fun questions(variation: Variation): List<Question> = SETS[variation.raetselSet]
}

/**
 * Klartext der Wissens-Hinweise — das ist das Faktum, das ein NPC kennt
 * und im Gespräch (in eigenen Worten) enthüllen kann. Einige Texte hängen
 * von der [Variation] ab.
 */
object Hints {

    fun text(hint: HintId, variation: Variation): String = when (hint) {
        HintId.HINT_SEITENPFAD ->
            "Hinter dem Felsspalt am Strand beginnt ein verborgener Seitenpfad in den " +
                "Dschungel — man erkennt ihn nur mit einer Lichtquelle in der Hand."
        HintId.HINT_ERSATZSCHLUESSEL ->
            "Ansgar versteckte einen Ersatzschlüssel für den Leuchtturm unter dem " +
                "losen Stein direkt neben der Turmtür."
        HintId.HINT_PUNSCH_REZEPT ->
            "Rasmus' geliebter Spezial-Punsch ist gewöhnlicher Sanddornpunsch, aufgepeppt mit einer " +
                "ordentlichen Prise Feuerpfeffer vom Markt."
        HintId.HINT_MEDAILLON_ORT ->
            if (variation.medaillonAmStrand)
                "Rasmus hat sein Medaillon beim letzten Landgang verloren — es müsste " +
                    "im Treibgut am Strand stecken."
            else
                "Rasmus hat sein Medaillon beim letzten Landgang verloren — es müsste " +
                    "im Gebüsch am Dschungelpfad hängen."
        HintId.HINT_QUALLEN_LICHT ->
            "Im Gezeitentümpel am Strand leben Leuchtquallen. In einem Eimer Wasser " +
                "leuchten sie heller als jede Laterne."
        HintId.HINT_KRAKEN_MUSIK ->
            "Der Krake liebt Musik. Wer vor seiner Höhle das Muschelhorn bläst, " +
                "besänftigt ihn augenblicklich."
        HintId.HINT_GEZEITEN_AMULETT ->
            "Die Einsiedlerin Mara besitzt ein Gezeiten-Amulett, das die Flut " +
                "zurückhält. Sie tauscht es nur gegen Glitzerkram."
        HintId.HINT_RAETSEL_A -> raetselWissen(variation, 0)
        HintId.HINT_RAETSEL_B -> raetselWissen(variation, 1)
        HintId.HINT_RAETSEL_C -> raetselWissen(variation, 2)
    }

    private fun raetselWissen(variation: Variation, index: Int): String {
        val q = Riddles.questions(variation)[index]
        return "Altes Kap-Wissen: ${q.frage} — ${q.antwort}."
    }

    /** Kurzthema für Spieler-Fragen („Frag nach …"). */
    fun topic(hint: HintId): String = when (hint) {
        HintId.HINT_SEITENPFAD -> "einem verborgenen Weg in den Dschungel"
        HintId.HINT_ERSATZSCHLUESSEL -> "einem Ersatzschlüssel für den Leuchtturm"
        HintId.HINT_PUNSCH_REZEPT -> "Rasmus' Lieblingsgetränk"
        HintId.HINT_MEDAILLON_ORT -> "Rasmus' verlorenem Medaillon"
        HintId.HINT_QUALLEN_LICHT -> "Licht für dunkle Orte"
        HintId.HINT_KRAKEN_MUSIK -> "der Schwäche des Kraken"
        HintId.HINT_GEZEITEN_AMULETT -> "einem Mittel gegen die Flut"
        HintId.HINT_RAETSEL_A -> "alten Kap-Legenden (Teil 1)"
        HintId.HINT_RAETSEL_B -> "alten Kap-Legenden (Teil 2)"
        HintId.HINT_RAETSEL_C -> "alten Kap-Legenden (Teil 3)"
    }
}
