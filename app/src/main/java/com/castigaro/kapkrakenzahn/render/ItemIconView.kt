package com.castigaro.kapkrakenzahn.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.ItemId

/**
 * Zeichnet ein Inventar-Icon: eine dunkle Kachel mit handgemaltem
 * Vektor-Motiv des Gegenstands ([ItemIconPainter]). Kein Bild-Asset,
 * konsistent zum Cartoon-Stil.
 */
class ItemIconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var item: ItemId? = null

    /** Goldrahmen um die Kachel — markiert den gewählten Slot bzw. Gegenstand. */
    var highlight: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    fun setItem(item: ItemId?) {
        this.item = item
        contentDescription = item?.let { GameWorld.itemName(it) }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (highlight) {
            framePaint.strokeWidth = w * 0.06f
            val inset = w * 0.04f
            canvas.drawPath(
                PaintKit.roundedRect(inset, inset, w - inset, h - inset, w * 0.18f),
                framePaint,
            )
        }
        val id = item ?: return
        ItemIconPainter.draw(canvas, id, minOf(w, h))
    }

    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFFE0A73C.toInt()
    }
}
