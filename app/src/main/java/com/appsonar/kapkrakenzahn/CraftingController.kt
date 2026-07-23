package com.appsonar.kapkrakenzahn

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsonar.kapkrakenzahn.databinding.ViewCraftingOverlayBinding
import com.appsonar.kapkrakenzahn.world.GameWorld
import com.appsonar.kapkrakenzahn.world.ItemId

/**
 * Steuert die Werkbank (Muster aus the-llm-adventure): drei Eingabe-Slots,
 * ein Ergebnis-Slot und darunter das Inventar zum Bestücken. Tippe einen
 * Slot an, dann einen Gegenstand aus dem Inventar; ein gefüllter Slot wird
 * durch erneutes Antippen geleert. „Verbinden" ist ab zwei belegten Slots
 * aktiv — die Kombination selbst entscheidet die [GameEngine].
 */
class CraftingController(
    private val binding: ViewCraftingOverlayBinding,
    private val inventory: () -> List<ItemId>,
    private val onCombine: (List<ItemId>) -> Unit,
) {

    private val slots = arrayOfNulls<ItemId>(3)
    private var selectedSlot = 0
    private val slotViews = listOf(binding.craftSlot0, binding.craftSlot1, binding.craftSlot2)

    private val invAdapter = InventoryAdapter(emptyList()) { placeInSlot(it) }

    init {
        binding.craftInventory.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.craftInventory.adapter = invAdapter

        slotViews.forEachIndexed { index, view ->
            view.setOnClickListener { onSlotTap(index) }
        }
        binding.craftCombine.setOnClickListener {
            onCombine(slots.filterNotNull())
        }
        binding.craftCancel.setOnClickListener { close() }
        // Tipp neben das Panel schließt die Werkbank.
        binding.craftRoot.setOnClickListener { close() }
    }

    val isOpen: Boolean get() = binding.root.visibility == View.VISIBLE

    fun open() {
        reset()
        refreshInventory()
        binding.root.visibility = View.VISIBLE
    }

    fun close() {
        binding.root.visibility = View.GONE
    }

    fun refreshInventory() {
        // Bereits in Slots liegende Gegenstände nicht doppelt anbieten.
        val used = slots.filterNotNull().toSet()
        invAdapter.submit(inventory().filter { it !in used })
    }

    fun reset() {
        slots.fill(null)
        selectedSlot = 0
        renderSlots()
        binding.craftResult.setItem(null)
        binding.craftCombine.isEnabled = false
        binding.craftFeedback.visibility = View.GONE
    }

    /**
     * Zeigt das Urteil der letzten Kombination im Overlay an — die
     * Kommentarzeile läge hinter dem vollflächigen Overlay und wäre unsichtbar.
     */
    fun showResult(text: String) {
        binding.craftFeedback.text = text
        binding.craftFeedback.visibility = View.VISIBLE
    }

    private fun onSlotTap(index: Int) {
        if (slots[index] != null) {
            // Gefüllten Slot leeren
            slots[index] = null
        }
        selectedSlot = index
        renderSlots()
        refreshInventory()
        updateCombineState()
    }

    private fun placeInSlot(item: ItemId) {
        // In den ausgewählten (oder ersten freien) Slot legen.
        val target = if (slots[selectedSlot] == null) selectedSlot
        else slots.indexOfFirst { it == null }
        if (target < 0) return
        slots[target] = item
        selectedSlot = (target + 1).coerceAtMost(2)
        renderSlots()
        refreshInventory()
        updateCombineState()
    }

    private fun renderSlots() {
        slotViews.forEachIndexed { index, view ->
            view.setItem(slots[index])
            view.highlight = index == selectedSlot
        }
        renderSlotNames()
    }

    /** Schreibt die eingelegten Gegenstände aus („Kräftiger Ast + Teerklumpen + …"). */
    private fun renderSlotNames() {
        val filled = slots.filterNotNull()
        if (filled.isEmpty()) {
            binding.craftSlotNames.visibility = View.GONE
            return
        }
        val namen = filled.joinToString(" + ") { GameWorld.itemName(it) }
        val offen = if (slots.any { it == null }) " + …" else ""
        binding.craftSlotNames.text = namen + offen
        binding.craftSlotNames.visibility = View.VISIBLE
    }

    private fun updateCombineState() {
        binding.craftCombine.isEnabled = slots.count { it != null } >= 2
    }
}
