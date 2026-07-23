package com.appsonar.kapkrakenzahn.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.max
import kotlin.math.sin

/**
 * Gemeinsame Zeichenwerkzeuge und Farbpalette für alle Szenen. Alles wird
 * programmatisch als Vektorgrafik in einen niedrig aufgelösten Puffer gemalt
 * (siehe [SceneView]) und mit Nearest-Neighbor hochskaliert — das erzeugt den
 * knackigen Pixel-Look der 90er-Klassiker, ganz ohne Bild-Assets. Neben den
 * flachen Grundformen bieten die Helfer hier Tiefe (Perspektivboden,
 * atmosphärische Verläufe), Pixeltextur (geordnetes Dithering), Lichtstimmung
 * (Glow, Vignette) und weiche Schatten.
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

    // Erweiterte Zwischentöne für Schattierung/Tiefe.
    const val LAUB_HELL = 0xFF6FB25A.toInt()
    const val LAUB_TIEF = 0xFF12401F.toInt()
    const val DUNST_GRUEN = 0xFFB8D6A0.toInt()   // ferner Dschungeldunst
    const val ERDE = 0xFF6E5A38.toInt()
    const val ERDE_DUNKEL = 0xFF4A3B22.toInt()
    const val ERDE_HELL = 0xFF8A7048.toInt()
    const val SCHATTEN = 0x66000000            // halbtransparenter Schatten

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

    // ---- Pixeltextur: geordnetes Dithering (Bayer 4×4) ---------------------

    // Bayer-4×4-Schwellen (0..15). Ein Pixel bekommt Farbe B, wenn der
    // Schwellwert < level (0..16). Im hochskalierten Puffer werden daraus
    // saubere Pixelblöcke — der klassische Retro-Farbverlauf.
    private val BAYER = intArrayOf(
        0, 8, 2, 10,
        12, 4, 14, 6,
        3, 11, 1, 9,
        15, 7, 13, 5,
    )
    private val ditherCache = HashMap<String, Paint>()

    /**
     * Ein Paint, das per 4×4-Kachel zwischen [a] und [b] dithert. [level] 0..16
     * steuert den Anteil von [b] (0 = nur a, 16 = nur b). Die Kachel liegt an
     * den Pufferpixeln an, wird also beim Hochskalieren pixelscharf.
     */
    fun ditherPaint(a: Int, b: Int, level: Int): Paint {
        val lv = level.coerceIn(0, 16)
        val key = "$a:$b:$lv"
        ditherCache[key]?.let { return it }
        val tile = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        for (y in 0 until 4) for (x in 0 until 4) {
            tile.setPixel(x, y, if (BAYER[y * 4 + x] < lv) b else a)
        }
        val paint = Paint().apply {
            shader = android.graphics.BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
        ditherCache[key] = paint
        return paint
    }

    /** Füllt ein Rechteck mit einem geditherten Übergang zwischen zwei Farben. */
    fun ditherRect(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, a: Int, c: Int, level: Int) {
        canvas.drawRect(l, t, r, b, ditherPaint(a, c, level))
    }

    /**
     * Ein vertikaler, in mehreren Stufen geditherter Verlauf — weich wirkender
     * Himmel/Wasser im Pixel-Look statt eines glatten Gradienten.
     */
    fun ditherGradient(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, top: Int, bottom: Int, steps: Int = 6) {
        val h = (b - t) / steps
        for (i in 0 until steps) {
            val y0 = t + i * h
            // Zwischen den beiden Stufenfarben ditern, damit die Bänder verschwimmen.
            val f0 = i.toFloat() / steps
            val f1 = (i + 1f) / steps
            canvas.drawRect(l, y0, r, y0 + h + 1f, ditherPaint(lerpColor(top, bottom, f0), lerpColor(top, bottom, f1), 8))
        }
    }

    // ---- Licht & Schatten --------------------------------------------------

    /** Ein weicher Lichtschein (Laterne, Feuer, Tageslicht-Pfütze). */
    fun radialGlow(canvas: Canvas, cx: Float, cy: Float, radius: Float, centerColor: Int) {
        val edge = centerColor and 0x00FFFFFF // gleiche Farbe, Alpha 0
        val p = Paint().apply {
            shader = RadialGradient(cx, cy, radius, centerColor, edge, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(cx, cy, radius, p)
    }

    /** Abdunkelnde Vignette an den Rändern — bündelt den Blick, schafft Tiefe. */
    fun vignette(canvas: Canvas, w: Float, h: Float, strength: Int = 120) {
        val p = Paint().apply {
            shader = RadialGradient(
                w * 0.5f, h * 0.48f, max(w, h) * 0.72f,
                intArrayOf(0x00000000, 0x00000000, (strength shl 24)),
                floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, w, h, p)
    }

    /** Weicher elliptischer Kontaktschatten (unter Figuren/Objekten). */
    fun softShadow(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float, alpha: Int = 90) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, rx, (alpha shl 24), 0x00000000, Shader.TileMode.CLAMP,
            )
        }
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(1f, ry / rx)
        canvas.translate(-cx, -cy)
        canvas.drawCircle(cx, cy, rx, p)
        canvas.restore()
    }

    // ---- Tiefe: Perspektivboden --------------------------------------------

    /**
     * Ein zum Horizont fliehender Boden: gefüllt mit einem Tiefenverlauf
     * (hinten dunkler), darüber Längsfugen, die zum Fluchtpunkt
     * ([vanishXNorm] auf Höhe [horizonYNorm]) zusammenlaufen, plus enger
     * werdende Querlinien. Alle Werte normiert (0..1).
     */
    fun perspectiveFloor(
        canvas: Canvas, w: Float, h: Float,
        horizonYNorm: Float, bottomYNorm: Float,
        colorNear: Int, colorFar: Int, lineColor: Int,
        vanishXNorm: Float = 0.5f, seams: Int = 8,
    ) {
        val top = horizonYNorm * h
        val bot = bottomYNorm * h
        ditherGradient(canvas, 0f, top, w, bot, colorFar, colorNear, steps = 6)

        val vx = vanishXNorm * w
        stroke.color = lineColor
        stroke.strokeWidth = max(1f, h * 0.004f)
        // Längsfugen: gleichmäßig über die Bodenvorderkante verteilt, alle zum
        // Fluchtpunkt.
        for (i in 0..seams) {
            val nx = i.toFloat() / seams * w
            canvas.drawLine(nx, bot, vx, top, stroke)
        }
        // Querlinien: exponentiell dichter Richtung Horizont.
        var t = 0.5f
        var acc = 0.5f
        while (acc < 0.97f) {
            val y = top + (bot - top) * acc
            stroke.alpha = (Color.alpha(lineColor) * (0.4f + 0.6f * acc)).toInt().coerceIn(0, 255)
            canvas.drawLine(0f, y, w, y, stroke)
            t *= 0.62f
            acc += t
        }
        stroke.alpha = 255
    }

    // ---- Farb-Utilities ----------------------------------------------------

    /** Lineare Interpolation zweier ARGB-Farben. */
    fun lerpColor(a: Int, b: Int, f: Float): Int {
        val t = f.coerceIn(0f, 1f)
        val ar = Color.red(a); val ag = Color.green(a); val ab = Color.blue(a); val aa = Color.alpha(a)
        val br = Color.red(b); val bg = Color.green(b); val bb = Color.blue(b); val ba = Color.alpha(b)
        return Color.argb(
            (aa + (ba - aa) * t).toInt(),
            (ar + (br - ar) * t).toInt(),
            (ag + (bg - ag) * t).toInt(),
            (ab + (bb - ab) * t).toInt(),
        )
    }

    /** Farbe abdunkeln (Schattenseite). */
    fun darken(color: Int, factor: Float = 0.7f): Int = Color.argb(
        Color.alpha(color),
        (Color.red(color) * factor).toInt().coerceIn(0, 255),
        (Color.green(color) * factor).toInt().coerceIn(0, 255),
        (Color.blue(color) * factor).toInt().coerceIn(0, 255),
    )

    /** Farbe aufhellen (Lichtseite). */
    fun lighten(color: Int, amount: Int = 40): Int = Color.argb(
        Color.alpha(color),
        (Color.red(color) + amount).coerceIn(0, 255),
        (Color.green(color) + amount).coerceIn(0, 255),
        (Color.blue(color) + amount).coerceIn(0, 255),
    )

    /** Gleiche Farbe mit neuem Alpha (0..255). */
    fun withAlpha(color: Int, alpha: Int): Int =
        (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)

    /** Frisches Stroke-Paint — das geteilte [stroke] wird von den Szenen
     *  mutiert und darf für wiederverwendbare Zeichner nicht benutzt werden. */
    fun line(color: Int, width: Float): Paint = Paint(stroke).apply {
        this.color = color
        strokeWidth = width
    }

    /**
     * Ein quaderartiges Rechteck mit Licht- (links) und Schattenseite (rechts)
     * — verleiht flachen Kästen einfaches Volumen. [lightFrac] = Breite der
     * Lichtkante als Bruchteil.
     */
    fun shadedBox(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, base: Int, lightFrac: Float = 0.26f) {
        canvas.drawRect(l, t, r, b, solid(base))
        val wdt = r - l
        canvas.drawRect(l, t, l + wdt * lightFrac, b, solid(lighten(base, 26)))
        canvas.drawRect(r - wdt * lightFrac * 0.8f, t, r, b, solid(darken(base, 0.72f)))
    }
}
