package com.appsonar.kapkrakenzahn.game

import com.appsonar.kapkrakenzahn.data.DialogMessage
import com.appsonar.kapkrakenzahn.world.FlagId
import com.appsonar.kapkrakenzahn.world.GameWorld
import com.appsonar.kapkrakenzahn.world.ItemId
import com.appsonar.kapkrakenzahn.world.NpcId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StateDigestTest {

    @Test fun `Digest nennt Inventar, geloeste und offene Raetsel`() {
        val s = GameWorld.newGame(1L, "T")
        s.inventory.add(ItemId.LATERNE)
        s.flags.add(FlagId.DUELL_GEWONNEN) // löst Rätsel 1

        val digest = StateDigest.build(s)
        assertTrue(digest.contains("Sturmlaterne"))
        assertTrue(digest.contains("Rotbarts Geheimnis")) // gelöst
        assertTrue(digest.contains("Der Dornenpfad")) // offen
    }

    @Test fun `Historie wird auf das Maximum gedeckelt`() {
        val s = GameWorld.newGame(1L, "T")
        repeat(30) {
            s.historyFor(NpcId.YOLA).add(DialogMessage(DialogMessage.ROLE_PLAYER, "m$it"))
        }
        val hist = StateDigest.history(s, NpcId.YOLA)
        assertEquals(StateDigest.MAX_HISTORY, hist.size)
        // Die jüngsten bleiben erhalten.
        assertEquals("m29", hist.last().text)
    }
}
