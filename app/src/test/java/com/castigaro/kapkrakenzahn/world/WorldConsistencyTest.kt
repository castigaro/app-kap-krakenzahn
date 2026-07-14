package com.castigaro.kapkrakenzahn.world

import com.castigaro.kapkrakenzahn.game.Variation
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Prüft die statische Konsistenz der Spielwelt (keine toten Verweise). */
class WorldConsistencyTest {

    @Test
    fun `jeder Ausgang zeigt auf einen existierenden Ort`() {
        GameWorld.locations.values.forEach { loc ->
            loc.exits.forEach { exit ->
                assertNotNull(
                    "Ausgang von ${loc.id} zeigt auf unbekannten Ort ${exit.target}",
                    GameWorld.locations[exit.target],
                )
            }
        }
    }

    @Test
    fun `jede Regel verweist auf existierenden Hotspot ihres Ortes`() {
        GameWorld.rules.forEach { rule ->
            val hotspotIds = GameWorld.location(rule.location).hotspots.map { it.id }.toSet()
            assertTrue(
                "Regel für ${rule.location}/${rule.hotspotId} hat keinen passenden Hotspot",
                rule.hotspotId in hotspotIds,
            )
        }
    }

    @Test
    fun `jeder NPC-Hotspot verweist auf eine definierte Figur`() {
        GameWorld.locations.values.forEach { loc ->
            loc.hotspots.mapNotNull { it.npc }.forEach { npcId ->
                assertNotNull("Unbekannte Figur $npcId in ${loc.id}", GameWorld.npcs[npcId])
            }
        }
    }

    @Test
    fun `jedes Raetsel hat mindestens zwei Loesungswege`() {
        GameWorld.puzzles.forEach { puzzle ->
            assertTrue(
                "${puzzle.id} hat zu wenige Lösungswege",
                puzzle.paths.size >= 2,
            )
        }
    }

    @Test
    fun `jeder Hinweis ist mindestens einem NPC zugeteilt`() {
        val variation = Variation(1234L)
        val zugeteilt = HintId.values().filter { hint ->
            NpcId.values().any { npc -> hint in variation.hintsOf(npc) }
        }
        assertTrue(
            "Nicht alle Hinweise erreichbar: ${HintId.values().toSet() - zugeteilt.toSet()}",
            zugeteilt.size == HintId.values().size,
        )
    }

    @Test
    fun `jeder AllowedEffect nennt eine aufloesbare Id`() {
        val state = GameWorld.newGame(1L, "T")
        GameWorld.npcs.values.forEach { npc ->
            npc.allowedEffects.forEach { eff ->
                when (eff.typ) {
                    AllowedEffect.TYP_HINWEIS -> assertNotNull(HintId.valueOf(eff.id))
                    AllowedEffect.TYP_FLAG -> assertNotNull(FlagId.valueOf(eff.id))
                    AllowedEffect.TYP_ITEM ->
                        assertNotNull("Item ${eff.id}", GameWorld.resolveItem(state, eff.id))
                    AllowedEffect.TYP_ITEMWEG ->
                        assertNotNull("Item ${eff.id}", GameWorld.resolveItem(state, eff.id))
                    // DUELL/RAETSEL brauchen keine Item-/Flag-Id.
                }
            }
        }
    }
}
