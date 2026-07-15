package com.castigaro.kapkrakenzahn.data

import com.castigaro.kapkrakenzahn.world.FlagId
import com.castigaro.kapkrakenzahn.world.HintId
import com.castigaro.kapkrakenzahn.world.ItemId
import com.castigaro.kapkrakenzahn.world.LocationId
import com.castigaro.kapkrakenzahn.world.NpcId
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Ein Beitrag im Gesprächsverlauf mit einer Spielfigur. */
class DialogMessage(
    val role: String, // ROLE_PLAYER oder ROLE_NPC
    val text: String,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("role", role)
        put("text", text)
    }

    companion object {
        const val ROLE_PLAYER = "player"
        const val ROLE_NPC = "npc"

        fun fromJson(json: JSONObject) = DialogMessage(
            role = json.optString("role", ROLE_PLAYER),
            text = json.optString("text"),
        )
    }
}

/** Ein laufendes Zufallsereignis (Sturm, Springflut, Händler Fielo). */
class ActiveEvent(
    val typ: String, // EVENT_FIELO, EVENT_STURM, EVENT_SPRINGFLUT
    var restSchritte: Int,
    val ort: LocationId,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("typ", typ)
        put("restSchritte", restSchritte)
        put("ort", ort.name)
    }

    companion object {
        const val EVENT_FIELO = "fielo"
        const val EVENT_STURM = "sturm"
        const val EVENT_SPRINGFLUT = "springflut"

        fun fromJson(json: JSONObject): ActiveEvent? {
            val ort = enumOrNull<LocationId>(json.optString("ort")) ?: return null
            return ActiveEvent(
                typ = json.optString("typ"),
                restSchritte = json.optInt("restSchritte", 0),
                ort = ort,
            )
        }
    }
}

/** Toleranter Enum-Parser: unbekannte Namen (ältere/neuere Stände) → null. */
inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    name?.let { n -> runCatching { enumValueOf<T>(n) }.getOrNull() }

/**
 * Der komplette veränderliche Spielstand einer Partie. Alles Unveränderliche
 * (Orte, Regeln, Figuren) liegt in [com.castigaro.kapkrakenzahn.world.GameWorld];
 * die pro Durchlauf gewürfelte Variation wird nur über [seed] festgehalten
 * und beim Laden deterministisch rekonstruiert.
 */
class GameState(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val seed: Long,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var playtimeMs: Long = 0L,
    var locationId: LocationId = LocationId.HAFEN,
    var heroX: Float = 0.5f,
    var heroY: Float = 0.86f,
    val inventory: MutableList<ItemId> = mutableListOf(),
    val flags: MutableSet<FlagId> = mutableSetOf(),
    val revealedHints: MutableSet<HintId> = mutableSetOf(),
    val npcHistories: MutableMap<NpcId, MutableList<DialogMessage>> = mutableMapOf(),
    val activeEvents: MutableList<ActiveEvent> = mutableListOf(),
    var stepCount: Int = 0,
    var finished: Boolean = false,
) {

    fun has(item: ItemId): Boolean = inventory.contains(item)
    fun has(flag: FlagId): Boolean = flags.contains(flag)
    fun knows(hint: HintId): Boolean = revealedHints.contains(hint)

    fun addItem(item: ItemId) {
        if (!inventory.contains(item)) inventory.add(item)
    }

    fun removeItem(item: ItemId) {
        inventory.remove(item)
    }

    fun historyFor(npc: NpcId): MutableList<DialogMessage> =
        npcHistories.getOrPut(npc) { mutableListOf() }

    /** Tiefe Kopie mit neuer Id — Grundlage für manuelle Spielstands-Klone. */
    fun cloneAs(newName: String): GameState {
        val copy = fromJson(toJson())
        return GameState(
            id = UUID.randomUUID().toString(),
            name = newName,
            seed = copy.seed,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            playtimeMs = copy.playtimeMs,
            locationId = copy.locationId,
            heroX = copy.heroX,
            heroY = copy.heroY,
            inventory = copy.inventory,
            flags = copy.flags,
            revealedHints = copy.revealedHints,
            npcHistories = copy.npcHistories,
            activeEvents = copy.activeEvents,
            stepCount = copy.stepCount,
            finished = copy.finished,
        )
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("seed", seed)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
        put("playtimeMs", playtimeMs)
        put("locationId", locationId.name)
        put("heroX", heroX.toDouble())
        put("heroY", heroY.toDouble())
        put("inventory", JSONArray().also { arr -> inventory.forEach { arr.put(it.name) } })
        put("flags", JSONArray().also { arr -> flags.forEach { arr.put(it.name) } })
        put("revealedHints", JSONArray().also { arr -> revealedHints.forEach { arr.put(it.name) } })
        put("npcHistories", JSONObject().also { obj ->
            npcHistories.forEach { (npc, messages) ->
                obj.put(npc.name, JSONArray().also { arr -> messages.forEach { arr.put(it.toJson()) } })
            }
        })
        put("activeEvents", JSONArray().also { arr -> activeEvents.forEach { arr.put(it.toJson()) } })
        put("stepCount", stepCount)
        put("finished", finished)
    }

    companion object {
        fun fromJson(json: JSONObject): GameState {
            val state = GameState(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name"),
                seed = json.optLong("seed", 0L),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = json.optLong("updatedAt", System.currentTimeMillis()),
                playtimeMs = json.optLong("playtimeMs", 0L),
                locationId = enumOrNull<LocationId>(json.optString("locationId"))
                    ?: LocationId.HAFEN,
                heroX = json.optDouble("heroX", 0.5).toFloat(),
                heroY = json.optDouble("heroY", 0.86).toFloat(),
                stepCount = json.optInt("stepCount", 0),
                finished = json.optBoolean("finished", false),
            )
            json.optJSONArray("inventory")?.let { arr ->
                for (i in 0 until arr.length()) {
                    enumOrNull<ItemId>(arr.optString(i))?.let { state.inventory.add(it) }
                }
            }
            json.optJSONArray("flags")?.let { arr ->
                for (i in 0 until arr.length()) {
                    enumOrNull<FlagId>(arr.optString(i))?.let { state.flags.add(it) }
                }
            }
            json.optJSONArray("revealedHints")?.let { arr ->
                for (i in 0 until arr.length()) {
                    enumOrNull<HintId>(arr.optString(i))?.let { state.revealedHints.add(it) }
                }
            }
            json.optJSONObject("npcHistories")?.let { obj ->
                obj.keys().forEach { key ->
                    val npc = enumOrNull<NpcId>(key) ?: return@forEach
                    val arr = obj.optJSONArray(key) ?: return@forEach
                    val messages = state.historyFor(npc)
                    for (i in 0 until arr.length()) {
                        arr.optJSONObject(i)?.let { messages.add(DialogMessage.fromJson(it)) }
                    }
                }
            }
            json.optJSONArray("activeEvents")?.let { arr ->
                for (i in 0 until arr.length()) {
                    arr.optJSONObject(i)?.let { obj ->
                        ActiveEvent.fromJson(obj)?.let { state.activeEvents.add(it) }
                    }
                }
            }
            return state
        }
    }
}
