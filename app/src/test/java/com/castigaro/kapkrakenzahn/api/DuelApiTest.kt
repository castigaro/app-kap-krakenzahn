package com.castigaro.kapkrakenzahn.api

import com.castigaro.kapkrakenzahn.game.DuelController
import com.castigaro.kapkrakenzahn.world.FallbackDuel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// parseRound nutzt org.json (Android-Framework) — daher unter Robolectric.
@RunWith(RobolectricTestRunner::class)
class DuelApiTest {

    @Test fun `Runde wird tolerant geparst`() {
        val raw = """Hier: {"beleidigung":"Du Landratte!","kontern":["A","B","C"],"richtig":2} Ende."""
        val round = DuelApi.parseRound(raw)
        assertEquals("Du Landratte!", round.beleidigung)
        assertEquals(3, round.kontern.size)
        assertEquals(2, round.richtig)
    }

    @Test fun `ungueltiger richtig-Index faellt auf 0 zurueck`() {
        val raw = """{"beleidigung":"x","kontern":["A","B"],"richtig":9}"""
        assertEquals(0, DuelApi.parseRound(raw).richtig)
    }

    @Test fun `Fallback-Runde hat drei Konter und gueltigen Treffer`() {
        val round = FallbackDuel.round(1, 42L)
        assertEquals(3, round.kontern.size)
        assertTrue(round.richtig in round.kontern.indices)
    }

    @Test fun `drei richtige Konter gewinnen das Duell`() {
        val controller = DuelController()
        repeat(3) {
            val round = DuelApi.DuelRound("beleidigung", listOf("gut", "schlecht", "lahm"), 0)
            controller.bewerte(round, 0) // immer den richtigen wählen
            if (!controller.vorbei) return@repeat
        }
        assertTrue(controller.vorbei)
        assertTrue(controller.spielerGewinnt)
        assertEquals(3, controller.spielerPunkte)
    }

    @Test fun `drei Fehlgriffe verlieren das Duell`() {
        val controller = DuelController()
        repeat(3) {
            val round = DuelApi.DuelRound("beleidigung", listOf("gut", "schlecht", "lahm"), 0)
            controller.bewerte(round, 1) // immer daneben
        }
        assertTrue(controller.vorbei)
        assertFalse(controller.spielerGewinnt)
        assertEquals(3, controller.gegnerPunkte)
    }
}
