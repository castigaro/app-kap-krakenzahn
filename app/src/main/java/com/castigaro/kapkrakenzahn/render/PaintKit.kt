package com.castigaro.kapkrakenzahn.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.sin

/**
 * Gemeinsame Zeichenwerkzeuge und Farbpalette für den flachen Cartoon-Stil
 * aller Szenen. Alles wird programmatisch als Vektorgrafik gemalt — es gibt
 * keine Bild-Assets.
 */
object PaintKit {

    // Palette (flach, kräftig).
    const val HIMMEL_TAG_OBEN = 0xFF7FC7E8.toInt()
    const val HIMMEL_TAG_UNTEN = 0xFFCDEBF7.toInt()
    const val HIMMEL_ABEND_OBEN = 0xFF2E4A7A.toInt()
    const val HIMMEL_ABEND_UNTEN = 0xFFE59A6A.toInt()
    const val MEER_OBEN = 0xFF1C6E8C.toInt()
    const val MEER_UNTEN = 0xFF0F4A5E.toInt()
    const val SAND = 0xFFE8D2A0.toInt()
    const val FELS = 0xFF6E6152.toInt()
    const val FELS_DUNKEL = 0xFF4A4136.toInt()
    const val HOLZ = 0xFF8A5A34.toInt()
    const val HOLZ_DUNKEL = 0xFF5E3B21.toInt()
    const val LAUB = 0xFF2F7D46.toInt()
    const val LAUB_DUNKEL = 0xFF1E5730.toInt()
    const val GOLD = 0xFFE0A73C.toInt()
    const val WEISS = 0xFFF5F1E6.toInt()
    const val NACHT = 0xFF0A1626.toInt()

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = WEISS
        textAlign = Paint.Align.CENTER
    }

    fun solid(color: Int): Paint = Paint(fill).apply { this.color = color }

    /** Vertikaler Farbverlauf über ein Rechteck (z. B. Himmel, Meer). */
    fun verticalGradient(
        canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float,
        colorTop: Int, colorBottom: Int,
    ) {
        val p = Paint()
        p.shader = LinearGradient(0f, top, 0f, bottom, colorTop, colorBottom, Shader.TileMode.CLAMP)
        canvas.drawRect(left, top, right, bottom, p)
    }

    /** Ein geschlossener Polygonzug aus normierten (x,y)-Punkten. */
    fun polygon(w: Float, h: Float, points: List<Pair<Float, Float>>): Path {
        val path = Path()
        points.forEachIndexed { i, (x, y) ->
            if (i == 0) path.moveTo(x * w, y * h) else path.lineTo(x * w, y * h)
        }
        path.close()
        return path
    }

    /** Eine sanfte Wellenlinie (für Meer/Brandung), animiert über [phase]. */
    fun wave(
        w: Float, yTop: Float, yBottom: Float, amplitude: Float, wavelength: Float, phase: Float,
    ): Path {
        val path = Path()
        path.moveTo(0f, yBottom)
        path.lineTo(0f, yTop)
        var x = 0f
        while (x <= w) {
            val y = yTop + amplitude * sin((x / wavelength + phase) * 2 * Math.PI).toFloat()
            path.lineTo(x, y)
            x += w / 40f
        }
        path.lineTo(w, yBottom)
        path.close()
        return path
    }

    /** Eine schlichte Möwe (zwei Bögen) an normierter Position. */
    fun seagull(canvas: Canvas, cx: Float, cy: Float, size: Float, phase: Float) {
        stroke.color = Color.argb(160, 40, 40, 40)
        stroke.strokeWidth = size * 0.12f
        val flap = size * (0.25f + 0.15f * sin(phase.toDouble()).toFloat())
        val path = Path()
        path.moveTo(cx - size, cy)
        path.quadTo(cx - size / 2, cy - flap, cx, cy)
        path.quadTo(cx + size / 2, cy - flap, cx + size, cy)
        canvas.drawPath(path, stroke)
    }

    /** Wolke aus überlappenden Kreisen. */
    fun cloud(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int = WEISS) {
        val p = solid(color)
        canvas.drawCircle(cx, cy, size * 0.6f, p)
        canvas.drawCircle(cx - size * 0.6f, cy + size * 0.1f, size * 0.45f, p)
        canvas.drawCircle(cx + size * 0.6f, cy + size * 0.1f, size * 0.45f, p)
        canvas.drawRect(cx - size * 0.6f, cy + size * 0.1f, cx + size * 0.6f, cy + size * 0.55f, p)
    }

    /** Eine einfache stehende NPC-Silhouette (Kopf + Umhang) an Boden-Position. */
    fun figure(
        canvas: Canvas, w: Float, h: Float,
        xNorm: Float, groundY: Float, heightNorm: Float,
        bodyColor: Int, headColor: Int = 0xFFE8C9A0.toInt(),
    ) {
        val cx = xNorm * w
        val top = (groundY - heightNorm) * h
        val bottom = groundY * h
        val bodyW = heightNorm * h * 0.42f
        val body = Path().apply {
            moveTo(cx - bodyW * 0.35f, top + heightNorm * h * 0.28f)
            lineTo(cx - bodyW * 0.55f, bottom)
            lineTo(cx + bodyW * 0.55f, bottom)
            lineTo(cx + bodyW * 0.35f, top + heightNorm * h * 0.28f)
            close()
        }
        canvas.drawPath(body, solid(bodyColor))
        val headR = heightNorm * h * 0.14f
        canvas.drawCircle(cx, top + headR, headR, solid(headColor))
    }

    fun roundedRect(l: Float, t: Float, r: Float, b: Float, radius: Float): Path =
        Path().apply { addRoundRect(RectF(l, t, r, b), radius, radius, Path.Direction.CW) }
}
