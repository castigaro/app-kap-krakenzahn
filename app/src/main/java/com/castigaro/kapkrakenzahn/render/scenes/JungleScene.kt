package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter
import com.castigaro.kapkrakenzahn.world.GameWorld

/** Der Dschungelpfad: dichtes Grün, Farne, rechts der Höhlenschlund. */
object JungleScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        // Dunstiger Dschungel-Hintergrund
        PaintKit.verticalGradient(canvas, 0f, 0f, w, h, 0xFF9BC08A.toInt(), 0xFF3E6B3A.toInt())

        // Schichten aus Blattwerk (hinten hell → vorne dunkel)
        leafLayer(canvas, w, h, 0.20f, 0xFF4F8A4A.toInt())
        leafLayer(canvas, w, h, 0.30f, 0xFF3A7340.toInt())

        // Pfadboden
        canvas.drawRect(0f, h * 0.74f, w, h, PaintKit.solid(0xFF6E5A38.toInt()))
        canvas.drawRect(0f, h * 0.74f, w, h * 0.77f, PaintKit.solid(0xFF5A4A2E.toInt()))

        // Baumstämme
        for (x in listOf(0.10f, 0.45f, 0.62f)) {
            canvas.drawRect(w * x, h * 0.20f, w * (x + 0.04f), h * 0.78f, PaintKit.solid(PaintKit.HOLZ_DUNKEL))
        }

        // Farn / Gebüsch (mit Medaillon-Versteck)
        canvas.drawCircle(w * 0.28f, h * 0.70f, h * 0.09f, PaintKit.solid(PaintKit.LAUB_DUNKEL))
        canvas.drawCircle(w * 0.34f, h * 0.72f, h * 0.07f, PaintKit.solid(PaintKit.LAUB))

        // Ast
        PaintKit.stroke.color = PaintKit.HOLZ_DUNKEL
        PaintKit.stroke.strokeWidth = h * 0.015f
        canvas.drawLine(w * 0.42f, h * 0.82f, w * 0.54f, h * 0.80f, PaintKit.stroke)

        // Höhlenschlund rechts
        canvas.drawRect(w * 0.78f, 0f, w, h, PaintKit.solid(PaintKit.FELS_DUNKEL))
        val begehbar = GameWorld.hoehleBegehbar(state)
        canvas.drawOval(w * 0.80f, h * 0.30f, w * 0.99f, h * 0.90f, PaintKit.solid(PaintKit.NACHT))
        if (!begehbar) {
            // Flutwasser im Schlund
            canvas.drawRect(w * 0.80f, h * 0.62f, w * 0.99f, h * 0.90f, PaintKit.solid(0xCC0F4A5E.toInt()))
        }
    }

    private fun leafLayer(canvas: Canvas, w: Float, h: Float, top: Float, color: Int) {
        val p = PaintKit.solid(color)
        var x = -0.05f
        while (x < 1.05f) {
            canvas.drawCircle(w * x, h * top, h * 0.10f, p)
            x += 0.12f
        }
        canvas.drawRect(0f, h * top, w, h * (top + 0.06f), p)
    }
}
