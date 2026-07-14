package com.castigaro.kapkrakenzahn.world

import com.castigaro.kapkrakenzahn.data.GameState

/**
 * Geskriptete Ersatz-Dialoge für den Betrieb ohne API-Key (oder wenn die
 * KI nicht erreichbar ist). Sie werden generisch aus derselben
 * Effekt-Whitelist gebaut wie die LLM-Prompts — damit ist garantiert,
 * dass jedes nötige Zugeständnis (Hinweis, Gegenstand, Duell, Rätsel)
 * auch offline erreichbar bleibt und das Spiel lösbar ist.
 */
object FallbackDialogs {

    const val ABSCHIED_LABEL = "Tschüss."

    /** Baut den Gesprächsknoten für die Begrüßung bzw. nach einer Antwort. */
    fun build(npcId: NpcId, state: GameState, text: String? = null): FallbackNode {
        val npc = GameWorld.npc(npcId)
        val options = GameWorld.allowedEffectsFor(state, npcId)
            // ITEMWEG-Einträge sind nur Bausteine für die KI-Tauschlogik;
            // im Ersatz-Dialog übernimmt enforceAmulettTausch den Tausch.
            .filter { it.typ != AllowedEffect.TYP_ITEMWEG }
            .map { allowed ->
                FallbackOption(
                    label = allowed.frage,
                    antwort = allowed.antwort,
                    effects = GameWorld.enforceAmulettTausch(
                        state, GameWorld.toEffects(state, npcId, allowed),
                    ),
                )
            }
        val smalltalk = FallbackOption(
            label = smalltalkFrage(npcId),
            antwort = smalltalkAntwort(npcId),
        )
        val abschied = FallbackOption(
            label = ABSCHIED_LABEL,
            antwort = abschiedAntwort(npcId),
            beendet = true,
        )
        return FallbackNode(
            text = text ?: npc.begruessung,
            options = options + smalltalk + abschied,
        )
    }

    private fun smalltalkFrage(npc: NpcId): String = when (npc) {
        NpcId.GRIET -> "Wie läuft das Geschäft?"
        NpcId.RASMUS -> "Erzähl mir von deinen Heldentaten!"
        NpcId.YOLA -> "Was gibt es Neues am Kap?"
        NpcId.BO -> "Woran arbeitest du gerade?"
        NpcId.MARA -> "Was sagt das Meer heute?"
        NpcId.KRAECHZ -> "Polly will 'nen Keks?"
        NpcId.ANSGAR -> "Was ist damals geschehen?"
        NpcId.FIELO -> "Wo kommst du eigentlich her?"
    }

    private fun smalltalkAntwort(npc: NpcId): String = when (npc) {
        NpcId.GRIET -> "Seit das Leuchtfeuer aus ist, kommt kein Schiff mehr — und ohne Schiffe keine durstigen Kehlen. Wenn das so weitergeht, kann ich zumachen."
        NpcId.RASMUS -> "Ich habe dem Kraken persönlich ins Auge gesehen! Ins echte! Aber das ist eine Geschichte für Leute, die mich vorher beeindrucken."
        NpcId.YOLA -> "Der alte Ansgar ist verschwunden, das Feuer ist aus, und die Ziege am Dornenpfad frisst neuerdings Hüte. Sonst? Alles wie immer."
        NpcId.BO -> "An einem Anker. Oder einer Wetterfahne. Das entscheidet das Eisen, nicht ich."
        NpcId.MARA -> "Das Meer sagt: Die Flut nimmt nur, was man ihr freiwillig gibt. Denk darüber nach, Kind."
        NpcId.KRAECHZ -> "KRAAAH! Keks! Keks! ... Kein Keks? Landratte GEIZIG!"
        NpcId.ANSGAR -> "Ich brach mein Versprechen an den Kraken — eine Nacht im Jahr gehört ihm, dunkel und still. Ich ließ das Feuer brennen... und er holte sich sein Auge zurück. Und mich dazu."
        NpcId.FIELO -> "Von überall und nirgends! Fielo folgt den Geschichten — und am Kap Krakenzahn riecht es gerade nach einer GROSSEN."
    }

    private fun abschiedAntwort(npc: NpcId): String = when (npc) {
        NpcId.GRIET -> "Lauf nicht dem Kraken in die Arme, Deern."
        NpcId.RASMUS -> "Verschwinde, bevor ich sentimental werde. Hicks."
        NpcId.YOLA -> "Komm wieder, wenn du Fisch brauchst! Oder Klatsch!"
        NpcId.BO -> "Hm."
        NpcId.MARA -> "Die Ebbe nimmt dich mit. Wie ich sagte."
        NpcId.KRAECHZ -> "KRAAAH! Tschüss-tschüss! Tschüss-tschüss!"
        NpcId.ANSGAR -> "Geh mit dem Licht. Ich bleibe bei der Dunkelheit — noch."
        NpcId.FIELO -> "Empfehlen Sie Fielo weiter! Fielo empfiehlt sich!"
    }
}
