package com.appsonar.kapkrakenzahn.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import com.appsonar.kapkrakenzahn.world.Hotspot
import com.appsonar.kapkrakenzahn.world.NpcId
import kotlin.math.sin

/**
 * Stehende Spielfiguren (NPCs), im Stil von [HeroSprite] aus Vektorformen
 * gezeichnet — kein Bild-Asset. Jede Figur hat eine feste Farbgebung und
 * wippt nur leicht auf der Stelle; Ansgar erscheint als durchscheinender,
 * schwebender Geist. Krächz fehlt bewusst: Der Papagei wird bereits von der
 * [scenes.HarborScene] auf seinem Poller gemalt.
 */
object NpcSprite {

    private class Look(
        val mantel: Int,
        val haar: Int,
        val haut: Int = 0xFFE8C9A0.toInt(),
        val ghost: Boolean = false,
        val bart: Boolean = false,
    )

    private val LOOKS = mapOf(
        NpcId.GRIET to Look(mantel = 0xFF7A4A2E.toInt(), haar = 0xFF4A2C14.toInt()),
        NpcId.RASMUS to Look(mantel = 0xFF5E2430.toInt(), haar = 0xFFB03A2A.toInt(), bart = true),
        NpcId.YOLA to Look(mantel = 0xFF2E6E68.toInt(), haar = 0xFF20140C.toInt()),
        NpcId.BO to Look(mantel = 0xFF4A4136.toInt(), haar = 0xFF8A8A8A.toInt()),
        NpcId.MARA to Look(mantel = 0xFF3E6E4A.toInt(), haar = 0xFFD8D8CB.toInt()),
        NpcId.ANSGAR to Look(
            mantel = 0xFF7A94A8.toInt(), haar = 0xFFC8D8E0.toInt(),
            haut = 0xFFDCE8EE.toInt(), ghost = true,
        ),
        NpcId.FIELO to Look(mantel = 0xFF9C7A2E.toInt(), haar = 0xFF3A2418.toInt()),
    )

    /** Fußpunkt der Figur und optionale Sichtkante (alles darunter wird geklippt). */
    class Placement(val x: Float, val groundY: Float, val clipBottomNorm: Float? = null)

    /**
     * Wo die Figur eines NPC-Hotspots steht. Default: Mitte/Unterkante des
     * Hotspot-Rechtecks. Overrides, wo die Szene es verlangt — Griet steht
     * hinter der Theke, der Clip an der Thekenkante verdeckt ihren Unterkörper.
     */
    fun placement(hotspot: Hotspot): Placement = when (hotspot.id) {
        "taverne_griet" -> Placement(0.30f, 0.74f, clipBottomNorm = 0.615f)
        else -> Placement(hotspot.rect.centerX, hotspot.rect.bottom - 0.03f)
    }

    private val outline = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    /**
     * Zeichnet den NPC an normierter x-Position auf der Boden-Linie
     * [groundY]; [heightNorm] ist die Figurhöhe als Bruchteil der View-Höhe
     * (Tiefenskalierung wie bei der Heldin), [timeMs] treibt das Wippen.
     */
    fun draw(
        canvas: Canvas, w: Float, h: Float,
        npc: NpcId, xNorm: Float, groundY: Float, heightNorm: Float, timeMs: Long,
    ) {
        val look = LOOKS[npc] ?: return
        val alpha = if (look.ghost) 170 else 255
        fun tint(color: Int): Int =
            Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

        val cx = xNorm * w
        val figH = heightNorm * h
        val bob = sin(timeMs / 500.0 + npc.ordinal).toFloat() * figH * 0.012f
        // Der Geist schwebt eine Handbreit über dem Boden und wirft keinen Schatten.
        val hover = if (look.ghost) figH * 0.06f - bob * 2f else 0f
        val bottom = groundY * h - hover
        val facingRight = xNorm < 0.5f

        if (!look.ghost) {
            PaintKit.softShadow(canvas, cx, groundY * h, figH * 0.20f, figH * 0.045f, alpha = 80)
        }

        canvas.save()
        canvas.translate(cx, bottom)
        if (!facingRight) canvas.scale(-1f, 1f)
        canvas.translate(0f, -bob)

        outline.strokeWidth = figH * 0.03f
        outline.color = tint(0xFF1A0E08.toInt())

        // Langer Mantel/Rock: Trapez von den Schultern bis zum Boden.
        val coat = Path().apply {
            moveTo(-figH * 0.15f, -figH * 0.84f)
            lineTo(-figH * 0.22f, 0f)
            lineTo(figH * 0.22f, 0f)
            lineTo(figH * 0.15f, -figH * 0.84f)
            close()
        }
        canvas.drawPath(coat, PaintKit.solid(tint(look.mantel)))
        // Lichtseite links, Schattenseite rechts — wie bei der Heldin.
        val hell = PaintKit.lerpColor(look.mantel, 0xFFFFFFFF.toInt(), 0.22f)
        val dunkel = PaintKit.lerpColor(look.mantel, 0xFF000000.toInt(), 0.32f)
        val licht = Path().apply {
            moveTo(-figH * 0.15f, -figH * 0.84f)
            lineTo(-figH * 0.22f, 0f)
            lineTo(-figH * 0.12f, 0f)
            lineTo(-figH * 0.07f, -figH * 0.82f)
            close()
        }
        canvas.drawPath(licht, PaintKit.solid(tint(hell)))
        val schatten = Path().apply {
            moveTo(figH * 0.06f, 0f)
            lineTo(figH * 0.22f, 0f)
            lineTo(figH * 0.15f, -figH * 0.84f)
            close()
        }
        canvas.drawPath(schatten, PaintKit.solid(tint(dunkel)))
        canvas.drawPath(coat, outline)

        // Gürtel
        canvas.drawRect(
            -figH * 0.17f, -figH * 0.46f, figH * 0.17f, -figH * 0.42f,
            PaintKit.solid(tint(0xFF3A2418.toInt())),
        )

        // Seitlicher Arm
        val armW = figH * 0.07f
        canvas.drawRect(
            figH * 0.12f, -figH * 0.80f, figH * 0.12f + armW, -figH * 0.46f,
            PaintKit.solid(tint(look.mantel)),
        )
        canvas.drawCircle(
            figH * 0.12f + armW / 2, -figH * 0.44f, armW * 0.6f,
            PaintKit.solid(tint(look.haut)),
        )

        // Kopf mit Haar und Auge
        val headR = figH * 0.11f
        val headY = -figH * 0.84f - headR
        canvas.drawCircle(0f, headY, headR, PaintKit.solid(tint(look.haut)))
        canvas.drawCircle(0f, headY, headR, outline)
        canvas.drawCircle(-headR * 0.2f, headY - headR * 0.25f, headR * 0.85f, PaintKit.solid(tint(look.haar)))
        canvas.drawCircle(0f, headY + headR * 0.08f, headR * 0.95f, PaintKit.solid(tint(look.haut)))
        canvas.drawCircle(headR * 0.35f, headY, headR * 0.12f, PaintKit.solid(tint(0xFF20140C.toInt())))

        // Rasmus' legendärer Bart
        if (look.bart) {
            val bart = Path().apply {
                moveTo(-headR * 0.7f, headY + headR * 0.3f)
                lineTo(headR * 0.7f, headY + headR * 0.3f)
                lineTo(0f, headY + headR * 2.2f)
                close()
            }
            canvas.drawPath(bart, PaintKit.solid(tint(look.haar)))
        }

        canvas.restore()
    }
}
