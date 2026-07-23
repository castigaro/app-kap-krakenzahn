package com.appsonar.kapkrakenzahn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.databinding.RowSavegameBinding
import com.appsonar.kapkrakenzahn.world.GameWorld
import java.text.DateFormat
import java.util.Date

/** Liste der Spielstände auf dem Startbildschirm. */
class SaveAdapter(
    private var items: List<GameState>,
    private val onClick: (GameState) -> Unit,
    private val onLongClick: (GameState) -> Unit,
) : RecyclerView.Adapter<SaveAdapter.VH>() {

    class VH(val binding: RowSavegameBinding) : RecyclerView.ViewHolder(binding.root)

    fun submit(list: List<GameState>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowSavegameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false,
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val save = items[position]
        holder.binding.saveName.text = save.name
        val ort = GameWorld.location(save.locationId).name
        val datum = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            .format(Date(save.updatedAt))
        val geloest = GameWorld.puzzles.count { GameWorld.isSolved(save, it.id) }
        holder.binding.saveSubtitle.text =
            "$ort · $geloest/${GameWorld.puzzles.size} Rätsel · $datum"
        holder.binding.root.setOnClickListener { onClick(save) }
        holder.binding.root.setOnLongClickListener { onLongClick(save); true }
    }

    override fun getItemCount(): Int = items.size
}
