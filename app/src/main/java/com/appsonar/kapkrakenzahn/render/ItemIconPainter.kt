package com.appsonar.kapkrakenzahn.render

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.appsonar.kapkrakenzahn.world.ItemId

/**
 * Malt für jeden Gegenstand ein wiedererkennbares Mini-Motiv auf eine
 * einheitlich dunkle Kachel — reine Vektorformen, kein Bild-Asset. Alle
 * Koordinaten sind Anteile der Kachelgröße, damit 36, 44 und 56 dp identisch
 * skalieren. Crafting-Familien teilen sich ihre Grundform (Stock→Teerfackel→
 * Fackel, Laterne→Brennende Laterne, …), damit die Verwandtschaft sichtbar ist.
 */
object ItemIconPainter {

    // Einheitliche Kachel: etwas heller als die Leiste (#111018), damit sie
    // als Kachel lesbar bleibt; der Rand ist Pflicht für die Abgrenzung.
    private const val KACHEL = 0xFF262433.toInt()
    private const val KACHEL_LICHT = 0xFF34324A.toInt()
    private const val KACHEL_RAND = 0xFF403D5C.toInt()

    // Motiv-Farben (natürliche Töne; Holz/Gold/Weiß kommen aus PaintKit).
    private const val STAHL = 0xFFB9C0C8.toInt()
    private const val EISEN = 0xFF6E747E.toInt()
    private const val MESSING = 0xFFCB9636.toInt()
    private const val TUERKIS = 0xFF35D6C8.toInt()
    private const val PUNSCH = 0xFFE8912C.toInt()
    private const val FLAMME_A = 0xFFE86A2C.toInt()
    private const val FLAMME_B = 0xFFF2C24E.toInt()
    private const val CHILI = 0xFFD8402C.toInt()
    private const val TAU = 0xFFC7A26A.toInt()
    private const val TEER = 0xFF1E1C20.toInt()
    private const val TEER_GLANZ = 0xFF4A4852.toInt()
    private const val TEER_RAND = 0xFF55525E.toInt()

    /** Malt Kachel + Motiv in das Quadrat (0,0)–(size,size). */
    fun draw(canvas: Canvas, id: ItemId, size: Float) {
        val pad = size * 0.1f
        val radius = size * 0.18f
        val kachel = PaintKit.roundedRect(pad, pad, size - pad, size - pad, radius)
        canvas.drawPath(kachel, PaintKit.solid(KACHEL))
        canvas.drawPath(
            PaintKit.roundedRect(pad, pad, size - pad, size * 0.32f, radius),
            PaintKit.solid(KACHEL_LICHT),
        )
        canvas.drawPath(kachel, PaintKit.line(KACHEL_RAND, size * 0.02f))
        motif(canvas, id, size)
    }

    /**
     * Motiv-Dispatch — absichtlich als Expression ohne else: Ein neuer
     * [ItemId] erzwingt hier eine Ergänzung, statt still leer zu bleiben.
     */
    private fun motif(canvas: Canvas, id: ItemId, s: Float): Unit = when (id) {
        ItemId.MEDAILLON -> medaillon(canvas, s)
        ItemId.SPIEGEL -> spiegel(canvas, s)
        ItemId.SANDDORNPUNSCH -> becher(canvas, s)
        ItemId.FEUERPFEFFER -> feuerpfeffer(canvas, s)
        ItemId.SPEZIALPUNSCH -> spezialpunsch(canvas, s)
        ItemId.MACHETE -> machete(canvas, s)
        ItemId.STINKFISCH -> stinkfisch(canvas, s)
        ItemId.LATERNE -> laterne(canvas, s, brennt = false)
        ItemId.TRANOEL -> tranoel(canvas, s)
        ItemId.BRENNENDE_LATERNE -> laterne(canvas, s, brennt = true)
        ItemId.EIMER -> eimer(canvas, s, quallen = false)
        ItemId.QUALLENEIMER -> eimer(canvas, s, quallen = true)
        ItemId.STOCK -> fackelStab(canvas, s, teerkopf = false, brennt = false)
        ItemId.TEER -> teerklumpen(canvas, s, 0.5f * s, 0.54f * s, 0.22f * s)
        ItemId.TEERFACKEL -> fackelStab(canvas, s, teerkopf = true, brennt = false)
        ItemId.FACKEL -> fackelStab(canvas, s, teerkopf = true, brennt = true)
        ItemId.FEUERSTEIN -> feuerstein(canvas, s)
        ItemId.SEIL -> seil(canvas, s)
        ItemId.ENTERHAKEN -> haken(canvas, 0.5f * s, 0.52f * s, 0.52f * s)
        ItemId.WURFANKER -> wurfanker(canvas, s)
        ItemId.GEZEITENAMULETT -> amulett(canvas, s)
        ItemId.MUSCHELHORN -> muschelhorn(canvas, s)
        ItemId.DIETRICH -> dietrich(canvas, s)
        ItemId.SCHLUESSEL -> schluessel(canvas, s, MESSING)
        ItemId.ERSATZSCHLUESSEL -> ersatzschluessel(canvas, s)
        ItemId.LAPPEN -> lappen(canvas, s)
        ItemId.FASS -> fass(canvas, s)
        ItemId.KRAKENAUGE -> krakenauge(canvas, s)
    }

