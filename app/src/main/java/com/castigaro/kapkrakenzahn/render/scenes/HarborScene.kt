package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import android.graphics.Color
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter

/** Der Hafen: Himmel, Meer mit Anleger, Ladekran, Fässer, Papagei-Stange. */
object HarborScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        val phase = timeMs / 1000f

        // Himmel + Morgensonne
        PaintKit.ditherGradient(canvas, 0f, 0f, w, h * 0.60f, PaintKit.HIMMEL_TAG_OBEN, PaintKit.HIMMEL_TAG_UNTEN, 6)
        PaintKit.radialGlow(canvas, w * 0.82f, h * 0.12f, w * 0.24f, Color.argb(120, 255, 240, 190))

        PaintKit.cloud(canvas, w * 0.22f, h * 0.16f, h * 0.05f)
        PaintKit.cloud(canvas, w * 0.7f, h * 0.1f, h * 0.06f)
        PaintKit.seagull(canvas, w * 0.4f, h * 0.14f, h * 0.03f, phase * 3)
        PaintKit.seagull(canvas, w * 0.52f, h * 0.2f, h * 0.025f, phase * 3 + 1)

        // Meer
        PaintKit.ditherGradient(canvas, 0f, h * 0.56f, w, h * 0.76f, PaintKit.MEER_OBEN, PaintKit.MEER_UNTEN, 5)
        canvas.drawPath(PaintKit.wave(w, h * 0.60f, h * 0.76f, h * 0.008f, w * 0.25f, phase), PaintKit.solid(0x33FFFFFF))

        // Kai als Perspektivboden (Holzdielen fliehen zum Horizont)
        PaintKit.perspectiveFloor(
            canvas, w, h, horizonYNorm = 0.72f, bottomYNorm = 1.0f,
            colorNear = PaintKit.HOLZ, colorFar = PaintKit.HOLZ_DUNKEL,
            lineColor = Color.argb(90, 40, 24, 12), vanishXNorm = 0.5f, seams = 11,
        )

        // Anleger links mit Fähre
        canvas.drawRect(w * 0.02f, h * 0.70f, w * 0.28f, h * 0.75f, PaintKit.solid(PaintKit.HOLZ_DUNKEL))
        PaintKit.softShadow(canvas, w * 0.17f, h * 0.73f, w * 0.11f, h * 0.02f, 90)
        PaintKit.shadedBox(canvas, w * 0.10f, h * 0.60f, w * 0.24f, h * 0.72f, 0xFF7A4A2A.toInt()) // Boot
        canvas.drawRect(w * 0.16f, h * 0.46f, w * 0.175f, h * 0.62f, PaintKit.solid(PaintKit.HOLZ_DUNKEL)) // Mast

        // Papageienstange
        canvas.drawRect(w * 0.335f, h * 0.30f, w * 0.35f, h * 0.74f, PaintKit.solid(PaintKit.HOLZ_DUNKEL))
        canvas.drawCircle(w * 0.35f, h * 0.37f, h * 0.05f, PaintKit.solid(0xFF3AA69F.toInt())) // Papagei-Körper
        canvas.drawCircle(w * 0.37f, h * 0.34f, h * 0.03f, PaintKit.solid(0xFFE0A73C.toInt())) // Kopf

        // Ladekran
        PaintKit.shadedBox(canvas, w * 0.44f, h * 0.18f, w * 0.47f, h * 0.74f, PaintKit.FELS_DUNKEL, 0.4f)
        canvas.drawRect(w * 0.44f, h * 0.18f, w * 0.60f, h * 0.21f, PaintKit.solid(PaintKit.FELS_DUNKEL))
        PaintKit.stroke.color = PaintKit.FELS_DUNKEL
        PaintKit.stroke.strokeWidth = h * 0.006f
        canvas.drawLine(w * 0.58f, h * 0.21f, w * 0.58f, h * 0.40f, PaintKit.stroke)

        // Tauwerk, Teerfass, Eimer (mit Bodenschatten)
        PaintKit.softShadow(canvas, w * 0.65f, h * 0.84f, h * 0.06f, h * 0.02f, 80)
        canvas.drawCircle(w * 0.65f, h * 0.80f, h * 0.05f, PaintKit.solid(0xFFB98A5A.toInt()))
        canvas.drawCircle(w * 0.65f, h * 0.80f, h * 0.025f, PaintKit.solid(PaintKit.HOLZ_DUNKEL))
        PaintKit.softShadow(canvas, w * 0.77f, h * 0.86f, h * 0.06f, h * 0.02f, 80)
        PaintKit.shadedBox(canvas, w * 0.72f, h * 0.72f, w * 0.82f, h * 0.85f, 0xFF2A2A30.toInt(), 0.2f) // Teerfass
        PaintKit.shadedBox(canvas, w * 0.85f, h * 0.79f, w * 0.92f, h * 0.85f, PaintKit.HOLZ) // Eimer

        PaintKit.vignette(canvas, w, h, 95)
    }
}
