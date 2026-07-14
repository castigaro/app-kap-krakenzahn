package com.castigaro.kapkrakenzahn.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.world.Exit
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.Hotspot
import kotlin.math.abs

/**
 * Die Spielfläche: malt den aktuellen Schauplatz plus Heldin, treibt die
 * Lauf- und Ambient-Animation über [postInvalidateOnAnimation] und meldet
 * Taps als Ereignisse an die [Listener] (Activity). Reine View/Canvas-
 * Lösung, keine Game-Engine-Bibliothek.
 */
class SceneView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : View(context, attrs) {

    interface Listener {
        /** Held ist bei einem Hotspot angekommen → Verben anbieten. */
        fun onHotspotReached(hotspot: Hotspot)

        /** Held ist an einem Ausgang angekommen. */
        fun onExitReached(exit: Exit)

        /** Leere Stelle angetippt (nur Laufen). */
        fun onWalkOnly() {}
    }

    var listener: Listener? = null

    private var state: GameState? = null
    private var painter: ScenePainter? = null

    // Lauf-Animation
    private var heroX = 0.5f
    private var targetX = 0.5f
    private var walking = false
    private var facingRight = true
    private var walkPhase = 0f
    private var pendingHotspot: Hotspot? = null
    private var pendingExit: Exit? = null

    private var lastFrame = 0L
    private val startTime = System.currentTimeMillis()

    private val walkSpeed = 0.28f // Bildschirmbreiten pro Sekunde
    private val groundY get() = state?.let { GameWorld.location(it.locationId).walkY } ?: 0.86f

    fun bind(state: GameState) {
        this.state = state
        this.painter = ScenePainter.forLocation(state.locationId)
        heroX = state.heroX
        targetX = state.heroX
        walking = false
        pendingHotspot = null
        pendingExit = null
        invalidate()
    }

    /** Setzt die Heldin ohne Laufanimation an eine Position (nach Ortswechsel). */
    fun placeHero(x: Float) {
        heroX = x
        targetX = x
        walking = false
        state?.heroX = x
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return true
        val s = state ?: return true
        val nx = event.x / width
        val ny = event.y / height

        // Hotspots zuerst (obere Trefferpriorität)
        val hotspot = GameWorld.hotspotsFor(s, s.locationId)
            .firstOrNull { it.rect.contains(nx, ny) }
        if (hotspot != null) {
            walkTo(hotspot.anchorX)
            pendingHotspot = hotspot
            pendingExit = null
            return true
        }
        // Ausgänge (Pfeile am Rand, unterer Bereich)
        val exit = GameWorld.exitsFor(s, s.locationId)
            .firstOrNull { abs(nx - it.xNorm) < 0.08f && ny > 0.45f }
        if (exit != null) {
            walkTo(exit.xNorm)
            pendingExit = exit
            pendingHotspot = null
            return true
        }
        // Sonst nur laufen
        walkTo(nx.coerceIn(0.05f, 0.95f))
        pendingHotspot = null
        pendingExit = null
        listener?.onWalkOnly()
        return true
    }

    private fun walkTo(x: Float) {
        targetX = x
        facingRight = x >= heroX
        walking = true
        lastFrame = System.currentTimeMillis()
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        val s = state ?: return
        val p = painter ?: return
        val w = width.toFloat()
        val h = height.toFloat()
        val now = System.currentTimeMillis()

        p.draw(canvas, w, h, s, now - startTime)

        // Ausgangs-Pfeile
        drawExits(canvas, s, w, h)

        // Held bewegen
        if (walking) {
            val dt = (now - lastFrame).coerceAtMost(64L) / 1000f
            lastFrame = now
            val dir = if (targetX > heroX) 1f else -1f
            heroX += dir * walkSpeed * dt
            walkPhase += dt * 10f
            if ((dir > 0 && heroX >= targetX) || (dir < 0 && heroX <= targetX)) {
                heroX = targetX
                walking = false
                s.heroX = heroX
                arriveAtTarget()
            }
        }

        // Heldin — leicht kleiner je weiter „hinten" (höher am Boden ist hier egal, feste Bande)
        HeroSprite.draw(
            canvas, w, h, heroX, groundY, heightNorm = 0.32f,
            phase = walkPhase, facingRight = facingRight, walking = walking,
        )

        // Nächstes Frame anfordern — treibt Lauf- und Ambient-Animation
        // vsync-getaktet. Solange die View sichtbar ist, läuft das weiter;
        // bei pausierter Activity stellt das System das Zeichnen ein.
        if (animate) postInvalidateOnAnimation()
    }

    /** Erlaubt der Activity, die Animationsschleife bei onPause anzuhalten. */
    var animate: Boolean = true
        set(value) {
            field = value
            if (value) postInvalidateOnAnimation()
        }

    private fun arriveAtTarget() {
        pendingHotspot?.let { hs ->
            pendingHotspot = null
            listener?.onHotspotReached(hs)
        }
        pendingExit?.let { ex ->
            pendingExit = null
            listener?.onExitReached(ex)
        }
    }

    private fun drawExits(canvas: Canvas, s: GameState, w: Float, h: Float) {
        val arrow = PaintKit.solid(Color.argb(150, 245, 241, 230))
        GameWorld.exitsFor(s, s.locationId).forEach { exit ->
            val cx = exit.xNorm * w
            val cy = h * 0.40f
            val size = h * 0.035f
            val pointsRight = exit.xNorm > 0.5f
            val path = android.graphics.Path().apply {
                if (pointsRight) {
                    moveTo(cx - size, cy - size); lineTo(cx + size, cy); lineTo(cx - size, cy + size)
                } else {
                    moveTo(cx + size, cy - size); lineTo(cx - size, cy); lineTo(cx + size, cy + size)
                }
                close()
            }
            canvas.drawPath(path, arrow)
        }
    }
}