    // ---- Familien-Helfer ----------------------------------------------------

    /** Diagonaler Ast; optional mit Teerkopf und Flamme (Stock→Teerfackel→Fackel). */
    private fun fackelStab(canvas: Canvas, s: Float, teerkopf: Boolean, brennt: Boolean) {
        if (brennt) {
            // Glow-Position = gedrehtes obere Astende (vorgerechnet).
            PaintKit.radialGlow(canvas, 0.26f * s, 0.22f * s, 0.24f * s, PaintKit.withAlpha(FLAMME_B, 120))
        }
        canvas.save()
        canvas.rotate(-40f, 0.5f * s, 0.5f * s)
        canvas.drawPath(
            PaintKit.roundedRect(0.455f * s, 0.24f * s, 0.545f * s, 0.82f * s, 0.04f * s),
            PaintKit.solid(PaintKit.HOLZ),
        )
        canvas.drawRect(
            0.465f * s, 0.26f * s, 0.49f * s, 0.80f * s,
            PaintKit.solid(PaintKit.lighten(PaintKit.HOLZ, 30)),
        )
        // Seitenast-Stummel
        canvas.drawPath(
            PaintKit.roundedRect(0.52f * s, 0.54f * s, 0.64f * s, 0.60f * s, 0.03f * s),
            PaintKit.solid(PaintKit.HOLZ_DUNKEL),
        )
        if (teerkopf) teerklumpen(canvas, s, 0.5f * s, 0.24f * s, 0.11f * s)
        if (brennt) flamme(canvas, 0.5f * s, 0.10f * s, 0.10f * s)
        canvas.restore()
    }

    /** Sturmlaterne; brennend mit warmem Glas, Flamme und Lichtschein. */
    private fun laterne(canvas: Canvas, s: Float, brennt: Boolean) {
        if (brennt) {
            PaintKit.radialGlow(canvas, 0.5f * s, 0.56f * s, 0.34f * s, PaintKit.withAlpha(FLAMME_B, 110))
        }
        val glasFarbe = if (brennt) FLAMME_B else 0xFF8FA3AA.toInt()
        val fenster = PaintKit.roundedRect(0.34f * s, 0.38f * s, 0.66f * s, 0.74f * s, 0.04f * s)
        canvas.drawPath(fenster, PaintKit.solid(glasFarbe))
        if (brennt) flamme(canvas, 0.5f * s, 0.58f * s, 0.09f * s)
        val rahmen = PaintKit.line(EISEN, 0.045f * s)
        canvas.drawPath(fenster, rahmen)
        canvas.drawLine(0.5f * s, 0.38f * s, 0.5f * s, 0.74f * s, rahmen)
        // Deckel + Tragering
        canvas.drawRect(0.38f * s, 0.32f * s, 0.62f * s, 0.38f * s, PaintKit.solid(EISEN))
        canvas.drawCircle(0.5f * s, 0.26f * s, 0.055f * s, PaintKit.line(EISEN, 0.04f * s))
    }

