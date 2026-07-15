package com.castigaro.kapkrakenzahn.game

import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.world.Effect
import com.castigaro.kapkrakenzahn.world.FlagId
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.Hotspot
import com.castigaro.kapkrakenzahn.world.ItemId
import com.castigaro.kapkrakenzahn.world.LocationId
import com.castigaro.kapkrakenzahn.world.NpcId
import com.castigaro.kapkrakenzahn.world.VerbId

/** Ergebnis einer Spielhandlung — Text plus auszulösende UI-Aktionen. */
class ActionResult(
    val text: String,
    val moveTo: LocationId? = null,
    val startDialog: NpcId? = null,
    val startDuel: NpcId? = null,
    val startRiddle: Boolean = false,
    val sfx: List<String> = emptyList(),
    val endGame: Boolean = false,
    val stateChanged: Boolean = false,
)

/**
 * Der deterministische Kern des Spiels: wendet Verben, Gegenstände und
 * Kombinationen auf den Spielstand an. Die erste passende Regel gewinnt;
 * ohne Regel gibt es einen situativen Standardtext.
 */
object GameEngine {

    /** Führt Verb (+ optional benutzten Gegenstand) auf einem Hotspot aus. */
    fun performAction(
        state: GameState,
        verb: VerbId,
        hotspotId: String,
        usedItem: ItemId? = null,
    ): ActionResult {
        val hotspot = GameWorld.hotspotsFor(state, state.locationId)
            .firstOrNull { it.id == hotspotId }
            ?: return ActionResult("Da ist nichts mehr.")

        val rule = GameWorld.rules.firstOrNull {
            it.location == state.locationId && it.hotspotId == hotspotId &&
                it.verb == verb && it.usedItem == usedItem && it.condition(state)
        }
        if (rule != null) {
            return applyEffects(state, rule.effects, rule.text)
        }

        // REDE auf einer Figur startet immer das Gespräch.
        if (verb == VerbId.REDE && hotspot.npc != null) {
            return ActionResult(text = "", startDialog = hotspot.npc)
        }

        return ActionResult(defaultText(verb, usedItem, hotspot.beschreibung))
    }

    private fun defaultText(verb: VerbId, usedItem: ItemId?, beschreibung: String): String =
        when (verb) {
            VerbId.SCHAU -> beschreibung
            VerbId.NIMM -> "Das bleibt besser, wo es ist."
            VerbId.REDE -> "Du redest. Es schweigt. Klassische Rollenverteilung."
            VerbId.BENUTZE ->
                if (usedItem != null)
                    "${GameWorld.itemName(usedItem)} und das hier? Das ergibt beim besten Willen keinen Sinn."
                else
                    "Du drückst, ziehst und rüttelst. Nichts passiert."
        }

    /** Kombiniert zwei Inventar-Gegenstände („Benutze X mit Y"). */
    fun combine(state: GameState, a: ItemId, b: ItemId): ActionResult {
        val combo = GameWorld.combos.firstOrNull {
            (it.a == a && it.b == b) || (it.a == b && it.b == a)
        } ?: return ActionResult(
            "${GameWorld.itemName(a)} und ${GameWorld.itemName(b)}? Nette Idee, aber nein.",
        )
        return applyEffects(state, combo.effects, combo.text)
    }

    /**
     * Kombiniert 2–3 Inventar-Gegenstände (Werkbank). Bei dreien werden
     * Paar-Kombos verkettet: Das Zwischenergebnis wandert zurück in den
     * Pool, sodass z. B. Stock+Teer+Feuerstein in einem Zug die brennende
     * Fackel ergeben. Der Pool wird aus den Kombo-Effekten fortgeschrieben,
     * denn manche Kombos verbrauchen nur einen der beiden Teile (der
     * Feuerstein überlebt das Entzünden). Kein Treffer → Fehltext ohne
     * Zustandsänderung; ein Teiltreffer zählt als Erfolg.
     */
    fun combineAll(state: GameState, items: List<ItemId>): ActionResult {
        if (items.size == 2) return combine(state, items[0], items[1])

        val pool = items.toMutableList()
        val texts = mutableListOf<String>()
        val sfx = mutableListOf<String>()
        var moveTo: LocationId? = null
        var startDialog: NpcId? = null
        var startDuel: NpcId? = null
        var startRiddle = false
        var endGame = false
        var rounds = 0

        while (rounds < GameWorld.combos.size + items.size) {
            val combo = GameWorld.combos.firstOrNull { it.a in pool && it.b in pool } ?: break
            val result = applyEffects(state, combo.effects, combo.text)
            combo.effects.forEach { effect ->
                when (effect) {
                    is Effect.RemoveItem -> pool.remove(effect.item)
                    is Effect.AddItem -> pool.add(effect.item)
                    else -> Unit
                }
            }
            texts.add(result.text)
            sfx.addAll(result.sfx)
            result.moveTo?.let { moveTo = it }
            result.startDialog?.let { startDialog = it }
            result.startDuel?.let { startDuel = it }
            startRiddle = startRiddle || result.startRiddle
            endGame = endGame || result.endGame
            rounds += 1
        }

        if (texts.isEmpty()) {
            val namen = items.dropLast(1).joinToString(", ") { GameWorld.itemName(it) } +
                " und " + GameWorld.itemName(items.last())
            return ActionResult("$namen? Nette Idee, aber nein.")
        }
        return ActionResult(
            text = texts.joinToString(" "),
            moveTo = moveTo,
            startDialog = startDialog,
            startDuel = startDuel,
            startRiddle = startRiddle,
            sfx = sfx,
            endGame = endGame,
            stateChanged = true,
        )
    }

