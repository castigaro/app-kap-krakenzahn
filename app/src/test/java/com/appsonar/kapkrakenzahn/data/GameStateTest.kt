package com.appsonar.kapkrakenzahn.data

import com.appsonar.kapkrakenzahn.world.FlagId
import com.appsonar.kapkrakenzahn.world.HintId
import com.appsonar.kapkrakenzahn.world.ItemId
import com.appsonar.kapkrakenzahn.world.LocationId
import com.appsonar.kapkrakenzahn.world.NpcId
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// org.json ist eine Android-Framework-Klasse; unter Robolectric liegt eine
// echte Implementierung vor (in reinen JVM-Tests würde sie "not mocked" werfen).
@RunWith(RobolectricTestRunner::class)
class GameStateTest {

    @Test
    fun `JSON-Roundtrip erhaelt den kompletten Spielstand`() {
        val original = GameState(name = "Test", seed = 4711L, locationId = LocationId.HOEHLE, heroX = 0.42f)
        original.inventory.add(ItemId.LATERNE)
        original.inventory.add(ItemId.KRAKENAUGE)
        original.flags.add(FlagId.DUELL_GEWONNEN)
        original.revealedHints.add(HintId.HINT_KRAKEN_MUSIK)
        original.historyFor(NpcId.RASMUS).add(DialogMessage(DialogMessage.ROLE_PLAYER, "Hallo"))
        original.historyFor(NpcId.RASMUS).add(DialogMessage(DialogMessage.ROLE_NPC, "Hicks."))
        original.activeEvents.add(ActiveEvent(ActiveEvent.EVENT_STURM, 3, LocationId.KLIPPEN))
        original.stepCount = 12
        original.playtimeMs = 90_000

        val loaded = GameState.fromJson(original.toJson())

        assertEquals("Test", loaded.name)
        assertEquals(4711L, loaded.seed)
        assertEquals(LocationId.HOEHLE, loaded.locationId)
        assertEquals(0.42f, loaded.heroX, 0.0001f)
        assertEquals(listOf(ItemId.LATERNE, ItemId.KRAKENAUGE), loaded.inventory)
        assertTrue(loaded.has(FlagId.DUELL_GEWONNEN))
        assertTrue(loaded.knows(HintId.HINT_KRAKEN_MUSIK))
        assertEquals(2, loaded.historyFor(NpcId.RASMUS).size)
        assertEquals("Hicks.", loaded.historyFor(NpcId.RASMUS)[1].text)
        assertEquals(1, loaded.activeEvents.size)
        assertEquals(3, loaded.activeEvents.first().restSchritte)
        assertEquals(12, loaded.stepCount)
        assertEquals(90_000L, loaded.playtimeMs)
    }

    @Test
    fun `unbekannte Enum-Namen werden toleriert und uebersprungen`() {
        val json = JSONObject(
            """
            {
              "id":"x","name":"Alt","seed":1,"locationId":"HAFEN",
              "inventory":["LATERNE","GIBTESNICHT"],
              "flags":["DUELL_GEWONNEN","ALTES_FLAG"],
              "revealedHints":["HINT_KRAKEN_MUSIK","WEG"]
            }
            """.trimIndent(),
        )
        val loaded = GameState.fromJson(json)
        assertEquals(listOf(ItemId.LATERNE), loaded.inventory)
        assertTrue(loaded.has(FlagId.DUELL_GEWONNEN))
        assertEquals(1, loaded.revealedHints.size)
    }

    @Test
    fun `cloneAs erzeugt eigenstaendige Kopie mit neuer Id`() {
        val original = GameState(name = "A", seed = 5L)
        original.inventory.add(ItemId.SANDDORNPUNSCH)
        val klon = original.cloneAs("Sicherung")

        assertFalse(original.id == klon.id)
        assertEquals("Sicherung", klon.name)
        assertEquals(5L, klon.seed)
        klon.inventory.add(ItemId.FEUERPFEFFER)
        // Änderung am Klon darf das Original nicht berühren.
        assertEquals(listOf(ItemId.SANDDORNPUNSCH), original.inventory)
    }
}