    /** Holzeimer; mit Quallen leuchtet türkises Wasser in der Öffnung. */
    private fun eimer(canvas: Canvas, s: Float, quallen: Boolean) {
        if (quallen) {
            PaintKit.radialGlow(canvas, 0.5f * s, 0.40f * s, 0.28f * s, PaintKit.withAlpha(TUERKIS, 90))
        }
        val korpus = Path().apply {
            moveTo(0.30f * s, 0.40f * s)
            lineTo(0.36f * s, 0.78f * s)
            lineTo(0.64f * s, 0.78f * s)
            lineTo(0.70f * s, 0.40f * s)
            close()
        }
        canvas.drawPath(korpus, PaintKit.solid(PaintKit.HOLZ))
        canvas.drawRect(0.31f * s, 0.47f * s, 0.69f * s, 0.51f * s, PaintKit.solid(EISEN))
        canvas.drawRect(0.34f * s, 0.66f * s, 0.66f * s, 0.70f * s, PaintKit.solid(EISEN))
        canvas.drawOval(RectF(0.30f * s, 0.34f * s, 0.70f * s, 0.46f * s), PaintKit.solid(PaintKit.HOLZ_DUNKEL))
        if (quallen) {
            canvas.drawOval(RectF(0.33f * s, 0.36f * s, 0.67f * s, 0.44f * s), PaintKit.solid(TUERKIS))
            val schirm = PaintKit.solid(PaintKit.lighten(TUERKIS, 60))
            canvas.drawCircle(0.42f * s, 0.40f * s, 0.035f * s, schirm)
            canvas.drawCircle(0.55f * s, 0.39f * s, 0.03f * s, schirm)
        }
        val henkel = Path().apply {
            moveTo(0.30f * s, 0.40f * s)
            quadTo(0.5f * s, 0.16f * s, 0.70f * s, 0.40f * s)
        }
        canvas.drawPath(henkel, PaintKit.line(PaintKit.HOLZ_DUNKEL, 0.035f * s))
    }

    /** Holzkrug mit Punsch — Basis für Sanddorn- und Spezial-Punsch. */
    private fun becher(canvas: Canvas, s: Float) {
        canvas.drawPath(
            PaintKit.roundedRect(0.34f * s, 0.36f * s, 0.62f * s, 0.76f * s, 0.05f * s),
            PaintKit.solid(PaintKit.HOLZ),
        )
        canvas.drawRect(
            0.37f * s, 0.38f * s, 0.42f * s, 0.74f * s,
            PaintKit.solid(PaintKit.lighten(PaintKit.HOLZ, 30)),
        )
        val henkel = Path().apply {
            moveTo(0.62f * s, 0.44f * s)
            quadTo(0.76f * s, 0.48f * s, 0.72f * s, 0.58f * s)
            quadTo(0.69f * s, 0.65f * s, 0.62f * s, 0.64f * s)
        }
        canvas.drawPath(henkel, PaintKit.line(PaintKit.HOLZ_DUNKEL, 0.05f * s))
        canvas.drawOval(RectF(0.36f * s, 0.32f * s, 0.60f * s, 0.42f * s), PaintKit.solid(PUNSCH))
    }

    /** Dreizinkiger Enterhaken mit Öse — auch Teil des Wurfankers. */
    private fun haken(canvas: Canvas, cx: Float, cy: Float, h: Float) {
        canvas.drawPath(
            PaintKit.roundedRect(cx - h * 0.08f, cy - h * 0.5f, cx + h * 0.08f, cy + h * 0.25f, h * 0.08f),
            PaintKit.solid(EISEN),
        )
        canvas.drawCircle(cx, cy - h * 0.58f, h * 0.11f, PaintKit.line(EISEN, h * 0.07f))
        val zinkeL = Path().apply {
            moveTo(cx, cy + h * 0.18f)
            quadTo(cx - h * 0.34f, cy + h * 0.30f, cx - h * 0.30f, cy - h * 0.05f)
        }
        canvas.drawPath(zinkeL, PaintKit.line(EISEN, h * 0.13f))
        val zinkeR = Path().apply {
            moveTo(cx, cy + h * 0.18f)
            quadTo(cx + h * 0.34f, cy + h * 0.30f, cx + h * 0.30f, cy - h * 0.05f)
        }
        canvas.drawPath(zinkeR, PaintKit.line(EISEN, h * 0.13f))
        canvas.drawCircle(cx, cy + h * 0.28f, h * 0.09f, PaintKit.solid(EISEN))
    }

