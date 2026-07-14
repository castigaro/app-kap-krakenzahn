package com.castigaro.kapkrakenzahn.render

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.ItemId

/**
 * Zeichnet ein Inventar-Icon programmatisch: eine farbige, gerundete Kachel
 * mit den Initialen des Gegenstands. Kein Bild-Asset, konsistent zum
 * Cartoon-Stil.
 */
class ItemIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var item: ItemId? = null

    fun setItem(item: ItemId) {
        this.item = item
        contentDescription = GameWorld.itemName(item)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val id = item ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        val pad = w * 0.1f
        val color = colorFor(id)

        canvas.drawPath(
            PaintKit.roundedRect(pad, pad, w - pad, h - pad, w * 0.18f),
            PaintKit.solid(color),
        )
        canvas.drawPath(
            PaintKit.roundedRect(pad, pad, w - pad, h * 0.32f, w * 0.18f),
            PaintKit.solid(lighten(color)),
        )

        val label = initials(GameWorld.itemName(id))
        PaintKit.text.textSize = h * 0.34f
        PaintKit.text.color = 0xFF16110A.toInt()
        val fm = PaintKit.text.fontMetrics
        val baseline = h / 2 - (fm.ascent + fm.descent) / 2
        canvas.drawText(label, w / 2, baseline, PaintKit.text)
    }

    private fun initials(name: String): String {
        val words = name.split(" ", "-").filter { it.isNotBlank() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            name.length >= 2 -> name.take(2).uppercase()
            else -> name.uppercase()
        }
    }

    private fun colorFor(id: ItemId): Int = PALETTE[id.ordinal % PALETTE.size]

    private fun lighten(color: Int): Int {
        val r = ((color shr 16 and 0xFF) + 40).coerceAtMost(255)
        val g = ((color shr 8 and 0xFF) + 40).coerceAtMost(255)
        val b = ((color and 0xFF) + 40).coerceAtMost(255)
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    companion object {
        private val PALETTE = listOf(
            0xFFE0A73C.toInt(), 0xFF3AA69F.toInt(), 0xFFB86A4A.toInt(),
            0xFF8AA84A.toInt(), 0xFFC9843C.toInt(), 0xFF7A72C7.toInt(),
            0xFFD98A6A.toInt(), 0xFF5AA0B0.toInt(),
        )
    }
}
