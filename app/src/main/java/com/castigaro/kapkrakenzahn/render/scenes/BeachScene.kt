package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter

/** Der Strand: Meer, Sand, Maras Treibholzhütte, Gezeitentümpel, Wrack, Felsspalt. */
object BeachScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        val phase = timeMs / 1000f
        PaintKit.verticalGradient(canvas, 0f, 0f, w, h * 0.50f, PaintKit.HIMMEL_TAG_OBEN, PaintKit.HIMMEL_TAG_UNTEN)
        PaintKit.cloud(canvas, w * 0.6f, h * 0.12f, h * 0.05f)
        PaintKit.seagull(canvas, w * 0.3f, h * 0.16f, h * 0.03f, phase * 3)

        // Meer bis zur Brandung
        PaintKit.verticalGradient(canvas, 0f, h * 0.46f, w, h * 0.66f, PaintKit.MEER_OBEN, PaintKit.MEER_UNTEN)
        canvas.drawPath(PaintKit.wave(w, h * 0.62f, h * 0.70f, h * 0.01f, w * 0.20f, phase), PaintKit.solid(0x55FFFFFF))

        // Sand
        canvas.drawRect(0f, h * 0.64f, w, h, PaintKit.solid(PaintKit.SAND))
        canvas.drawPath(PaintKit.wave(w, h * 0.66f, h * 0.70f, h * 0.008f, w * 0.18f, -phase), PaintKit.solid(0x33FFFFFF))

        // Steilwand rechts mit Felsspalt
        canvas.drawRect(w * 0.72f, 0f, w, h * 0.72f, PaintKit.solid(PaintKit.FELS))
        canvas.drawRect(w * 0.55f, h * 0.30f, w * 0.63f, h * 0.66f, PaintKit.solid(PaintKit.NACHT)) // Felsspalt-Dunkel

        // Maras Treibholzhütte links
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(
                0.06f to 0.66f, 0.06f to 0.44f, 0.15f to 0.36f, 0.24f to 0.44f, 0.24f to 0.66f,
            )),
            PaintKit.solid(0xFF7A5A3A.toInt()),
        )
        canvas.drawRect(w * 0.12f, h * 0.52f, w * 0.18f, h * 0.66f, PaintKit.solid(PaintKit.HOLZ_DUNKEL)) // Tür
        // Netz
        PaintKit.stroke.color = 0xFF8A8070.toInt()
        PaintKit.stroke.strokeWidth = h * 0.004f
        canvas.drawLine(w * 0.26f, h * 0.40f, w * 0.30f, h * 0.66f, PaintKit.stroke)

        // Gezeitentümpel (schimmert)
        canvas.drawCircleTuempel(w, h, phase)

        // Strandfelsen
        canvas.drawCircle(w * 0.70f, h * 0.80f, h * 0.05f, PaintKit.solid(PaintKit.FELS))
        canvas.drawCircle(w * 0.66f, h * 0.83f, h * 0.035f, PaintKit.solid(PaintKit.FELS_DUNKEL))

        // Treibgut
        canvas.drawRect(w * 0.31f, h * 0.80f, w * 0.42f, h * 0.84f, PaintKit.solid(PaintKit.HOLZ_DUNKEL))

        // Wrack
        PaintKit.stroke.color = PaintKit.HOLZ_DUNKEL
        PaintKit.stroke.strokeWidth = h * 0.012f
        for (i in 0..4) {
            val x = w * (0.80f + i * 0.03f)
            canvas.drawLine(x, h * 0.78f, x + w * 0.01f, h * 0.55f, PaintKit.stroke)
        }
    }

    private fun Canvas.drawCircleTuempel(w: Float, h: Float, phase: Float) {
        val glow = 0.5f + 0.5f * kotlin.math.sin(phase.toDouble()).toFloat()
        drawOval(w * 0.48f, h * 0.80f, w * 0.62f, h * 0.90f, PaintKit.solid(0xFF15505E.toInt()))
        drawOval(
            w * 0.51f, h * 0.82f, w * 0.59f, h * 0.88f,
            PaintKit.solid(android.graphics.Color.argb((120 * glow).toInt(), 90, 220, 210)),
        )
    }
}
