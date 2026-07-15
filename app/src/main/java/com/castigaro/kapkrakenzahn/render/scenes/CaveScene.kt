package com.castigaro.kapkrakenzahn.render.scenes

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RadialGradient
import android.graphics.Shader
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.PaintKit
import com.castigaro.kapkrakenzahn.render.ScenePainter
import com.castigaro.kapkrakenzahn.world.GameWorld
import kotlin.math.sin

/**
 * Die Krakenhöhle. Ohne Lichtquelle fast schwarz (nur ein schwacher
 * Schimmer um die Heldin); mit Licht werden Wächterstein, Opferbecken und
 * das schwarze Wasser sichtbar.
 */
object CaveScene : ScenePainter {
    override fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long) {
        val phase = timeMs / 1000f
        val licht = GameWorld.hasLight(state)

        // Grundton
        canvas.drawRect(0f, 0f, w, h, PaintKit.solid(PaintKit.NACHT))

        if (licht) {
            // Höhlenwände
            PaintKit.ditherGradient(canvas, 0f, 0f, w, h, 0xFF243642.toInt(), 0xFF101A22.toInt(), 6)
            // Stalaktiten
            for (x in listOf(0.15f, 0.35f, 0.55f, 0.75f)) {
                canvas.drawPath(
                    PaintKit.polygon(w, h, listOf(
                        x to 0f, (x + 0.03f) to 0f, (x + 0.015f) to 0.18f,
                    )),
                    PaintKit.solid(0xFF1A2830.toInt()),
                )
            }
            // Boden
            canvas.drawRect(0f, h * 0.80f, w, h, PaintKit.solid(0xFF16242C.toInt()))
            canvas.drawRect(0f, h * 0.80f, w, h * 0.815f, PaintKit.solid(0xFF20323A.toInt()))

            // Opferbecken links (mit Schimmer)
            canvas.drawOval(w * 0.18f, h * 0.78f, w * 0.36f, h * 0.90f, PaintKit.solid(0xFF0E3038.toInt()))
            canvas.drawOval(w * 0.21f, h * 0.80f, w * 0.33f, h * 0.87f, PaintKit.solid(0xFF1C6E6A.toInt()))
            PaintKit.radialGlow(canvas, w * 0.27f, h * 0.835f, w * 0.09f, Color.argb(60, 60, 200, 190))

            // Wächterstein (Monolith mit Tentakeln + leere/gefüllte Fassung)
            PaintKit.shadedBox(canvas, w * 0.60f, h * 0.25f, w * 0.75f, h * 0.85f, 0xFF2A3A42.toInt(), 0.2f)
            tentacle(canvas, w * 0.60f, h * 0.55f, -1f, h)
            tentacle(canvas, w * 0.75f, h * 0.60f, 1f, h)
            val hatAuge = state.has(com.castigaro.kapkrakenzahn.world.FlagId.KRAKENAUGE_ERHALTEN)
            val fassungGlow = if (hatAuge) 0 else (150 * (0.6f + 0.4f * sin(phase.toDouble()).toFloat())).toInt()
            if (!hatAuge) PaintKit.radialGlow(canvas, w * 0.675f, h * 0.45f, h * 0.10f, Color.argb(fassungGlow, 40, 200, 190))
            canvas.drawCircle(
                w * 0.675f, h * 0.45f, h * 0.04f,
                PaintKit.solid(if (hatAuge) 0xFF14202A.toInt() else Color.argb(fassungGlow, 60, 220, 210)),
            )

            // Schwarzes Wasser rechts
            canvas.drawRect(w * 0.78f, h * 0.60f, w, h, PaintKit.solid(0xFF060E14.toInt()))
            canvas.drawPath(
                PaintKit.wave(w, h * 0.62f, h, h * 0.01f, w * 0.15f, phase),
                PaintKit.solid(0x22FFFFFF),
            )
        }

        // Schimmer / Lichtkegel um die Heldin (folgt heroX/heroY)
        val cx = state.heroX * w
        val cy = state.heroY * h
        val radius = if (licht) h * 0.55f else h * 0.22f
        val alpha = if (licht) 90 else 60
        val p = android.graphics.Paint()
        p.shader = RadialGradient(
            cx, cy, radius,
            Color.argb(alpha, 255, 225, 150), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy, radius, p)

        if (licht) PaintKit.vignette(canvas, w, h, 150)
    }

    private fun tentacle(canvas: Canvas, x: Float, y: Float, dir: Float, h: Float) {
        PaintKit.stroke.color = 0xFF3A5A54.toInt()
        PaintKit.stroke.strokeWidth = h * 0.02f
        val path = android.graphics.Path().apply {
            moveTo(x, y)
            quadTo(x + dir * h * 0.12f, y + h * 0.05f, x + dir * h * 0.06f, y + h * 0.18f)
        }
        canvas.drawPath(path, PaintKit.stroke)
    }
}
