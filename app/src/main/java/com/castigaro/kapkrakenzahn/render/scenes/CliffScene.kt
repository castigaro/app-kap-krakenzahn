package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import android.graphics.Color
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter
import com.castigaro.kapkrakenzahn.world.GameWorld
import kotlin.random.Random

/** Der Klippenpfad: hohe Felskante über der See, Möwenfelsen; bei Sturm Regen. */
object CliffScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        val phase = timeMs / 1000f
        val sturm = GameWorld.sturmAktiv(state)

        if (sturm) {
            PaintKit.ditherGradient(canvas, 0f, 0f, w, h * 0.55f, 0xFF3A4453.toInt(), 0xFF6B7480.toInt(), 6)
        } else {
            PaintKit.ditherGradient(canvas, 0f, 0f, w, h * 0.55f, PaintKit.HIMMEL_TAG_OBEN, PaintKit.HIMMEL_TAG_UNTEN, 6)
            PaintKit.seagull(canvas, w * 0.5f, h * 0.14f, h * 0.03f, phase * 3)
        }

        // Tiefe See unten
        PaintKit.ditherGradient(canvas, 0f, h * 0.50f, w, h, PaintKit.MEER_OBEN, PaintKit.MEER_UNTEN, 6)
        canvas.drawPath(PaintKit.wave(w, h * 0.55f, h * 0.75f, h * 0.015f, w * 0.3f, phase), PaintKit.solid(0x33FFFFFF))

        // Klippenkante (Vordergrund, links breit)
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(
                0f to 0.55f, 0.55f to 0.60f, 0.50f to 1.0f, 0f to 1.0f,
            )),
            PaintKit.solid(PaintKit.FELS),
        )
        // Grasige Kante
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(
                0f to 0.55f, 0.55f to 0.60f, 0.55f to 0.64f, 0f to 0.60f,
            )),
            PaintKit.solid(PaintKit.LAUB_DUNKEL),
        )
        // Schattenkante der Klippe
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(
                0.42f to 0.72f, 0.50f to 0.62f, 0.50f to 1.0f, 0.40f to 1.0f,
            )),
            PaintKit.solid(PaintKit.darken(PaintKit.FELS, 0.7f)),
        )

        // Leuchtturm-Pfad rechts
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(
                0.80f to 0.58f, 1.0f to 0.55f, 1.0f to 1.0f, 0.78f to 1.0f,
            )),
            PaintKit.solid(PaintKit.FELS),
        )

        // Möwenfelsen (Zacken)
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(
                0.58f to 0.70f, 0.64f to 0.36f, 0.70f to 0.70f,
            )),
            PaintKit.solid(PaintKit.FELS_DUNKEL),
        )
        if (!sturm) {
            PaintKit.seagull(canvas, w * 0.64f, h * 0.32f, h * 0.02f, phase * 4)
            PaintKit.seagull(canvas, w * 0.60f, h * 0.40f, h * 0.018f, phase * 4 + 2)
        }

        // Regen bei Sturm
        if (sturm) {
            val rnd = Random((timeMs / 80))
            PaintKit.stroke.color = Color.argb(120, 200, 220, 235)
            PaintKit.stroke.strokeWidth = h * 0.004f
            for (i in 0..60) {
                val x = rnd.nextFloat() * w
                val y = rnd.nextFloat() * h * 0.9f
                canvas.drawLine(x, y, x - w * 0.01f, y + h * 0.05f, PaintKit.stroke)
            }
        }

        PaintKit.vignette(canvas, w, h, if (sturm) 140 else 95)
    }
}
