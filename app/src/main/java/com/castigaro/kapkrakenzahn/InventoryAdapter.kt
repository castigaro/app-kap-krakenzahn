package com.castigaro.kapkrakenzahn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.castigaro.kapkrakenzahn.databinding.ItemInventoryBinding
import com.castigaro.kapkrakenzahn.world.ItemId

/** Horizontale Inventarleiste; Tap wählt einen Gegenstand zum Benutzen aus. */
class InventoryAdapter(
    private var items: List<ItemId>,
    private val onClick: (ItemId) -> Unit,
) : RecyclerView.Adapter<InventoryAdapter.VH>() {

    class VH(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root)

    fun submit(list: List<ItemId>) {
        items = list
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
        holder.binding.itemIcon.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
