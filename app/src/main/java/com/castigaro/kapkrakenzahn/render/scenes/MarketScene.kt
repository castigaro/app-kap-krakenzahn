package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter
import com.castigaro.kapkrakenzahn.world.GameWorld

/** Die Marktgasse: Häuserzeile, Fischstand, Schmiedestand, rechts der Dornenpfad. */
object MarketScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        PaintKit.verticalGradient(canvas, 0f, 0f, w, h * 0.55f, PaintKit.HIMMEL_TAG_OBEN, PaintKit.HIMMEL_TAG_UNTEN)
        PaintKit.cloud(canvas, w * 0.35f, h * 0.14f, h * 0.05f)

        // Pflastergasse
        canvas.drawRect(0f, h * 0.72f, w, h, PaintKit.solid(0xFF9A8C78.toInt()))
        canvas.drawRect(0f, h * 0.72f, w, h * 0.74f, PaintKit.solid(0xFF7A6E5C.toInt()))

        // Häuserzeile hinten
        val farben = listOf(0xFFCFA97A.toInt(), 0xFFB98A5A.toInt(), 0xFFD9C0A0.toInt(), 0xFFA9754A.toInt())
        for (i in 0..3) {
            val x = w * (0.02f + i * 0.20f)
            val top = h * (0.30f + (i % 2) * 0.05f)
            canvas.drawRect(x, top, x + w * 0.18f, h * 0.72f, PaintKit.solid(farben[i]))
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

        // Fischstand (links) mit Markise
        canvas.drawRect(w * 0.03f, h * 0.58f, w * 0.16f, h * 0.74f, PaintKit.solid(PaintKit.HOLZ))
        stripedAwning(canvas, w * 0.02f, h * 0.50f, w * 0.17f, h * 0.55f, 0xFF3AA69F.toInt())
        canvas.drawCircle(w * 0.07f, h * 0.60f, h * 0.02f, PaintKit.solid(0xFFC0C8C0.toInt()))
        canvas.drawCircle(w * 0.11f, h * 0.60f, h * 0.02f, PaintKit.solid(0xFFC0C8C0.toInt()))

        // Gewürzkorb & Trankrug
        canvas.drawCircle(w * 0.37f, h * 0.78f, h * 0.04f, PaintKit.solid(0xFFB05020.toInt()))
        canvas.drawRect(w * 0.44f, h * 0.72f, w * 0.50f, h * 0.80f, PaintKit.solid(0xFF8A6A3A.toInt()))

        // Schmiedestand (Bo)
        stripedAwning(canvas, w * 0.50f, h * 0.48f, w * 0.68f, h * 0.53f, 0xFFB05020.toInt())
        canvas.drawRect(w * 0.52f, h * 0.60f, w * 0.66f, h * 0.74f, PaintKit.solid(PaintKit.FELS_DUNKEL))
        canvas.drawRect(w * 0.69f, h * 0.66f, w * 0.80f, h * 0.80f, PaintKit.solid(PaintKit.HOLZ_DUNKEL)) // Trödelkiste

        // Dornenpfad rechts — verschwindet, wenn frei
        if (!GameWorld.dornenFrei(state)) {
            val dorn = PaintKit.solid(PaintKit.LAUB_DUNKEL)
            for (i in 0..6) {
                val x = w * (0.84f + (i % 3) * 0.05f)
                val y = h * (0.35f + i * 0.06f)
                canvas.drawCircle(x, y, h * 0.05f, dorn)
            }
            // Ziege
            canvas.drawRect(w * 0.90f, h * 0.62f, w * 0.97f, h * 0.72f, PaintKit.solid(0xFFD9D2C4.toInt()))
            canvas.drawCircle(w * 0.895f, h * 0.62f, h * 0.03f, PaintKit.solid(0xFFD9D2C4.toInt()))
        } else {
            // Freier Pfad in den Dschungel
            canvas.drawRect(w * 0.86f, h * 0.40f, w, h * 0.72f, PaintKit.solid(PaintKit.LAUB))
        }
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