    /** Aufgeschossene Seilrolle — auch Teil des Wurfankers. */
    private fun seilrolle(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        canvas.drawCircle(cx, cy, r, PaintKit.line(TAU, r * 0.55f))
        val rille = PaintKit.line(PaintKit.darken(TAU, 0.75f), r * 0.09f)
        canvas.drawCircle(cx, cy, r * 0.82f, rille)
        canvas.drawCircle(cx, cy, r * 1.18f, rille)
        // Herausragendes Seilende
        val ende = Path().apply {
            moveTo(cx + r * 0.6f, cy + r * 0.9f)
            quadTo(cx + r * 1.1f, cy + r * 1.2f, cx + r * 1.4f, cy + r * 1.1f)
        }
        canvas.drawPath(ende, PaintKit.line(TAU, r * 0.3f))
    }

    /** Schlüssel mit Ring-Reide, Schaft und zwei Bartzähnen. */
    private fun schluessel(canvas: Canvas, s: Float, farbe: Int) {
        canvas.drawCircle(0.30f * s, 0.50f * s, 0.10f * s, PaintKit.line(farbe, 0.055f * s))
        canvas.drawPath(
            PaintKit.roundedRect(0.38f * s, 0.465f * s, 0.78f * s, 0.535f * s, 0.03f * s),
            PaintKit.solid(farbe),
        )
        canvas.drawRect(0.63f * s, 0.535f * s, 0.69f * s, 0.66f * s, PaintKit.solid(farbe))
        canvas.drawRect(0.72f * s, 0.535f * s, 0.78f * s, 0.62f * s, PaintKit.solid(farbe))
    }

    /** Teer-Blob mit Glanzbogen; helle Kontur, sonst säuft er auf der Kachel ab. */
    private fun teerklumpen(canvas: Canvas, s: Float, cx: Float, cy: Float, r: Float) {
        val blob = Path().apply {
            moveTo(cx - r, cy)
            quadTo(cx - r, cy - r, cx, cy - r)
            quadTo(cx + r * 1.1f, cy - r, cx + r, cy + r * 0.2f)
            quadTo(cx + r * 0.7f, cy + r, cx - r * 0.2f, cy + r * 0.9f)
            quadTo(cx - r, cy + r * 0.6f, cx - r, cy)
            close()
        }
        canvas.drawPath(blob, PaintKit.solid(TEER))
        canvas.drawPath(blob, PaintKit.line(TEER_RAND, r * 0.14f))
        val glanz = Path().apply {
            moveTo(cx - r * 0.5f, cy - r * 0.45f)
            quadTo(cx - r * 0.1f, cy - r * 0.75f, cx + r * 0.35f, cy - r * 0.5f)
        }
        canvas.drawPath(glanz, PaintKit.line(TEER_GLANZ, r * 0.18f))
    }

    /** Zweifarbige Flamme (außen orange, Kern gelb). */
    private fun flamme(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val aussen = Path().apply {
            moveTo(cx, cy - r * 1.2f)
            quadTo(cx + r, cy - r * 0.1f, cx, cy + r * 0.8f)
            quadTo(cx - r, cy - r * 0.1f, cx, cy - r * 1.2f)
            close()
        }
        canvas.drawPath(aussen, PaintKit.solid(FLAMME_A))
        val innen = Path().apply {
            moveTo(cx, cy - r * 0.5f)
            quadTo(cx + r * 0.45f, cy + r * 0.1f, cx, cy + r * 0.6f)
            quadTo(cx - r * 0.45f, cy + r * 0.1f, cx, cy - r * 0.5f)
            close()
        }
        canvas.drawPath(innen, PaintKit.solid(FLAMME_B))
    }

