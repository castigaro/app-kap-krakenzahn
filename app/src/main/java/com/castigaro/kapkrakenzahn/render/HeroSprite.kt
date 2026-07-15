package com.castigaro.kapkrakenzahn.render

import android.graphics.Canvas
import android.graphics.Path
import kotlin.math.abs
import kotlin.math.sin

/**
 * Die Heldin Jonna, komplett aus Vektorformen gezeichnet (kein Bild-Asset).
 * Beim Gehen schwingen Arme und Beine über die Laufphase; die Blickrichtung
 * spiegelt die Figur. Eine leichte Skalierung nach unten erzeugt Pseudo-Tiefe.
 */
object HeroSprite {

    private const val HAUT = 0xFFE8C9A0.toInt()
    private const val HAAR = 0xFF7A3B1E.toInt()
    private const val MANTEL = 0xFF9C2B2B.toInt()
    private const val MANTEL_HELL = 0xFFC24242.toInt()
    private const val MANTEL_DUNKEL = 0xFF6E1D1D.toInt()
    private const val HOSE = 0xFF2E3550.toInt()
    private const val STIEFEL = 0xFF2A1C12.toInt()
    private const val HUT = 0xFF3A2418.toInt()

    /** Dunkle Kontur für die Körperformen (Strichbreite je Figur gesetzt). */
    private val outline = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeJoin = android.graphics.Paint.Join.ROUND
        color = 0xFF1A0E08.toInt()
    }

    /**
     * Zeichnet Jonna an normierter x-Position auf der Boden-Linie [groundY].
     * [heightNorm] ist die Figurhöhe als Bruchteil der View-Höhe, [phase]
     * treibt die Gehanimation (0 = Stillstand), [facingRight] die Blickseite.
     */
    fun draw(
        canvas: Canvas, w: Float, h: Float,
        xNorm: Float, groundY: Float, heightNorm: Float,
        phase: Float, facingRight: Boolean, walking: Boolean,
    ) {
        val cx = xNorm * w
        val figH = heightNorm * h
        val bottom = groundY * h

        // Weicher Kontaktschatten am Boden — verankert die Figur im Raum.
        val shadowSquash = if (walking) 0.9f + 0.1f * abs(sin(phase.toDouble())).toFloat() else 1f
        PaintKit.softShadow(canvas, cx, bottom, figH * 0.22f * shadowSquash, figH * 0.05f, alpha = 90)

        canvas.save()
        canvas.translate(cx, bottom)
        if (!facingRight) canvas.scale(-1f, 1f)

        val swing = if (walking) sin(phase.toDouble()).toFloat() else 0f
        val bob = if (walking) abs(sin(phase.toDouble())).toFloat() * figH * 0.02f else 0f
        canvas.translate(0f, -bob)

        // Dünne dunkle Kontur für alle Körperformen (Pixel-Look).
        outline.strokeWidth = figH * 0.03f

        val legW = figH * 0.12f
        val legTop = -figH * 0.42f
        // Hinteres Bein
        leg(canvas, -swing * figH * 0.10f, legTop, legW, STIEFEL)
        // Vorderes Bein
        leg(canvas, swing * figH * 0.10f, legTop, legW, STIEFEL)

        // Mantel/Rumpf
        val body = Path().apply {
            moveTo(-figH * 0.16f, -figH * 0.86f)
            lineTo(-figH * 0.20f, -figH * 0.40f)
            lineTo(figH * 0.20f, -figH * 0.40f)
            lineTo(figH * 0.16f, -figH * 0.86f)
            close()
        }
        canvas.drawPath(body, PaintKit.solid(MANTEL))
        // Lichtseite (vorne links) als schmaler Streifen.
        val licht = Path().apply {
            moveTo(-figH * 0.16f, -figH * 0.86f)
            lineTo(-figH * 0.20f, -figH * 0.40f)
            lineTo(-figH * 0.10f, -figH * 0.40f)
            lineTo(-figH * 0.08f, -figH * 0.84f)
            close()
        }
        canvas.drawPath(licht, PaintKit.solid(MANTEL_HELL))
        // Mantelschoß-Schatten
        val schoss = Path().apply {
            moveTo(figH * 0.04f, -figH * 0.40f)
            lineTo(figH * 0.20f, -figH * 0.40f)
            lineTo(figH * 0.16f, -figH * 0.86f)
            close()
        }
        canvas.drawPath(schoss, PaintKit.solid(MANTEL_DUNKEL))
        canvas.drawPath(body, outline)

        // Gürtel
        canvas.drawRect(-figH * 0.20f, -figH * 0.44f, figH * 0.20f, -figH * 0.40f, PaintKit.solid(HUT))
        canvas.drawRect(-figH * 0.03f, -figH * 0.45f, figH * 0.03f, -figH * 0.39f, PaintKit.solid(PaintKit.GOLD))

        // Vorderer Arm (schwingt)
        arm(canvas, swing * figH * 0.14f, -figH * 0.80f, figH, MANTEL, HAUT)

        // Kopf
        val headR = figH * 0.11f
        val headY = -figH * 0.86f - headR
        canvas.drawCircle(0f, headY, headR, PaintKit.solid(HAUT))
        canvas.drawCircle(0f, headY, headR, outline)
        // Haar
        canvas.drawCircle(-headR * 0.2f, headY - headR * 0.2f, headR * 0.9f, PaintKit.solid(HAAR))
        canvas.drawCircle(0f, headY + headR * 0.05f, headR, PaintKit.solid(HAUT))
        // Auge
        canvas.drawCircle(headR * 0.35f, headY, headR * 0.12f, PaintKit.solid(0xFF20140C.toInt()))

        // Dreispitz-Hut
        val hut = Path().apply {
            moveTo(-headR * 1.5f, headY - headR * 0.6f)
            quadTo(0f, headY - headR * 2.0f, headR * 1.5f, headY - headR * 0.6f)
            quadTo(0f, headY - headR * 0.3f, -headR * 1.5f, headY - headR * 0.6f)
            close()
        }
        canvas.drawPath(hut, PaintKit.solid(HUT))

        canvas.restore()
    }

    private fun leg(canvas: Canvas, dx: Float, top: Float, width: Float, color: Int) {
        val path = Path().apply {
            moveTo(dx - width / 2, top)
            lineTo(dx - width / 2 + dx * 0.3f, 0f)
            lineTo(dx + width / 2 + dx * 0.3f, 0f)
            lineTo(dx + width / 2, top)
            close()
        }
        canvas.drawPath(path, PaintKit.solid(HOSE))
        // Stiefel
        canvas.drawRect(dx - width / 2 + dx * 0.3f, -width * 0.6f, dx + width * 0.9f + dx * 0.3f, 0f, PaintKit.solid(color))
    }

    private fun arm(canvas: Canvas, dx: Float, shoulderY: Float, figH: Float, sleeve: Int, hand: Int) {
        val armW = figH * 0.08f
        val path = Path().apply {
            moveTo(figH * 0.14f, shoulderY)
            lineTo(figH * 0.14f + dx, shoulderY + figH * 0.34f)
            lineTo(figH * 0.14f + dx + armW, shoulderY + figH * 0.34f)
            lineTo(figH * 0.14f + armW, shoulderY)
            close()
        }
        canvas.drawPath(path, PaintKit.solid(sleeve))
        canvas.drawCircle(figH * 0.14f + dx + armW / 2, shoulderY + figH * 0.36f, armW * 0.6f, PaintKit.solid(hand))
    }
}
