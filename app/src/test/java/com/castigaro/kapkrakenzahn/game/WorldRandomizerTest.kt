package com.castigaro.kapkrakenzahn.game

import com.castigaro.kapkrakenzahn.data.ActiveEvent
import com.castigaro.kapkrakenzahn.world.FlagId
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.HintId
import com.castigaro.kapkrakenzahn.world.NpcId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldRandomizerTest {

    @Test fun `gleicher Seed erzeugt identische Variation`() {
        val a = Variation(999L)
        val b = Variation(999L)
        assertEquals(a.medaillonAmStrand, b.medaillonAmStrand)
        assertEquals(a.raetselSet, b.raetselSet)
        assertEquals(a.duellThema, b.duellThema)
        assertEquals(a.hintOwners, b.hintOwners)
        assertEquals(a.fieloStock, b.fieloStock)
    }

    @Test fun `verschiedene Seeds unterscheiden sich meist`() {
        // Über viele Seeds darf die Hinweisverteilung nicht konstant sein.
        val verteilungen = (1..50).map { Variation(it.toLong()).hintOwners[HintId.HINT_PUNSCH_REZEPT] }
        assertTrue("Verteilung wirkt nicht zufällig", verteilungen.toSet().size > 1)
    }

    @Test fun `jeder Hinweis hat genau einen Besitzer, Kraechz plappert einen zweiten`() {
        val v = Variation(7L)
        HintId.values().forEach { hint ->
            assertTrue("Hinweis $hint ohne Besitzer", v.hintOwners.containsKey(hint))
        }
        // Krächz kennt seinen eigenen plus den nachgeplapperten.
        assertTrue(v.hintsOf(NpcId.KRAECHZ).size >= 2)
    }

    @Test fun `EventDirector ist deterministisch bei gleichem Seed und Schritt`() {
        val s1 = GameWorld.newGame(42L, "A")
        val s2 = GameWorld.newGame(42L, "B")
        // Gleiche Ausgangslage → gleiche Ereignis-Entscheidung.
        repeat(5) {
            val e1 = EventDirector.onLocationChange(s1.also { it.stepCount++ })
            val e2 = EventDirector.onLocationChange(s2.also { it.stepCount++ })
            assertEquals(e1, e2)
        }
    }

    @Test fun `Springflut sperrt die Hoehle nur bei bereits erreichtem Zugang`() {
        val s = GameWorld.newGame(1L, "A")
        s.flags.add(FlagId.HOEHLE_ZUGANG)
        s.activeEvents.add(ActiveEvent(ActiveEvent.EVENT_SPRINGFLUT, 2, com.castigaro.kapkrakenzahn.world.LocationId.HOEHLE))
        assertTrue(GameWorld.springflutAktiv(s))
        assertTrue("Höhle bei Springflut nicht begehbar", !GameWorld.hoehleBegehbar(s))
    }
}
