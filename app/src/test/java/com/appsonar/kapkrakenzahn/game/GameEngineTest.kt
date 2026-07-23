package com.appsonar.kapkrakenzahn.game

import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.world.FlagId
import com.appsonar.kapkrakenzahn.world.GameWorld
import com.appsonar.kapkrakenzahn.world.Hotspot
import com.appsonar.kapkrakenzahn.world.ItemId
import com.appsonar.kapkrakenzahn.world.LocationId
import com.appsonar.kapkrakenzahn.world.NpcId
import com.appsonar.kapkrakenzahn.world.PuzzleId
import com.appsonar.kapkrakenzahn.world.RectFN
import com.appsonar.kapkrakenzahn.world.VerbId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prüft, dass jedes Rätsel über JEDEN seiner Lösungswege lösbar ist —
 * indem die jeweiligen Voraussetzungen gesetzt und die entscheidende
 * Aktion ausgeführt werden.
 */
class GameEngineTest {

    private fun game(loc: LocationId, vararg items: ItemId): GameState {
        val s = GameWorld.newGame(1L, "T")
        s.locationId = loc
        items.forEach { s.inventory.add(it) }
        return s
    }

    private fun act(s: GameState, verb: VerbId, hotspot: String, item: ItemId? = null) =
        GameEngine.performAction(s, verb, hotspot, item)

    // ---- Rätsel 1: Rotbarts Geheimnis --------------------------------------

    @Test fun `Rotbart via Duell`() {
        val s = game(LocationId.TAVERNE)
        GameEngine.winDuel(s)
        assertTrue(GameWorld.isSolved(s, PuzzleId.ROTBARTS_GEHEIMNIS))
        assertTrue(s.has(FlagId.WEG_ZUR_HOEHLE_BEKANNT))
    }

    @Test fun `Rotbart via Spezial-Punsch`() {
        val s = game(LocationId.TAVERNE, ItemId.SANDDORNPUNSCH, ItemId.FEUERPFEFFER)
        GameEngine.combine(s, ItemId.SANDDORNPUNSCH, ItemId.FEUERPFEFFER)
        assertTrue(s.has(ItemId.SPEZIALPUNSCH))
        act(s, VerbId.BENUTZE, "taverne_rasmus", ItemId.SPEZIALPUNSCH)
        assertTrue(GameWorld.isSolved(s, PuzzleId.ROTBARTS_GEHEIMNIS))
    }

    @Test fun `Rotbart via Medaillon`() {
        val s = game(LocationId.TAVERNE, ItemId.MEDAILLON)
        act(s, VerbId.BENUTZE, "taverne_rasmus", ItemId.MEDAILLON)
        assertTrue(GameWorld.isSolved(s, PuzzleId.ROTBARTS_GEHEIMNIS))
    }

    // ---- Rätsel 2: Der Dornenpfad ------------------------------------------