    // ---- Einzelmotive --------------------------------------------------------

    private fun medaillon(canvas: Canvas, s: Float) {
        val silber = 0xFFC8CAD2.toInt()
        val rand = 0xFF8E93A2.toInt()
        canvas.drawCircle(0.5f * s, 0.56f * s, 0.22f * s, PaintKit.solid(silber))
        canvas.drawCircle(0.5f * s, 0.56f * s, 0.22f * s, PaintKit.line(rand, 0.035f * s))
        canvas.drawCircle(0.5f * s, 0.56f * s, 0.13f * s, PaintKit.line(rand, 0.02f * s))
        // Kettenglieder zur Ecke
        canvas.drawCircle(0.60f * s, 0.30f * s, 0.045f * s, PaintKit.line(silber, 0.025f * s))
        canvas.drawCircle(0.68f * s, 0.22f * s, 0.045f * s, PaintKit.line(silber, 0.025f * s))
    }

    private fun spiegel(canvas: Canvas, s: Float) {
        val rahmen = 0xFFE8DCE0.toInt()
        canvas.drawPath(
            PaintKit.roundedRect(0.46f * s, 0.58f * s, 0.54f * s, 0.82f * s, 0.04f * s),
            PaintKit.solid(rahmen),
        )
        canvas.drawCircle(0.5f * s, 0.42f * s, 0.21f * s, PaintKit.solid(rahmen))
        canvas.drawCircle(0.5f * s, 0.42f * s, 0.16f * s, PaintKit.solid(0xFF9FD4E8.toInt()))
        canvas.drawLine(
            0.42f * s, 0.48f * s, 0.55f * s, 0.33f * s,
            PaintKit.line(PaintKit.withAlpha(PaintKit.WEISS, 200), 0.035f * s),
        )
    }

    private fun feuerpfeffer(canvas: Canvas, s: Float) {
        val body = Path().apply {
            moveTo(0.40f * s, 0.34f * s)
            quadTo(0.36f * s, 0.62f * s, 0.62f * s, 0.72f * s)
        }
        canvas.drawPath(body, PaintKit.line(CHILI, 0.16f * s))
        val glanz = Path().apply {
            moveTo(0.42f * s, 0.40f * s)
            quadTo(0.40f * s, 0.56f * s, 0.50f * s, 0.63f * s)
        }
        canvas.drawPath(glanz, PaintKit.line(PaintKit.lighten(CHILI, 50), 0.05f * s))
        val stiel = Path().apply {
            moveTo(0.40f * s, 0.34f * s)
            quadTo(0.42f * s, 0.24f * s, 0.52f * s, 0.22f * s)
        }
        canvas.drawPath(stiel, PaintKit.line(0xFF3F7D3A.toInt(), 0.05f * s))
    }

    private fun spezialpunsch(canvas: Canvas, s: Float) {
        becher(canvas, s)
        // Dampf
        val dampfPaint = PaintKit.line(PaintKit.withAlpha(PaintKit.WEISS, 140), 0.035f * s)
        val dampf1 = Path().apply {
            moveTo(0.42f * s, 0.28f * s)
            quadTo(0.37f * s, 0.23f * s, 0.42f * s, 0.18f * s)
        }
        canvas.drawPath(dampf1, dampfPaint)
        val dampf2 = Path().apply {
            moveTo(0.52f * s, 0.27f * s)
            quadTo(0.57f * s, 0.21f * s, 0.52f * s, 0.15f * s)
        }
        canvas.drawPath(dampf2, dampfPaint)
        // Mini-Chili am Becherrand
        val chili = Path().apply {
            moveTo(0.60f * s, 0.30f * s)
            quadTo(0.66f * s, 0.34f * s, 0.66f * s, 0.40f * s)
        }
        canvas.drawPath(chili, PaintKit.line(CHILI, 0.06f * s))
    }

