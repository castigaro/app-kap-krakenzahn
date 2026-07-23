package com.appsonar.kapkrakenzahn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appsonar.kapkrakenzahn.databinding.ItemInventoryBinding
import com.appsonar.kapkrakenzahn.world.ItemId

/** Horizontale Inventarleiste; Tap wählt einen Gegenstand zum Benutzen aus. */
class InventoryAdapter(
    private var items: List<ItemId>,
    private val onClick: (ItemId) -> Unit,
) : RecyclerView.Adapter<InventoryAdapter.VH>() {

    class VH(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)

    /** Gold umrandeter Gegenstand (aktives „Benutze X mit …"). */
    private var selected: ItemId? = null

    fun submit(list: List<ItemId>, selected: ItemId? = null) {
        items = list
        this.selected = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemInventoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.itemIcon.setItem(item)
        holder.binding.itemIcon.highlight = item == selected
        holder.binding.itemIcon.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
