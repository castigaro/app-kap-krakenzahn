package com.appsonar.kapkrakenzahn.world

import com.appsonar.kapkrakenzahn.data.GameState

/** Achsenparalleles Rechteck in normierten Szenen-Koordinaten (0..1). */
data class RectFN(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun contains(x: Float, y: Float): Boolean =
        x in left..right && y in top..bottom

    val centerX: Float get() = (left + right) / 2f
}

/**
 * Ein Ausgang zu einem Nachbarort. [xNorm] ist die Position des Pfeils in
 * der Szene; eine unerfüllte [condition] zeigt beim Antippen [lockedText].
 */
class Exit(
    val target: LocationId,
    val xNorm: Float,
    val label: String,
    /** Unsichtbare Ausgänge (Geheimpfade) erscheinen erst, wenn [visible] gilt. */
    val visible: (GameState) -> Boolean = { true },
    val condition: (GameState) -> Boolean = { true },
    val lockedText: String? = null,
)

/**
 * Ein antippbarer Bereich der Szene. [anchorX] ist die Stelle, zu der die
 * Heldin hinläuft, bevor die Verben angeboten werden. Hotspots mit
 * unerfüllter [condition] existieren für den Spieler nicht (Variation,
 * Fortschritt, Ereignisse).
 */
class Hotspot(
    val id: String,
    val name: String,
    val beschreibung: String,
    val rect: RectFN,
    val anchorX: Float = rect.centerX,
    /**
     * Optionale Tiefe (y) der Anlaufstelle. Ist sie null, leitet die [SceneView]
     * den Wert aus der Hotspot-Lage ab (Unterkante in die Lauf-Bande geklemmt).
     */
    val anchorY: Float? = null,
    val verbs: Set<VerbId> = setOf(VerbId.SCHAU),
    val npc: NpcId? = null,
    val condition: (GameState) -> Boolean = { true },
)

/**
 * Ein Schauplatz mit seiner **begehbaren Boden-Bande**: [walkY] ist die
 * vordere Lauflinie (nah, volle Figurgröße), [walkFarY] die hintere (fern,
 * kleiner). Die Heldin darf sich frei zwischen beiden bewegen; ihre Größe
 * skaliert mit der Tiefe.
 */
class Location(
    val id: LocationId,
    val name: String,
    val exits: List<Exit>,
    val hotspots: List<Hotspot>,
    val walkY: Float = 0.86f,
    val walkFarY: Float = walkY - 0.12f,
)

/** Auswirkung einer Spielhandlung oder eines Dialog-Zugeständnisses. */
sealed class Effect {
    data class AddItem(val item: ItemId) : Effect()
    data class RemoveItem(val item: ItemId) : Effect()
    data class SetFlag(val flag: FlagId) : Effect()
    data class RevealHint(val hint: HintId) : Effect()
    data class MoveTo(val target: LocationId) : Effect()
    data class StartDialog(val npc: NpcId) : Effect()
    data class StartDuel(val npc: NpcId) : Effect()
    object StartRiddle : Effect()
    data class PlaySfx(val sound: String) : Effect()
    object EndGame : Effect()
}

/**
 * Eine Spielregel: Verb auf Hotspot (optional mit benutztem Gegenstand).
 * Die erste passende Regel mit erfüllter Bedingung gewinnt.
 */
class Rule(
    val location: LocationId,
    val hotspotId: String,
    val verb: VerbId,
    val usedItem: ItemId? = null,
    val condition: (GameState) -> Boolean = { true },
    val text: String,
    val effects: List<Effect> = emptyList(),
)

/** Kombination zweier Inventar-Gegenstände („Benutze X mit Y"). */
class Combo(
    val a: ItemId,
    val b: ItemId,
    val text: String,
    val effects: List<Effect>,
)

/**
 * Ein Zugeständnis, das die KI im Dialog machen DARF. Nur Effekte aus
 * dieser Whitelist (mit erfüllter Bedingung) werden dem Modell im Prompt
 * angeboten und beim Parsen akzeptiert — die KI kann Hinweise enthüllen
 * und freigeschaltete Übergaben auslösen, aber die Spiellogik nie
 * korrumpieren.
 */
class AllowedEffect(
    val typ: String, // TYP_HINWEIS, TYP_FLAG, TYP_ITEM, TYP_ITEMWEG, TYP_DUELL, TYP_RAETSEL
    val id: String,
    val beschreibung: String,
    /** Spieler-Frage, über die der geskriptete Ersatz-Dialog dieses Zugeständnis anbietet. */
    val frage: String,
    /** Antworttext des NPC im Ersatz-Dialog. */
    val antwort: String,
    val condition: (GameState) -> Boolean = { true },
) {
    companion object {
        const val TYP_HINWEIS = "hinweis"
        const val TYP_FLAG = "flag"
        const val TYP_ITEM = "item"
        const val TYP_ITEMWEG = "itemweg"
        const val TYP_DUELL = "duell"
        const val TYP_RAETSEL = "raetsel"
    }
}

/**
 * Ein Zug des geskripteten Ersatz-Dialogs (ohne API-Key / bei API-Fehlern).
 * Nach jeder gewählten Option wird der Knoten aus dem aktuellen Spielstand
 * neu aufgebaut — so bleiben die Optionen immer konsistent zum Fortschritt.
 */
class FallbackNode(
    val text: String,
    val options: List<FallbackOption>,
)

class FallbackOption(
    val label: String,
    val antwort: String,
    val effects: List<Effect> = emptyList(),
    val beendet: Boolean = false,
)

/** Eine Spielfigur samt Charakterkarte für den LLM-Prompt. */
class Npc(
    val id: NpcId,
    val name: String,
    val ort: LocationId?,
    val begruessung: String,
    val charakterkarte: String,
    val voicePitch: Float = 1f,
    val voiceRate: Float = 1f,
    val allowedEffects: List<AllowedEffect> = emptyList(),
)

/** Ein tragbarer Gegenstand. */
class Item(
    val id: ItemId,
    val name: String,
    val beschreibung: String,
)

/** Ein Rätsel mit seinen alternativen Lösungswegen. */
class SolutionPath(
    val id: String,
    val beschreibung: String,
    val solvedFlag: FlagId,
)

class Puzzle(
    val id: PuzzleId,
    val titel: String,
    val beschreibung: String,
    val paths: List<SolutionPath>,
)