    private fun machete(canvas: Canvas, s: Float) {
        canvas.save()
        canvas.rotate(-35f, 0.5f * s, 0.5f * s)
        val klinge = Path().apply {
            moveTo(0.30f * s, 0.46f * s)
            lineTo(0.74f * s, 0.46f * s)
            quadTo(0.84f * s, 0.50f * s, 0.72f * s, 0.58f * s)
            lineTo(0.30f * s, 0.56f * s)
            close()
        }
        canvas.drawPath(klinge, PaintKit.solid(STAHL))
        canvas.drawLine(
            0.32f * s, 0.565f * s, 0.72f * s, 0.575f * s,
            PaintKit.line(PaintKit.withAlpha(PaintKit.WEISS, 200), 0.018f * s),
        )
        canvas.drawPath(
            PaintKit.roundedRect(0.14f * s, 0.455f * s, 0.30f * s, 0.565f * s, 0.04f * s),
            PaintKit.solid(0xFF4A3B2A.toInt()),
        )
        canvas.restore()
    }

    private fun stinkfisch(canvas: Canvas, s: Float) {
        val koerper = 0xFF7C8A6A.toInt()
        canvas.drawOval(RectF(0.24f * s, 0.48f * s, 0.64f * s, 0.72f * s), PaintKit.solid(koerper))
        val schwanz = Path().apply {
            moveTo(0.62f * s, 0.60f * s)
            lineTo(0.78f * s, 0.50f * s)
            lineTo(0.78f * s, 0.70f * s)
            close()
        }
        canvas.drawPath(schwanz, PaintKit.solid(koerper))
        canvas.drawCircle(0.33f * s, 0.57f * s, 0.035f * s, PaintKit.solid(PaintKit.WEISS))
        canvas.drawCircle(0.335f * s, 0.575f * s, 0.017f * s, PaintKit.solid(0xFF20140C.toInt()))
        // Stinklinien
        val stink = PaintKit.line(PaintKit.withAlpha(0xFF9FB37A.toInt(), 150), 0.03f * s)
        val linie1 = Path().apply {
            moveTo(0.38f * s, 0.42f * s)
            quadTo(0.34f * s, 0.36f * s, 0.38f * s, 0.30f * s)
            quadTo(0.42f * s, 0.25f * s, 0.38f * s, 0.20f * s)
        }
        canvas.drawPath(linie1, stink)
        val linie2 = Path().apply {
            moveTo(0.50f * s, 0.42f * s)
            quadTo(0.46f * s, 0.36f * s, 0.50f * s, 0.30f * s)
        }
        canvas.drawPath(linie2, stink)
    }

    private fun tranoel(canvas: Canvas, s: Float) {
        val ton = 0xFFB08D5E.toInt()
        canvas.drawCircle(0.5f * s, 0.58f * s, 0.20f * s, PaintKit.solid(ton))
        canvas.drawPath(
            PaintKit.roundedRect(0.44f * s, 0.28f * s, 0.56f * s, 0.44f * s, 0.03f * s),
            PaintKit.solid(ton),
        )
        canvas.drawRect(0.45f * s, 0.24f * s, 0.55f * s, 0.30f * s, PaintKit.solid(0xFF6E4A2C.toInt()))
        // Öl-Tropfen als Signet auf dem Bauch
        val tropfen = Path().apply {
            moveTo(0.5f * s, 0.50f * s)
            quadTo(0.56f * s, 0.58f * s, 0.5f * s, 0.64f * s)
            quadTo(0.44f * s, 0.58f * s, 0.5f * s, 0.50f * s)
            close()
        }
        canvas.drawPath(tropfen, PaintKit.solid(0xFF3A5E66.toInt()))
    }

