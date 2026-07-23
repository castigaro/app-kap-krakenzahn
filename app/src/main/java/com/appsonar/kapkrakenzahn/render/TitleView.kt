package com.appsonar.kapkrakenzahn.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

/**
 * Kleines animiertes Titelbild für den Startbildschirm: Leuchtturm auf
 * einem Kap über nächtlicher See, mit rotierendem Lichtkegel. Programmatisch
 * gezeichnet, kein Bild-Asset.
 */
class TitleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val start = System.currentTimeMillis()

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val t = (System.currentTimeMillis() - start) / 1000f

        // Nachthimmel
        PaintKit.verticalGradient(canvas, 0f, 0f, w, h, 0xFF12203A.toInt(), 0xFF1C4A5E.toInt())
        // Sterne
        val star = PaintKit.solid(Color.argb(200, 255, 255, 230))
        listOf(0.15f to 0.2f, 0.3f to 0.12f, 0.7f to 0.18f, 0.85f to 0.1f, 0.55f to 0.08f).forEach {
            canvas.drawCircle(w * it.first, h * it.second, h * 0.006f, star)
        }

        // Mond
        canvas.drawCircle(w * 0.80f, h * 0.25f, h * 0.08f, PaintKit.solid(0xFFF0E6C0.toInt()))

        // See
        PaintKit.verticalGradient(canvas, 0f, h * 0.62f, w, h, PaintKit.MEER_OBEN, PaintKit.NACHT)
        canvas.drawPath(PaintKit.wave(w, h * 0.64f, h, h * 0.008f, w * 0.25f, t), PaintKit.solid(0x22FFFFFF))

        // Kap (Felsen)
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(0.30f to 0.66f, 0.70f to 0.66f, 0.62f to 1.0f, 0.38f to 1.0f)),
            PaintKit.solid(PaintKit.FELS_DUNKEL),
        )

        // Leuchtturm
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(0.46f to 0.66f, 0.475f to 0.28f, 0.525f to 0.28f, 0.54f to 0.66f)),
            PaintKit.solid(PaintKit.WEISS),
        )
        for (y in listOf(0.36f, 0.48f, 0.60f)) {
            canvas.drawRect(w * 0.465f, h * y, w * 0.535f, h * (y + 0.04f), PaintKit.solid(0xFFB84A3A.toInt()))
        }
        // Laternenraum
        canvas.drawRect(w * 0.475f, h * 0.22f, w * 0.525f, h * 0.28f, PaintKit.solid(PaintKit.GOLD))

        // Rotierender Lichtkegel
        val sweep = sin(t * 0.8).toFloat()
        val coneDir = if (sweep >= 0) 1f else -1f
        val cone = PaintKit.solid(Color.argb(70, 255, 240, 160))
        val tipX = 0.5f + coneDir * (0.5f * kotlin.math.abs(sweep))
        canvas.drawPath(
            PaintKit.polygon(w, h, listOf(
                0.5f to 0.25f, tipX to 0.05f, (tipX + coneDir * 0.15f) to 0.18f,
            )),
            cone,
        )

        postInvalidateOnAnimation()
    }
}
