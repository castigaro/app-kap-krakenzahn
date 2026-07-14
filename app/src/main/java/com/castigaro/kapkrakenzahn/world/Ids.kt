package com.castigaro.kapkrakenzahn.world

/** Die acht Schauplätze des Kaps. */
enum class LocationId {
    HAFEN, TAVERNE, MARKT, STRAND, DSCHUNGEL, KLIPPEN, HOEHLE, LEUCHTTURM
}

/** Die vier klassischen Adventure-Verben. */
enum class VerbId {
    SCHAU, NIMM, BENUTZE, REDE
}

/** Die Figuren des Spiels. */
enum class NpcId {
    GRIET,    // Wirtin der Taverne "Zum Ersoffenen Kraken"
    RASMUS,   // Käpt'n Rasmus Rotbart, versoffener Ex-Pirat und Duell-Meister
    YOLA,     // Marktfrau mit Fischstand
    BO,       // Trödelschmied
    MARA,     // Einsiedlerin am Strand
    KRAECHZ,  // Papagei im Hafen
    ANSGAR,   // Der verschwundene Leuchtturmwärter (Wächter in der Höhle)
    FIELO,    // Fahrender Händler (Zufallsereignis)
}

/** Alle tragbaren Gegenstände. */
enum class ItemId {
    MEDAILLON, SPIEGEL, SANDDORNPUNSCH, FEUERPFEFFER, SPEZIALPUNSCH,
    MACHETE, STINKFISCH, LATERNE, TRANOEL, BRENNENDE_LATERNE,
    EIMER, QUALLENEIMER, STOCK, TEER, TEERFACKEL, FACKEL, FEUERSTEIN,
    SEIL, ENTERHAKEN, WURFANKER, GEZEITENAMULETT, MUSCHELHORN,
    DIETRICH, SCHLUESSEL, ERSATZSCHLUESSEL, LAPPEN, FASS, KRAKENAUGE
}

/** Die sechs Rätsel des Spiels. */
enum class PuzzleId {
    ROTBARTS_GEHEIMNIS,   // Rasmus verrät den Weg zur Höhle
    DER_DORNENPFAD,       // Der Dschungelpfad ist versperrt
    DIE_FLUTHOEHLE,       // Der Höhleneingang liegt bei Flut unter Wasser
    LICHT_INS_DUNKEL,     // Die Höhle ist stockfinster
    DES_WAECHTERS_RAETSEL,// Der Wächter gibt das Krakenauge nur frei ...
    DAS_NEUE_FEUER,       // Leuchtturm öffnen und das Feuer entzünden
}

/** Spielfortschritts-Schalter. Lösungsweg-Flags markieren gelöste Pfade. */
enum class FlagId {
    // Gefälligkeiten
    GRIET_AUFTRAG, GRIET_GEFALLEN, BO_AUFTRAG, KRAN_ENTWIRRT, BO_GEFALLEN,
    // Rätsel 1: Rotbarts Geheimnis
    DUELL_GEWONNEN, SPEZIALPUNSCH_SERVIERT, MEDAILLON_ZURUECKGEGEBEN,
    WEG_ZUR_HOEHLE_BEKANNT,
    // Rätsel 2: Der Dornenpfad
    DORNEN_ZERSCHNITTEN, ZIEGE_WEGGELOCKT, SEITENPFAD_ENTDECKT,
    // Rätsel 3: Die Fluthöhle
    BOOT_GENUTZT, FLUT_GEBAENDIGT, ABGESEILT, HOEHLE_ZUGANG,
    // Rätsel 4: Licht ins Dunkel (gesetzt beim Bau der Lichtquelle)
    LICHT_LATERNE, LICHT_QUALLE, LICHT_FACKEL,
    // Rätsel 5: Des Wächters Rätsel
    WAECHTER_RAETSEL_GELOEST, KRAKE_BESAENFTIGT, OPFER_GEBRACHT,
    KRAKENAUGE_ERHALTEN,
    // Rätsel 6: Das neue Feuer
    TUER_MIT_SCHLUESSEL, TUER_MIT_DIETRICH, TUER_MIT_ERSATZSCHLUESSEL,
    LEUCHTTURM_OFFEN, AUGE_EINGESETZT, SPIEGEL_POLIERT, FEUER_ENTZUENDET,
}

/**
 * Wissens-Hinweise, die NPCs im Gespräch enthüllen können. Welche Figur
 * welchen Hinweis kennt, verteilt [com.castigaro.kapkrakenzahn.game.Variation]
 * pro Spieldurchlauf neu.
 */
enum class HintId {
    HINT_SEITENPFAD,       // Mara: verborgener Seitenpfad vom Strand in den Dschungel
    HINT_ERSATZSCHLUESSEL, // Krächz: wo Ansgars Ersatzschlüssel versteckt liegt
    HINT_PUNSCH_REZEPT,      // Rasmus' Spezial-Punsch: Sanddornpunsch + Feuerpfeffer
    HINT_MEDAILLON_ORT,    // wo Rasmus' Medaillon verloren ging
    HINT_QUALLEN_LICHT,    // Leuchtquallen im Gezeitentümpel spenden Licht
    HINT_KRAKEN_MUSIK,     // Muschelhorn-Musik besänftigt den Kraken
    HINT_GEZEITEN_AMULETT, // Mara tauscht ihr Gezeiten-Amulett gegen Glitzerkram
    HINT_RAETSEL_A,        // Antwort auf die erste Wächter-Frage
    HINT_RAETSEL_B,        // Antwort auf die zweite Wächter-Frage
    HINT_RAETSEL_C,        // Antwort auf die dritte Wächter-Frage
}