    private fun feuerstein(canvas: Canvas, s: Float) {
        val stein = Path().apply {
            moveTo(0.36f * s, 0.36f * s)
            lineTo(0.60f * s, 0.30f * s)
            lineTo(0.72f * s, 0.52f * s)
            lineTo(0.56f * s, 0.70f * s)
            lineTo(0.34f * s, 0.62f * s)
            close()
        }
        canvas.drawPath(stein, PaintKit.solid(0xFF8E8E96.toInt()))
        val facette = Path().apply {
            moveTo(0.60f * s, 0.30f * s)
            lineTo(0.72f * s, 0.52f * s)
            lineTo(0.56f * s, 0.70f * s)
            lineTo(0.52f * s, 0.48f * s)
            close()
        }
        canvas.drawPath(facette, PaintKit.solid(0xFF6E6E78.toInt()))
        // Funken an der Kante
        val funke = PaintKit.line(PaintKit.GOLD, 0.03f * s)
        canvas.drawLine(0.70f * s, 0.62f * s, 0.78f * s, 0.68f * s, funke)
        canvas.drawLine(0.66f * s, 0.68f * s, 0.71f * s, 0.77f * s, funke)
        canvas.drawLine(0.73f * s, 0.55f * s, 0.82f * s, 0.56f * s, funke)
    }

    private fun seil(canvas: Canvas, s: Float) {
        seilrolle(canvas, 0.48f * s, 0.50f * s, 0.20f * s)
    }

    private fun wurfanker(canvas: Canvas, s: Float) {
        seilrolle(canvas, 0.36f * s, 0.66f * s, 0.12f * s)
        // Verbindungsseil zur Öse
        val seil = Path().apply {
            moveTo(0.44f * s, 0.58f * s)
            quadTo(0.52f * s, 0.44f * s, 0.62f * s, 0.24f * s)
        }
        canvas.drawPath(seil, PaintKit.line(TAU, 0.035f * s))
        haken(canvas, 0.64f * s, 0.42f * s, 0.34f * s)
    }

    private fun amulett(canvas: Canvas, s: Float) {
        val tropfen = Path().apply {
            moveTo(0.5f * s, 0.36f * s)
            quadTo(0.68f * s, 0.52f * s, 0.5f * s, 0.74f * s)
            quadTo(0.32f * s, 0.52f * s, 0.5f * s, 0.36f * s)
            close()
        }
        canvas.drawPath(tropfen, PaintKit.solid(TUERKIS))
        canvas.drawCircle(0.46f * s, 0.50f * s, 0.045f * s, PaintKit.solid(PaintKit.withAlpha(PaintKit.WEISS, 90)))
        // Lederband
        val band = Path().apply {
            moveTo(0.40f * s, 0.34f * s)
            quadTo(0.5f * s, 0.18f * s, 0.60f * s, 0.34f * s)
        }
        canvas.drawPath(band, PaintKit.line(0xFF4A3B2A.toInt(), 0.035f * s))
    }

    private fun muschelhorn(canvas: Canvas, s: Float) {
        val sand = 0xFFE0C4A8.toInt()
        val horn = Path().apply {
            moveTo(0.24f * s, 0.72f * s)
            lineTo(0.54f * s, 0.30f * s)
            lineTo(0.76f * s, 0.42f * s)
            lineTo(0.46f * s, 0.78f * s)
            close()
        }
        canvas.drawPath(horn, PaintKit.solid(sand))
        // Spirale am dicken Ende
        val spirale = 0xFFB08D6E.toInt()
        canvas.drawCircle(0.62f * s, 0.42f * s, 0.09f * s, PaintKit.line(spirale, 0.03f * s))
        canvas.drawCircle(0.62f * s, 0.42f * s, 0.045f * s, PaintKit.line(spirale, 0.025f * s))
        // Querrillen
        val rille = PaintKit.line(spirale, 0.02f * s)
        canvas.drawLine(0.38f * s, 0.62f * s, 0.48f * s, 0.68f * s, rille)
        canvas.drawLine(0.44f * s, 0.52f * s, 0.55f * s, 0.59f * s, rille)
    }

    private fun dietrich(canvas: Canvas, s: Float) {
        val stab = Path().apply {
            moveTo(0.36f * s, 0.72f * s)
            lineTo(0.60f * s, 0.38f * s)
            quadTo(0.66f * s, 0.29f * s, 0.57f * s, 0.27f * s)
        }
        canvas.drawPath(stab, PaintKit.line(STAHL, 0.055f * s))
        canvas.drawCircle(0.33f * s, 0.76f * s, 0.05f * s, PaintKit.line(STAHL, 0.04f * s))
    }