    @Test fun `Dornenpfad via Machete`() {
        val s = game(LocationId.MARKT, ItemId.MACHETE)
        act(s, VerbId.BENUTZE, "markt_dornen", ItemId.MACHETE)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DER_DORNENPFAD))
    }

    @Test fun `Dornenpfad via Stinkfisch`() {
        val s = game(LocationId.MARKT, ItemId.STINKFISCH)
        act(s, VerbId.BENUTZE, "markt_dornen", ItemId.STINKFISCH)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DER_DORNENPFAD))
    }

    @Test fun `Dornenpfad via Seitenpfad`() {
        val s = game(LocationId.STRAND, ItemId.BRENNENDE_LATERNE)
        s.revealedHints.add(com.appsonar.kapkrakenzahn.world.HintId.HINT_SEITENPFAD)
        act(s, VerbId.SCHAU, "strand_felsspalt")
        assertTrue(GameWorld.isSolved(s, PuzzleId.DER_DORNENPFAD))
    }

    // ---- Rätsel 3: Die Fluthöhle -------------------------------------------

    @Test fun `Fluthoehle via Boot`() {
        val s = game(LocationId.HAFEN)
        s.flags.add(FlagId.GRIET_GEFALLEN)
        act(s, VerbId.BENUTZE, "hafen_boot")
        assertTrue(GameWorld.isSolved(s, PuzzleId.DIE_FLUTHOEHLE))
        assertTrue(s.has(FlagId.HOEHLE_ZUGANG))
    }

    @Test fun `Fluthoehle via Amulett`() {
        val s = game(LocationId.DSCHUNGEL, ItemId.GEZEITENAMULETT)
        act(s, VerbId.BENUTZE, "dschungel_schlund", ItemId.GEZEITENAMULETT)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DIE_FLUTHOEHLE))
    }

    @Test fun `Fluthoehle via Abseilen`() {
        val s = game(LocationId.KLIPPEN, ItemId.WURFANKER)
        act(s, VerbId.BENUTZE, "klippen_kante", ItemId.WURFANKER)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DIE_FLUTHOEHLE))
    }

    // ---- Rätsel 4: Licht ins Dunkel ----------------------------------------

    @Test fun `Licht via Laterne`() {
        val s = game(LocationId.TAVERNE, ItemId.LATERNE, ItemId.TRANOEL)
        GameEngine.combine(s, ItemId.LATERNE, ItemId.TRANOEL)
        assertTrue(GameWorld.isSolved(s, PuzzleId.LICHT_INS_DUNKEL))
        assertTrue(s.has(ItemId.BRENNENDE_LATERNE))
    }

    @Test fun `Licht via Quallen`() {
        val s = game(LocationId.STRAND, ItemId.EIMER)
        act(s, VerbId.BENUTZE, "strand_tuempel", ItemId.EIMER)
        assertTrue(GameWorld.isSolved(s, PuzzleId.LICHT_INS_DUNKEL))
    }

    @Test fun `Licht via Fackel`() {
        val s = game(LocationId.DSCHUNGEL, ItemId.STOCK, ItemId.TEER, ItemId.FEUERSTEIN)
        GameEngine.combine(s, ItemId.STOCK, ItemId.TEER)
        GameEngine.combine(s, ItemId.TEERFACKEL, ItemId.FEUERSTEIN)
        assertTrue(GameWorld.isSolved(s, PuzzleId.LICHT_INS_DUNKEL))
        assertTrue(s.has(ItemId.FACKEL))
    }

    // ---- Rätsel 5: Des Wächters Rätsel -------------------------------------

    @Test fun `Waechter via Raetsel`() {
        val s = game(LocationId.HOEHLE, ItemId.BRENNENDE_LATERNE)
        GameEngine.solveRiddle(s)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DES_WAECHTERS_RAETSEL))
        assertTrue(s.has(ItemId.KRAKENAUGE))
    }

    @Test fun `Waechter via Muschelhorn`() {
        val s = game(LocationId.HOEHLE, ItemId.BRENNENDE_LATERNE, ItemId.MUSCHELHORN)
        act(s, VerbId.BENUTZE, "hoehle_wasser", ItemId.MUSCHELHORN)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DES_WAECHTERS_RAETSEL))
    }

    @Test fun `Waechter via Opfer`() {
        val s = game(LocationId.HOEHLE, ItemId.BRENNENDE_LATERNE, ItemId.SPIEGEL)
        act(s, VerbId.BENUTZE, "hoehle_opferbecken", ItemId.SPIEGEL)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DES_WAECHTERS_RAETSEL))
    }

    // ---- Rätsel 6: Das neue Feuer ------------------------------------------

    @Test fun `Feuer-Tuer via Schluessel`() {
        val s = game(LocationId.LEUCHTTURM, ItemId.SCHLUESSEL)
        act(s, VerbId.BENUTZE, "turm_tuer", ItemId.SCHLUESSEL)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DAS_NEUE_FEUER))
    }

    @Test fun `Feuer-Tuer via Dietrich`() {
        val s = game(LocationId.LEUCHTTURM, ItemId.DIETRICH)
        act(s, VerbId.BENUTZE, "turm_tuer", ItemId.DIETRICH)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DAS_NEUE_FEUER))
    }

    @Test fun `Feuer-Tuer via Ersatzschluessel`() {
        val s = game(LocationId.LEUCHTTURM)
        s.revealedHints.add(com.appsonar.kapkrakenzahn.world.HintId.HINT_ERSATZSCHLUESSEL)
        act(s, VerbId.NIMM, "turm_stein")
        assertTrue(s.has(ItemId.ERSATZSCHLUESSEL))
        act(s, VerbId.BENUTZE, "turm_tuer", ItemId.ERSATZSCHLUESSEL)
        assertTrue(GameWorld.isSolved(s, PuzzleId.DAS_NEUE_FEUER))
    }

    // ---- Ende --------------------------------------------------------------

    @Test fun `Endsequenz entzuendet das Feuer`() {
        val s = game(LocationId.LEUCHTTURM, ItemId.KRAKENAUGE, ItemId.LAPPEN)
        s.flags.add(FlagId.LEUCHTTURM_OFFEN)
        act(s, VerbId.BENUTZE, "turm_fassung", ItemId.KRAKENAUGE)
        assertTrue(s.has(FlagId.AUGE_EINGESETZT))
        val result = act(s, VerbId.BENUTZE, "turm_spiegel", ItemId.LAPPEN)
        assertTrue(s.has(FlagId.FEUER_ENTZUENDET))
        assertTrue(result.endGame)
        assertTrue(s.finished)
    }

    // ---- Werkbank: combineAll ----------------------------------------------

    @Test fun `combineAll reicht Paare an combine durch`() {
        val s = game(LocationId.HAFEN, ItemId.LATERNE, ItemId.TRANOEL)
        val result = GameEngine.combineAll(s, listOf(ItemId.LATERNE, ItemId.TRANOEL))
        assertTrue(result.stateChanged)
        assertTrue(s.has(ItemId.BRENNENDE_LATERNE))
        assertFalse(s.has(ItemId.LATERNE))
        assertFalse(s.has(ItemId.TRANOEL))
        assertTrue(s.has(FlagId.LICHT_LATERNE))
    }

    @Test fun `combineAll verkettet Dreier zur Fackel — Feuerstein bleibt`() {
        // In jeder Reihenfolge: Stock+Teer → Teerfackel, +Feuerstein → Fackel.
        val dreier = listOf(ItemId.STOCK, ItemId.TEER, ItemId.FEUERSTEIN)
        for (i in dreier.indices) {
            val items = dreier.drop(i) + dreier.take(i)
            val s = game(LocationId.DSCHUNGEL, ItemId.STOCK, ItemId.TEER, ItemId.FEUERSTEIN)
            val result = GameEngine.combineAll(s, items)
            assertTrue(result.stateChanged)
            assertTrue(s.has(ItemId.FACKEL))
            assertTrue(s.has(ItemId.FEUERSTEIN)) // wird nicht verbraucht
            assertFalse(s.has(ItemId.STOCK))
            assertFalse(s.has(ItemId.TEER))
            assertFalse(s.has(ItemId.TEERFACKEL))
            assertTrue(s.has(FlagId.LICHT_FACKEL))
        }
    }

    @Test fun `combineAll wertet Teiltreffer als Erfolg`() {
        val s = game(LocationId.DSCHUNGEL, ItemId.STOCK, ItemId.TEER, ItemId.SEIL)
        val result = GameEngine.combineAll(s, listOf(ItemId.STOCK, ItemId.TEER, ItemId.SEIL))
        assertTrue(result.stateChanged)
        assertTrue(s.has(ItemId.TEERFACKEL))
        assertTrue(s.has(ItemId.SEIL)) // unbeteiligt, bleibt
    }

    @Test fun `combineAll ohne Treffer aendert nichts`() {
        val s = game(LocationId.DSCHUNGEL, ItemId.MEDAILLON, ItemId.SEIL, ItemId.STOCK)
        val result = GameEngine.combineAll(s, listOf(ItemId.MEDAILLON, ItemId.SEIL, ItemId.STOCK))
        assertFalse(result.stateChanged)
        assertTrue(s.has(ItemId.MEDAILLON))
        assertTrue(s.has(ItemId.SEIL))
        assertTrue(s.has(ItemId.STOCK))
    }

    // ---- Doppeltipp: defaultVerb -------------------------------------------

    private fun hotspot(verbs: Set<VerbId>, npc: NpcId? = null) = Hotspot(
        id = "test", name = "Test", beschreibung = "Test",
        rect = RectFN(0f, 0f, 1f, 1f), verbs = verbs, npc = npc,
    )

    @Test fun `defaultVerb waehlt sinnvolle Standards`() {
        assertEquals(
            VerbId.REDE,
            GameEngine.defaultVerb(hotspot(setOf(VerbId.SCHAU, VerbId.REDE), NpcId.YOLA)),
        )
        assertEquals(
            VerbId.NIMM,
            GameEngine.defaultVerb(hotspot(setOf(VerbId.SCHAU, VerbId.NIMM))),
        )
        assertEquals(
            VerbId.SCHAU,
            GameEngine.defaultVerb(hotspot(setOf(VerbId.SCHAU, VerbId.BENUTZE))),
        )
    }
}
