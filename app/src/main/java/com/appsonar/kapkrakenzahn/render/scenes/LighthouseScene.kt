package com.appsonar.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import android.graphics.Color
import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.render.PaintKit
import com.appsonar.kapkrakenzahn.render.ScenePainter
import com.appsonar.kapkrakenzahn.world.FlagId
import kotlin.math.sin

/**
 * Der Leuchtturm. Außen bei geschlossener Tür (Tür + loser Stein), nach dem
 * Öffnen der Innen-/Lampenraum mit Fassung und Reflektor. Brennt am Ende das
 * Feuer, strahlt ein Lichtkegel über die Bucht.
 */
object LighthouseScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        val phase = timeMs / 1000f
        val offen = state.has(FlagId.LEUCHTTURM_OFFEN)
        val brennt = state.has(FlagId.FEUER_ENTZUENDET)

        if (!offen) {
            // Abendhimmel
            PaintKit.ditherGradient(canvas, 0f, 0f, w, h * 0.6f, PaintKit.HIMMEL_ABEND_OBEN, PaintKit.HIMMEL_ABEND_UNTEN, 6)
            PaintKit.ditherGradient(canvas, 0f, h * 0.58f, w, h, PaintKit.MEER_UNTEN, PaintKit.NACHT, 5)
            // Boden am Turmfuß
            PaintKit.perspectiveFloor(
                canvas, w, h, horizonYNorm = 0.84f, bottomYNorm = 1.0f,
                colorNear = PaintKit.FELS, colorFar = PaintKit.FELS_DUNKEL,
                lineColor = Color.argb(50, 20, 20, 24), vanishXNorm = 0.5f, seams = 8,
            )

            // Aussenansicht: Turm mittig
            drawTower(canvas, w, h, brennt, phase)
            // Tür
            PaintKit.shadedBox(canvas, w * 0.40f, h * 0.40f, w * 0.56f, h * 0.88f, PaintKit.HOLZ_DUNKEL, 0.14f)
            canvas.drawCircle(w * 0.53f, h * 0.66f, h * 0.015f, PaintKit.solid(PaintKit.GOLD))
            // Loser Stein
            PaintKit.softShadow(canvas, w * 0.65f, h * 0.85f, h * 0.045f, h * 0.015f, 80)
            canvas.drawCircle(w * 0.65f, h * 0.82f, h * 0.03f, PaintKit.solid(PaintKit.FELS))
            canvas.drawCircle(w * 0.60f, h * 0.85f, h * 0.025f, PaintKit.solid(PaintKit.FELS_DUNKEL))
        } else {
            // Innen-/Lampenraum
            PaintKit.ditherGradient(canvas, 0f, 0f, w, h, 0xFF3A3A48.toInt(), 0xFF1A1A22.toInt(), 6)
            // Runde Wand / Boden
            canvas.drawRect(0f, h * 0.85f, w, h, PaintKit.solid(0xFF2A2A34.toInt()))
            // Fassung des Leuchtfeuers (mittig)
            val augeDrin = state.has(FlagId.AUGE_EINGESETZT)
            PaintKit.shadedBox(canvas, w * 0.42f, h * 0.30f, w * 0.58f, h * 0.80f, PaintKit.FELS_DUNKEL, 0.18f)
            val glow = 0.6f + 0.4f * sin(phase * 4.0).toFloat()
            val augeFarbe = when {
                brennt -> Color.argb(255, 255, 240, 180)
                augeDrin -> Color.argb((200 * glow).toInt(), 60, 220, 210)
                else -> 0xFF20303A.toInt()
            }
            if (brennt) PaintKit.radialGlow(canvas, w * 0.50f, h * 0.42f, h * 0.30f, Color.argb(160, 255, 235, 160))
            else if (augeDrin) PaintKit.radialGlow(canvas, w * 0.50f, h * 0.42f, h * 0.16f, Color.argb((120 * glow).toInt(), 60, 220, 210))
            canvas.drawCircle(w * 0.50f, h * 0.42f, h * 0.06f, PaintKit.solid(augeFarbe))
            // Reflektor rechts
            val poliert = state.has(FlagId.SPIEGEL_POLIERT)
            canvas.drawOval(
                w * 0.62f, h * 0.28f, w * 0.82f, h * 0.60f,
                PaintKit.solid(if (poliert) 0xFFD8E4E0.toInt() else 0xFF6A6A60.toInt()),
            )

            if (brennt) {
                // Lichtkegel hinaus
                val cone = PaintKit.solid(Color.argb(120, 255, 240, 170))
                canvas.drawPath(
                    PaintKit.polygon(w, h, listOf(0.50f to 0.42f, 1.0f to 0.05f, 1.0f to 0.40f)),
                    cone,
                )
            }
            PaintKit.vignette(canvas, w, h, 140)
        }
    }

    private fun drawTower(canvas: Canvas, w: Float, h: Float, brennt: Boolean, phase: Float) {
        // Turmkörper
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(0.40f to 0.88f, 0.44f to 0.20f, 0.56f to 0.20f, 0.60f to 0.88f)),
            PaintKit.solid(PaintKit.WEISS),
        )
        // Schattenseite rechts
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(0.52f to 0.20f, 0.56f to 0.20f, 0.60f to 0.88f, 0.54f to 0.88f)),
            PaintKit.solid(0xFFCFC8B8.toInt()),
        )
        // Rote Streifen
        for (y in listOf(0.34f, 0.52f, 0.70f)) {
            canvas.drawRect(w * 0.41f, h * y, w * 0.59f, h * (y + 0.06f), PaintKit.solid(0xFFB84A3A.toInt()))
        }
        // Laternenraum
        if (brennt) PaintKit.radialGlow(canvas, w * 0.50f, h * 0.15f, h * 0.22f, Color.argb(150, 255, 235, 160))
        canvas.drawRect(w * 0.43f, h * 0.10f, w * 0.57f, h * 0.20f, PaintKit.solid(PaintKit.GOLD))
        canvas.drawRect(w * 0.42f, h * 0.08f, w * 0.58f, h * 0.11f, PaintKit.solid(PaintKit.FELS_DUNKEL))
    }
}
