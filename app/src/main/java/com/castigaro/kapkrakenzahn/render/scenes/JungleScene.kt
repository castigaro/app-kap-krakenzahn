package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter
import com.castigaro.kapkrakenzahn.world.GameWorld
import kotlin.math.sin

/**
 * Der Dschungelpfad in Tiefe: ein dunstiger Hintergrund, mehrere Ebenen
 * Blattwerk (hinten hell/verwaschen → vorne dunkel/dicht), Baumstämme mit
 * Licht- und Schattenseite, ein zum Höhlenschlund fliehender Erdpfad und ein
 * rahmender Vordergrund aus Farnen. Stilreferenz für die übrigen Szenen.
 */
object JungleScene : ScenePainter {

    private const val TRUNK = 0xFF5A3B22.toInt()
    private const val TRUNK_LIGHT = 0xFF7A5230.toInt()
    private const val TRUNK_DARK = 0xFF3A2614.toInt()

    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        val sway = sin(timeMs / 1400.0).toFloat()

        // Dunstiger Tiefen-Hintergrund (fernes, lichtdurchflutetes Blätterdach).
        PaintKit.ditherGradient(canvas, 0f, 0f, w, h * 0.66f, PaintKit.DUNST_GRUEN, PaintKit.LAUB_DUNKEL, steps = 7)

        // Schräge Lichtschäfte durch das Blätterdach.
        lightShafts(canvas, w, h)

        // Blätterdach in Ebenen: hinten hell & verwaschen, vorne dunkel & dicht.
        canopyRow(canvas, w, h, top = 0.06f, r = 0.085f, step = 0.11f, color = PaintKit.lerpColor(PaintKit.LAUB_HELL, PaintKit.DUNST_GRUEN, 0.5f), sway = sway * 0.4f, phase = 0f)
        canopyRow(canvas, w, h, top = 0.14f, r = 0.11f, step = 0.14f, color = PaintKit.LAUB, sway = sway * 0.7f, phase = 1.3f)
        canopyRow(canvas, w, h, top = 0.24f, r = 0.13f, step = 0.17f, color = PaintKit.LAUB_DUNKEL, sway = sway, phase = 2.6f)

        // Baumstämme mit leichter Perspektive (vorne dicker).
        tree(canvas, w, h, x = 0.12f, width = 0.045f, top = 0.16f, bottom = 0.72f)
        tree(canvas, w, h, x = 0.40f, width = 0.05f, top = 0.14f, bottom = 0.74f)
        tree(canvas, w, h, x = 0.63f, width = 0.038f, top = 0.20f, bottom = 0.70f)

        // Erdpfad, der zum Höhlenschlund rechts hin flieht.
        PaintKit.perspectiveFloor(
            canvas, w, h,
            horizonYNorm = 0.62f, bottomYNorm = 1.0f,
            colorNear = PaintKit.ERDE_HELL, colorFar = PaintKit.ERDE_DUNKEL,
            lineColor = Color.argb(70, 40, 28, 12),
            vanishXNorm = 0.82f, seams = 9,
        )
        // Moosige Ränder am Pfad.
        PaintKit.ditherRect(canvas, 0f, h * 0.62f, w, h * 0.70f, PaintKit.LAUB_DUNKEL, PaintKit.ERDE_DUNKEL, 8)

        // Höhlenschlund rechts (rahmender Fels + dunkler Eingang).
        caveMouth(canvas, w, h, state)

        // Rahmender Vordergrund: dunkle Farne/Büsche links und rechts unten
        // (mit dem Medaillon-Versteck im linken Busch).
        foregroundFerns(canvas, w, h, sway)

