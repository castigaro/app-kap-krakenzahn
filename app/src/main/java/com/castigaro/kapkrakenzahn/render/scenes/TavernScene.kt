package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import android.graphics.Color
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter
import kotlin.math.sin

/** Innenraum der Taverne: Holzwände, Balken, Theke, Punschfass, Laterne, Regal. */
object TavernScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        // Warme, dunkle Holzwand
        PaintKit.ditherGradient(canvas, 0f, 0f, w, h * 0.82f, 0xFF5E3B21.toInt(), 0xFF3A2414.toInt(), 6)

        // Dielenboden mit Fluchtlinien
        PaintKit.perspectiveFloor(
            canvas, w, h, horizonYNorm = 0.80f, bottomYNorm = 1.0f,
            colorNear = 0xFF3A2414.toInt(), colorFar = 0xFF1E1208.toInt(),
            lineColor = Color.argb(80, 20, 12, 6), vanishXNorm = 0.5f, seams = 9,
        )

        // Deckenbalken
        canvas.drawRect(0f, 0f, w, h * 0.06f, PaintKit.solid(0xFF2A1A0E.toInt()))
        for (i in 0..4) {
            val x = w * (0.1f + i * 0.2f)
            PaintKit.shadedBox(canvas, x, 0f, x + w * 0.03f, h * 0.12f, 0xFF33200F.toInt(), 0.35f)
        }

        // Fenster mit Meerblick hinten
        PaintKit.ditherRect(canvas, w * 0.60f, h * 0.14f, w * 0.74f, h * 0.40f, PaintKit.MEER_OBEN, PaintKit.MEER_UNTEN, 8)
        PaintKit.radialGlow(canvas, w * 0.67f, h * 0.27f, w * 0.12f, Color.argb(70, 180, 220, 235))
        PaintKit.stroke.color = 0xFF2A1A0E.toInt()
        PaintKit.stroke.strokeWidth = h * 0.01f
        canvas.drawLine(w * 0.67f, h * 0.14f, w * 0.67f, h * 0.40f, PaintKit.stroke)
        canvas.drawLine(w * 0.60f, h * 0.27f, w * 0.74f, h * 0.27f, PaintKit.stroke)

        // Punschfass links
        PaintKit.shadedBox(canvas, w * 0.04f, h * 0.55f, w * 0.16f, h * 0.85f, PaintKit.HOLZ, 0.22f)
        canvas.drawRect(w * 0.04f, h * 0.63f, w * 0.16f, h * 0.66f, PaintKit.solid(PaintKit.FELS_DUNKEL))
        canvas.drawRect(w * 0.04f, h * 0.75f, w * 0.16f, h * 0.78f, PaintKit.solid(PaintKit.FELS_DUNKEL))

        // Theke
        PaintKit.shadedBox(canvas, w * 0.20f, h * 0.62f, w * 0.55f, h * 0.85f, 0xFF6E4522.toInt(), 0.18f)
        canvas.drawRect(w * 0.20f, h * 0.60f, w * 0.55f, h * 0.63f, PaintKit.solid(0xFF8A5A34.toInt()))

        // Laterne (flackert) — warmer Lichtschein
        val flicker = 0.7f + 0.3f * sin(timeMs / 120.0).toFloat()
        PaintKit.radialGlow(canvas, w * 0.42f, h * 0.26f, h * 0.22f * flicker, Color.argb((150 * flicker).toInt(), 255, 200, 90))
        canvas.drawRect(w * 0.40f, h * 0.16f, w * 0.44f, h * 0.20f, PaintKit.solid(PaintKit.FELS_DUNKEL))
        canvas.drawCircle(w * 0.42f, h * 0.25f, h * 0.035f * flicker, PaintKit.solid(Color.argb(220, 255, 220, 130)))
        canvas.drawRect(w * 0.405f, h * 0.20f, w * 0.435f, h * 0.28f, PaintKit.solid(PaintKit.GOLD))

        // Regal rechts
        PaintKit.shadedBox(canvas, w * 0.78f, h * 0.18f, w * 0.96f, h * 0.60f, 0xFF4A2E18.toInt(), 0.14f)
        for (i in 0..2) {
            val y = h * (0.28f + i * 0.13f)
            canvas.drawRect(w * 0.78f, y, w * 0.96f, y + h * 0.02f, PaintKit.solid(0xFF2A1A0E.toInt()))
            canvas.drawCircle(w * (0.82f + i * 0.03f), y - h * 0.03f, h * 0.02f, PaintKit.solid(0xFFB0C4C0.toInt()))
        }

        // Warme Lichtpfütze auf dem Boden, dann kräftige Vignette (Kneipenlicht)
        PaintKit.radialGlow(canvas, w * 0.42f, h * 0.9f, w * 0.4f, Color.argb(40, 255, 190, 90))
        PaintKit.vignette(canvas, w, h, 150)
    }
}