    private fun ersatzschluessel(canvas: Canvas, s: Float) {
        schluessel(canvas, s, PaintKit.darken(MESSING, 0.85f))
        // Erdkrümel — der Zweitschlüssel war vergraben.
        val erde = PaintKit.solid(0xFF5E4326.toInt())
        canvas.drawCircle(0.28f * s, 0.42f * s, 0.028f * s, erde)
        canvas.drawCircle(0.48f * s, 0.44f * s, 0.024f * s, erde)
        canvas.drawCircle(0.60f * s, 0.50f * s, 0.028f * s, erde)
        canvas.drawCircle(0.40f * s, 0.56f * s, 0.022f * s, erde)
    }

    private fun lappen(canvas: Canvas, s: Float) {
        val stoff = 0xFFD8D2C2.toInt()
        canvas.drawPath(PaintKit.roundedRect(0.28f * s, 0.36f * s, 0.72f * s, 0.64f * s, 0.04f * s), PaintKit.solid(stoff))
        // Wellige Unterkante als Halbkreise
        val welle = PaintKit.solid(stoff)
        canvas.drawCircle(0.35f * s, 0.64f * s, 0.06f * s, welle)
        canvas.drawCircle(0.48f * s, 0.65f * s, 0.06f * s, welle)
        canvas.drawCircle(0.61f * s, 0.64f * s, 0.06f * s, welle)
        canvas.drawLine(
            0.32f * s, 0.40f * s, 0.66f * s, 0.60f * s,
            PaintKit.line(0xFFA8A092.toInt(), 0.022f * s),
        )
        canvas.drawCircle(0.58f * s, 0.46f * s, 0.045f * s, PaintKit.solid(PaintKit.withAlpha(0xFF8A8274.toInt(), 120)))
    }

    private fun fass(canvas: Canvas, s: Float) {
        val korpus = Path().apply {
            moveTo(0.34f * s, 0.30f * s)
            quadTo(0.24f * s, 0.52f * s, 0.34f * s, 0.76f * s)
            lineTo(0.66f * s, 0.76f * s)
            quadTo(0.76f * s, 0.52f * s, 0.66f * s, 0.30f * s)
            close()
        }
        canvas.drawPath(korpus, PaintKit.solid(PaintKit.HOLZ))
        // Daubenlinien
        val daube = PaintKit.line(PaintKit.HOLZ_DUNKEL, 0.018f * s)
        canvas.drawLine(0.45f * s, 0.32f * s, 0.45f * s, 0.74f * s, daube)
        canvas.drawLine(0.55f * s, 0.32f * s, 0.55f * s, 0.74f * s, daube)
        // Eisenbänder
        canvas.drawRect(0.28f * s, 0.40f * s, 0.72f * s, 0.44f * s, PaintKit.solid(EISEN))
        canvas.drawRect(0.28f * s, 0.60f * s, 0.72f * s, 0.64f * s, PaintKit.solid(EISEN))
        // Deckel
        canvas.drawOval(RectF(0.34f * s, 0.26f * s, 0.66f * s, 0.34f * s), PaintKit.solid(PaintKit.lighten(PaintKit.HOLZ, 25)))
    }

    private fun krakenauge(canvas: Canvas, s: Float) {
        PaintKit.radialGlow(canvas, 0.5f * s, 0.54f * s, 0.34f * s, PaintKit.withAlpha(TUERKIS, 100))
        canvas.drawCircle(0.5f * s, 0.54f * s, 0.19f * s, PaintKit.solid(TUERKIS))
        // Senkrechter Pupillenschlitz
        canvas.drawOval(RectF(0.465f * s, 0.42f * s, 0.535f * s, 0.66f * s), PaintKit.solid(0xFF0F3A44.toInt()))
        canvas.drawCircle(0.43f * s, 0.46f * s, 0.03f * s, PaintKit.solid(PaintKit.withAlpha(PaintKit.WEISS, 220)))
    }
}