        // Abschließende Vignette für Tiefe und Blickführung.
        PaintKit.vignette(canvas, w, h, strength = 110)
    }

    /** Eine Reihe überlappender Blattkronen, sanft im Wind wiegend. */
    private fun canopyRow(
        canvas: Canvas, w: Float, h: Float,
        top: Float, r: Float, step: Float, color: Int, sway: Float, phase: Float,
    ) {
        val p = PaintKit.solid(color)
        val shade = PaintKit.solid(PaintKit.darken(color, 0.82f))
        var x = -0.05f
        var i = 0
        while (x < 1.08f) {
            val cx = w * (x + sway * 0.01f)
            val cy = h * (top + 0.012f * sin((i + phase).toDouble()).toFloat())
            canvas.drawCircle(cx, cy + h * 0.02f, h * r, shade)
            canvas.drawCircle(cx, cy, h * r, p)
            x += step
            i++
        }
        canvas.drawRect(0f, h * top, w, h * (top + r * 0.7f), p)
    }

    /** Ein Baumstamm mit Licht-/Schattenseite und angedeuteter Rinde. */
    private fun tree(canvas: Canvas, w: Float, h: Float, x: Float, width: Float, top: Float, bottom: Float) {
        val l = w * x
        val rgt = w * (x + width)
        val t = h * top
        val b = h * bottom
        canvas.drawRect(l, t, rgt, b, PaintKit.solid(TRUNK))
        // Lichtseite links, Schattenseite rechts.
        canvas.drawRect(l, t, l + (rgt - l) * 0.30f, b, PaintKit.solid(TRUNK_LIGHT))
        canvas.drawRect(rgt - (rgt - l) * 0.28f, t, rgt, b, PaintKit.solid(TRUNK_DARK))
        // Ein paar Rindenkerben.
        PaintKit.stroke.color = TRUNK_DARK
        PaintKit.stroke.strokeWidth = h * 0.004f
        var y = t + h * 0.06f
        while (y < b) {
            canvas.drawLine(l + (rgt - l) * 0.4f, y, l + (rgt - l) * 0.6f, y + h * 0.02f, PaintKit.stroke)
            y += h * 0.09f
        }
    }

    /** Der Fels mit dunklem Höhleneingang rechts; bei Flut steht Wasser darin. */
    private fun caveMouth(canvas: Canvas, w: Float, h: Float, state: GameState) {
        // Felswand.
        PaintKit.ditherRect(canvas, w * 0.76f, 0f, w, h, PaintKit.FELS, PaintKit.FELS_DUNKEL, 9)
        canvas.drawRect(w * 0.76f, 0f, w * 0.785f, h, PaintKit.solid(PaintKit.darken(PaintKit.FELS_DUNKEL, 0.8f)))

        // Dunkler Schlund.
        val mouth = Path().apply {
            addOval(w * 0.80f, h * 0.26f, w * 0.995f, h * 0.94f, Path.Direction.CW)
        }
        canvas.drawPath(mouth, PaintKit.solid(PaintKit.NACHT))
        // Schwacher, kühler Schimmer aus der Tiefe (Krakenhöhle).
        PaintKit.radialGlow(canvas, w * 0.90f, h * 0.55f, w * 0.09f, Color.argb(70, 40, 120, 130))

        if (!GameWorld.hoehleBegehbar(state)) {
            // Flutwasser im Schlund.
            canvas.save()
            canvas.clipPath(mouth)
            PaintKit.ditherRect(canvas, w * 0.80f, h * 0.60f, w * 0.995f, h * 0.94f, PaintKit.MEER_OBEN, PaintKit.MEER_UNTEN, 8)
            canvas.restore()
        }
    }

    /** Dunkle Farne links/rechts als Vordergrundrahmen. */
    private fun foregroundFerns(canvas: Canvas, w: Float, h: Float, sway: Float) {
        // Linker Busch (Medaillon-Versteck).
        bush(canvas, w * (0.14f + sway * 0.004f), h * 0.90f, h * 0.13f, PaintKit.LAUB_TIEF)
        bush(canvas, w * (0.22f + sway * 0.006f), h * 0.94f, h * 0.10f, PaintKit.LAUB_DUNKEL)
        // Rechter Farn (kleiner, näher).
        bush(canvas, w * (0.70f - sway * 0.004f), h * 0.97f, h * 0.11f, PaintKit.LAUB_TIEF)
    }

    private fun bush(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val p = PaintKit.solid(color)
        canvas.drawCircle(cx, cy, r, p)
        canvas.drawCircle(cx - r * 0.7f, cy + r * 0.15f, r * 0.7f, p)
        canvas.drawCircle(cx + r * 0.7f, cy + r * 0.15f, r * 0.7f, p)
        // Aufgehellte Blattspitzen.
        val hi = PaintKit.solid(PaintKit.lighten(color, 34))
        canvas.drawCircle(cx - r * 0.3f, cy - r * 0.5f, r * 0.22f, hi)
        canvas.drawCircle(cx + r * 0.4f, cy - r * 0.35f, r * 0.18f, hi)
    }

    /** Ein paar diagonale, weiche Lichtschäfte aus dem Blätterdach. */
    private fun lightShafts(canvas: Canvas, w: Float, h: Float) {
        val p = Paint().apply { color = Color.argb(26, 230, 240, 180) }
        for (i in 0..2) {
            val x = w * (0.2f + i * 0.28f)
            val shaft = Path().apply {
                moveTo(x, 0f)
                lineTo(x + w * 0.08f, 0f)
                lineTo(x - w * 0.06f, h * 0.66f)
                lineTo(x - w * 0.16f, h * 0.66f)
                close()
            }
            canvas.drawPath(shaft, p)
        }
    }
}
