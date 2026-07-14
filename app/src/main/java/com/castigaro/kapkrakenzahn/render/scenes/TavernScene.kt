package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter
import kotlin.math.sin

/** Innenraum der Taverne: Holzwände, Balken, Theke, Punschfass, Laterne, Regal. */
object TavernScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        // Warme, dunkle Holzwand
        PaintKit.verticalGradient(canvas, 0f, 0f, w, h, 0xFF5E3B21.toInt(), 0xFF3A2414.toInt())
        // Boden
        canvas.drawRect(0f, h * 0.80f, w, h, PaintKit.solid(0xFF2A1A0E.toInt()))
        canvas.drawRect(0f, h * 0.80f, w, h * 0.82f, PaintKit.solid(PaintKit.HOLZ))

        // Deckenbalken
        for (i in 0..4) {
            val x = w * (0.1f + i * 0.2f)
            canvas.drawRect(x, 0f, x + w * 0.03f, h * 0.12f, PaintKit.solid(0xFF2A1A0E.toInt()))
        }
        canvas.drawRect(0f, 0f, w, h * 0.06f, PaintKit.solid(0xFF2A1A0E.toInt()))

        // Fenster mit Meerblick hinten
        canvas.drawRect(w * 0.60f, h * 0.14f, w * 0.74f, h * 0.40f, PaintKit.solid(0xFF1C6E8C.toInt()))
        PaintKit.stroke.color = 0xFF2A1A0E.toInt()
        PaintKit.stroke.strokeWidth = h * 0.01f
        canvas.drawLine(w * 0.67f, h * 0.14f, w * 0.67f, h * 0.40f, PaintKit.stroke)
        canvas.drawLine(w * 0.60f, h * 0.27f, w * 0.74f, h * 0.27f, PaintKit.stroke)

        // Punschfass links
        canvas.drawRect(w * 0.04f, h * 0.55f, w * 0.16f, h * 0.85f, PaintKit.solid(PaintKit.HOLZ))
        canvas.drawRect(w * 0.04f, h * 0.63f, w * 0.16f, h * 0.66f, PaintKit.solid(PaintKit.FELS_DUNKEL))
        canvas.drawRect(w * 0.04f, h * 0.75f, w * 0.16f, h * 0.78f, PaintKit.solid(PaintKit.FELS_DUNKEL))

        // Theke
        canvas.drawRect(w * 0.20f, h * 0.62f, w * 0.55f, h * 0.85f, PaintKit.solid(0xFF6E4522.toInt()))
        canvas.drawRect(w * 0.20f, h * 0.60f, w * 0.55f, h * 0.63f, PaintKit.solid(0xFF8A5A34.toInt()))

        // Laterne (flackert)
        val flicker = 0.7f + 0.3f * sin(timeMs / 120.0).toFloat()
        canvas.drawRect(w * 0.40f, h * 0.16f, w * 0.44f, h * 0.20f, PaintKit.solid(PaintKit.FELS_DUNKEL))
        val glow = PaintKit.solid(android.graphics.Color.argb((160 * flicker).toInt(), 255, 200, 90))
        canvas.drawCircle(w * 0.42f, h * 0.25f, h * 0.05f * flicker, glow)
        canvas.drawRect(w * 0.405f, h * 0.20f, w * 0.435f, h * 0.28f, PaintKit.solid(PaintKit.GOLD))

        // Regal rechts
        canvas.drawRect(w * 0.78f, h * 0.18f, w * 0.96f, h * 0.60f, PaintKit.solid(0xFF4A2E18.toInt()))
        for (i in 0..2) {
            val y = h * (0.28f + i * 0.13f)
            canvas.drawRect(w * 0.78f, y, w * 0.96f, y + h * 0.02f, PaintKit.solid(0xFF2A1A0E.toInt()))
            canvas.drawCircle(w * (0.82f + i * 0.03f), y - h * 0.03f, h * 0.02f, PaintKit.solid(0xFFB0C4C0.toInt()))
        }
    }
}