    /**
     * Standard-Verb eines Hotspots für den Doppeltipp: Figuren wird
     * zugehört, Nehmbares genommen, alles andere angeschaut.
     */
    fun defaultVerb(hotspot: Hotspot): VerbId = when {
        hotspot.npc != null -> VerbId.REDE
        VerbId.NIMM in hotspot.verbs -> VerbId.NIMM
        else -> VerbId.SCHAU
    }

    /**
     * Wendet eine Effektliste auf den Spielstand an und sammelt die
     * UI-Aktionen ein. Wird auch vom Dialogsystem (Whitelist-Effekte),
     * Duell und Rätsel genutzt.
     */
    fun applyEffects(state: GameState, effects: List<Effect>, text: String): ActionResult {
        var moveTo: LocationId? = null
        var startDialog: NpcId? = null
        var startDuel: NpcId? = null
        var startRiddle = false
        var endGame = false
        val sfx = mutableListOf<String>()

        effects.forEach { effect ->
            when (effect) {
                is Effect.AddItem -> state.addItem(effect.item)
                is Effect.RemoveItem -> state.removeItem(effect.item)
                is Effect.SetFlag -> state.flags.add(effect.flag)
                is Effect.RevealHint -> state.revealedHints.add(effect.hint)
                is Effect.MoveTo -> moveTo = effect.target
                is Effect.StartDialog -> startDialog = effect.npc
                is Effect.StartDuel -> startDuel = effect.npc
                Effect.StartRiddle -> startRiddle = true
                is Effect.PlaySfx -> sfx.add(effect.sound)
                Effect.EndGame -> {
                    state.finished = true
                    endGame = true
                }
            }
        }
        return ActionResult(
            text = text,
            moveTo = moveTo,
            startDialog = startDialog,
            startDuel = startDuel,
            startRiddle = startRiddle,
            sfx = sfx,
            endGame = endGame,
            stateChanged = effects.isNotEmpty(),
        )
    }

    /**
     * Ortswechsel: zählt den Schritt, lässt Ereignisse ticken und würfelt
     * neue an. Liefert den Ereignistext (falls etwas passiert) zurück.
     */
    fun changeLocation(state: GameState, target: LocationId): String? {
        val kamVonRechts = GameWorld.location(state.locationId).exits
            .firstOrNull { it.target == target }?.let { it.xNorm > 0.5f } ?: true
        state.locationId = target
        state.heroX = if (kamVonRechts) 0.08f else 0.92f
        state.heroY = GameWorld.location(target).walkY
        state.stepCount += 1
        return EventDirector.onLocationChange(state)
    }

    /** Effekte des bestandenen Wächter-Rätsels. */
    fun solveRiddle(state: GameState): ActionResult = applyEffects(
        state,
        listOf(
            Effect.SetFlag(FlagId.WAECHTER_RAETSEL_GELOEST),
            Effect.SetFlag(FlagId.KRAKENAUGE_ERHALTEN),
            Effect.AddItem(ItemId.KRAKENAUGE),
            Effect.PlaySfx("erfolg"),
        ),
        "Ansgar verneigt sich. 'Drei Fragen, drei Wahrheiten. Du bist würdig.' Die Tentakel des Wächtersteins geben das Krakenauge frei — es ist warm wie ein Herzschlag.",
    )

    /** Effekte des gewonnenen Duells der spitzen Zungen. */
    fun winDuel(state: GameState): ActionResult = applyEffects(
        state,
        listOf(
            Effect.SetFlag(FlagId.DUELL_GEWONNEN),
            Effect.SetFlag(FlagId.WEG_ZUR_HOEHLE_BEKANNT),
            Effect.PlaySfx("erfolg"),
        ),
        "Rasmus starrt dich an — dann bricht er in donnerndes Gelächter aus. 'Geschlagen! Mit MEINEN Waffen!' Er verneigt sich. 'Der Weg zur Höhle: hinter dem Möwenfelsen am Klippenpfad beginnt der alte Wärterabstieg.'",
    )
}
