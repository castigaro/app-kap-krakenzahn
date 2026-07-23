package com.appsonar.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import android.graphics.Color
import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.render.PaintKit
import com.appsonar.kapkrakenzahn.render.ScenePainter
import com.appsonar.kapkrakenzahn.world.GameWorld

/** Die Marktgasse: Häuserzeile, Fischstand, Schmiedestand, rechts der Dornenpfad. */
object MarketScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        PaintKit.ditherGradient(canvas, 0f, 0f, w, h * 0.55f, PaintKit.HIMMEL_TAG_OBEN, PaintKit.HIMMEL_TAG_UNTEN, 6)
        PaintKit.radialGlow(canvas, w * 0.5f, h * 0.06f, w * 0.3f, Color.argb(90, 255, 245, 200))
        PaintKit.cloud(canvas, w * 0.35f, h * 0.14f, h * 0.05f)

        // Häuserzeile hinten (mit Licht-/Schattenseite)
        val farben = listOf(0xFFCFA97A.toInt(), 0xFFB98A5A.toInt(), 0xFFD9C0A0.toInt(), 0xFFA9754A.toInt())
        for (i in 0..3) {
            val x = w * (0.02f + i * 0.20f)
            val top = h * (0.30f + (i % 2) * 0.05f)
            PaintKit.shadedBox(canvas, x, top, x + w * 0.18f, h * 0.72f, farben[i], 0.16f)
            // Dach
            canvas.drawPath(
                PaintKit.polygon(w, h, listOf(
                    (x / w) to (top / h),
                    ((x + w * 0.09f) / w) to ((top - h * 0.08f) / h),
                    ((x + w * 0.18f) / w) to (top / h),
                )),
                PaintKit.solid(0xFF6E3B2A.toInt()),
            )
            // Fenster
            canvas.drawRect(x + w * 0.05f, top + h * 0.08f, x + w * 0.09f, top + h * 0.16f, PaintKit.solid(0xFF3A4A55.toInt()))
        }

        // Pflastergasse als Perspektivboden
        PaintKit.perspectiveFloor(
            canvas, w, h, horizonYNorm = 0.72f, bottomYNorm = 1.0f,
            colorNear = 0xFF9A8C78.toInt(), colorFar = 0xFF6E6250.toInt(),
            lineColor = Color.argb(70, 60, 52, 42), vanishXNorm = 0.5f, seams = 10,
        )

        // Fischstand (links) mit Markise
        PaintKit.shadedBox(canvas, w * 0.03f, h * 0.58f, w * 0.16f, h * 0.74f, PaintKit.HOLZ, 0.2f)
        stripedAwning(canvas, w * 0.02f, h * 0.50f, w * 0.17f, h * 0.55f, 0xFF3AA69F.toInt())
        canvas.drawCircle(w * 0.07f, h * 0.60f, h * 0.02f, PaintKit.solid(0xFFC0C8C0.toInt()))
        canvas.drawCircle(w * 0.11f, h * 0.60f, h * 0.02f, PaintKit.solid(0xFFC0C8C0.toInt()))

        // Gewürzkorb & Trankrug
        PaintKit.softShadow(canvas, w * 0.37f, h * 0.82f, h * 0.05f, h * 0.016f, 70)
        canvas.drawCircle(w * 0.37f, h * 0.78f, h * 0.04f, PaintKit.solid(0xFFB05020.toInt()))
        PaintKit.shadedBox(canvas, w * 0.44f, h * 0.72f, w * 0.50f, h * 0.80f, 0xFF8A6A3A.toInt())

        // Schmiedestand (Bo)
        stripedAwning(canvas, w * 0.50f, h * 0.48f, w * 0.68f, h * 0.53f, 0xFFB05020.toInt())
        PaintKit.shadedBox(canvas, w * 0.52f, h * 0.60f, w * 0.66f, h * 0.74f, PaintKit.FELS_DUNKEL, 0.2f)
        PaintKit.shadedBox(canvas, w * 0.69f, h * 0.66f, w * 0.80f, h * 0.80f, PaintKit.HOLZ_DUNKEL) // Trödelkiste

        // Dornenpfad rechts — verschwindet, wenn frei
        if (!GameWorld.dornenFrei(state)) {
            val dorn = PaintKit.solid(PaintKit.LAUB_DUNKEL)
            val dornHi = PaintKit.solid(PaintKit.LAUB)
            for (i in 0..6) {
                val x = w * (0.84f + (i % 3) * 0.05f)
                val y = h * (0.35f + i * 0.06f)
                canvas.drawCircle(x, y, h * 0.05f, dorn)
                canvas.drawCircle(x - h * 0.015f, y - h * 0.015f, h * 0.02f, dornHi)
            }
            // Ziege
            PaintKit.shadedBox(canvas, w * 0.90f, h * 0.62f, w * 0.97f, h * 0.72f, 0xFFD9D2C4.toInt())
            canvas.drawCircle(w * 0.895f, h * 0.62f, h * 0.03f, PaintKit.solid(0xFFD9D2C4.toInt()))
        } else {
            // Freier Pfad in den Dschungel
            PaintKit.ditherRect(canvas, w * 0.86f, h * 0.40f, w, h * 0.72f, PaintKit.LAUB, PaintKit.LAUB_DUNKEL, 8)
        }

        PaintKit.vignette(canvas, w, h, 90)
    }

    private fun stripedAwning(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, color: Int) {
        canvas.drawRect(l, t, r, b, PaintKit.solid(PaintKit.WEISS))
        val stripe = PaintKit.solid(color)
        var x = l
        val step = (r - l) / 6f
        var i = 0
        while (x < r) {
            if (i % 2 == 0) canvas.drawRect(x, t, (x + step).coerceAtMost(r), b, stripe)
            x += step; i++
        }
    }
}
