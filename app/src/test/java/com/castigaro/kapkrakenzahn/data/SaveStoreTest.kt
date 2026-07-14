package com.castigaro.kapkrakenzahn.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.castigaro.kapkrakenzahn.world.ItemId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SaveStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun reset() {
        SaveStore.resetForTest()
        File(context.filesDir, "savegames.json").delete()
    }

    @Test
    fun `Spielstaende ueberleben einen Neustart`() {
        val save = GameState(name = "Roundtrip", seed = 1L)
        save.inventory.add(ItemId.LATERNE)
        SaveStore.add(context, save)

        SaveStore.resetForTest() // simuliert App-Neustart

        val loaded = SaveStore.get(context, save.id)!!
        assertEquals("Roundtrip", loaded.name)
        assertEquals(listOf(ItemId.LATERNE), loaded.inventory)
    }

    @Test
    fun `getAll sortiert zuletzt gespeicherte zuerst`() {
        SaveStore.add(context, GameState(name = "alt", seed = 1L, updatedAt = 1L))
        SaveStore.add(context, GameState(name = "neu", seed = 2L, updatedAt = 2L))
        assertEquals(listOf("neu", "alt"), SaveStore.getAll(context).map { it.name })
    }

    @Test
    fun `update ueberschreibt denselben Slot statt zu duplizieren`() {
        val save = GameState(name = "Auto", seed = 1L)
        SaveStore.add(context, save)
        save.stepCount = 5
        SaveStore.update(context, save)

        SaveStore.resetForTest()
        val alle = SaveStore.getAll(context)
        assertEquals(1, alle.size)
        assertEquals(5, alle.first().stepCount)
    }

    @Test
    fun `delete entfernt den Spielstand dauerhaft`() {
        val save = GameState(name = "weg", seed = 1L)
        SaveStore.add(context, save)
        SaveStore.delete(context, save)

        SaveStore.resetForTest()
        assertNull(SaveStore.get(context, save.id))
        assertTrue(SaveStore.getAll(context).isEmpty())
    }
}
