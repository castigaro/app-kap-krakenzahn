package com.castigaro.kapkrakenzahn.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.world.Exit
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.Hotspot
import com.castigaro.kapkrakenzahn.world.NpcId
import kotlin.math.abs
import kotlin.math.hypot

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
        /** Ein Hotspot wurde angetippt (Held läuft noch) → Satzzeile aktualisieren. */
        fun onHotspotTargeted(hotspot: Hotspot) {}

        /** Held ist beim Hotspot angekommen → aktuelles Verb ausführen. */
        fun onHotspotReached(hotspot: Hotspot)

        /** Held ist nach einem Doppeltipp angekommen → Standard-Aktion ausführen. */
        fun onHotspotDefaultAction(hotspot: Hotspot) {}

        /** Held ist an einem Ausgang angekommen. */
        fun onExitReached(exit: Exit)

        /** Leere Stelle angetippt (nur Laufen). */
        fun onWalkOnly() {}
    }

    var listener: Listener? = null

    private var state: GameState? = null
    private var painter: ScenePainter? = null

    // Lauf-Animation (frei über die begehbare Boden-Bande, x und y).
    private var heroX = 0.5f
    private var heroY = 0.86f
    private var targetX = 0.5f
    private var targetY = 0.86f
    private var walking = false
    private var facingRight = true
    private var walkPhase = 0f
    private var pendingHotspot: Hotspot? = null
    private var pendingExit: Exit? = null

    // Doppeltipp-Erkennung: zweiter Tipp auf denselben Hotspot innerhalb des
    // Zeitfensters löst bei Ankunft die Standard-Aktion statt „Gehe zu" aus.
    private var lastTapHotspotId: String? = null
    private var lastTapAt = 0L
    private var defaultOnArrive = false
    private val doubleTapWindow =
        android.view.ViewConfiguration.getDoubleTapTimeout().toLong()

    private var lastFrame = 0L
    private val startTime = System.currentTimeMillis()

    private val walkSpeed = 0.28f // Bildschirmbreiten pro Sekunde

    // Begehbare Boden-Bande je Ort (vorn = walkY, hinten = walkFarY).
    private val nearY get() = state?.let { GameWorld.location(it.locationId).walkY } ?: 0.86f
    private val farY get() = state?.let { GameWorld.location(it.locationId).walkFarY } ?: 0.74f

    private fun clampToBand(y: Float): Float =
        y.coerceIn(minOf(nearY, farY), maxOf(nearY, farY))

    // Tiefenskalierung der Figur: an die absolute Bildschirm-y gekoppelt
    // (perspektivisch konsistent, unabhängig von der Banden-Feinabstimmung).
    private val refTopY = 0.55f       // grober Fluchthorizont
    private val refBottomY = 1.0f
    private val heightAtTop = 0.18f   // ganz hinten
    private val heightAtBottom = 0.36f // ganz vorn

    private fun heightNormAt(y: Float): Float {
        val t = ((y - refTopY) / (refBottomY - refTopY)).coerceIn(0f, 1f)
        return heightAtTop + (heightAtBottom - heightAtTop) * t
    }

    private fun heroHeightNorm(): Float = heightNormAt(heroY)

    // Pixel-Look: Szene wird in einen niedrig aufgelösten Puffer gemalt und
    // mit Nearest-Neighbor auf die View hochskaliert (kein FILTER_BITMAP).
    private val pixelTargetWidth = 440
    private var buffer: android.graphics.Bitmap? = null
    private var bufferCanvas: Canvas? = null
    private val blitPaint = Paint().apply { isFilterBitmap = false; isDither = false }

    private fun ensureBuffer(w: Int, h: Int): android.graphics.Bitmap {
        val bw = pixelTargetWidth
        val bh = (pixelTargetWidth.toLong() * h / w).toInt().coerceAtLeast(1)
        val current = buffer
        if (current == null || current.width != bw || current.height != bh) {
            current?.recycle()
            val fresh = android.graphics.Bitmap.createBitmap(bw, bh, android.graphics.Bitmap.Config.ARGB_8888)
            buffer = fresh
            bufferCanvas = Canvas(fresh)
            return fresh
        }
        return current
    }

    fun bind(state: GameState) {
        this.state = state
        this.painter = ScenePainter.forLocation(state.locationId)
        heroX = state.heroX
        heroY = state.heroY
        targetX = state.heroX
        targetY = state.heroY
        walking = false
        pendingHotspot = null
        pendingExit = null
        lastTapHotspotId = null
        defaultOnArrive = false
        invalidate()
    }

    /** Setzt die Heldin ohne Laufanimation an eine Position (nach Ortswechsel). */
    fun placeHero(x: Float, y: Float) {
        heroX = x
        heroY = y
        targetX = x
        targetY = y
        walking = false
        state?.heroX = x
        state?.heroY = y
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
            val now = android.os.SystemClock.uptimeMillis()
            val isDouble = hotspot.id == lastTapHotspotId && now - lastTapAt <= doubleTapWindow
            lastTapHotspotId = hotspot.id
            lastTapAt = now
            listener?.onHotspotTargeted(hotspot)
            // Steht die Heldin schon am Ziel, kommt die „Ankunft" im nächsten
            // Frame (dist < 1e-4) — der Doppeltipp braucht daher keinen Timer.
            walkTo(hotspot.anchorX, hotspot.anchorY ?: clampToBand(hotspot.rect.bottom))
            pendingHotspot = hotspot
            pendingExit = null
            defaultOnArrive = isDouble
            return true
        }
        lastTapHotspotId = null
        defaultOnArrive = false
        // Ausgänge (Pfeile am Rand, unterer Bereich)
        val exit = GameWorld.exitsFor(s, s.locationId)
            .firstOrNull { abs(nx - it.xNorm) < 0.08f && ny > 0.45f }
        if (exit != null) {
            walkTo(exit.xNorm, nearY)
            pendingExit = exit
            pendingHotspot = null
            return true
        }
        // Sonst frei zum getippten Punkt der Boden-Bande laufen.
        walkTo(nx.coerceIn(0.05f, 0.95f), clampToBand(ny))
        pendingHotspot = null
        pendingExit = null
        listener?.onWalkOnly()
        return true
    }

    private fun walkTo(x: Float, y: Float) {
        targetX = x
        targetY = clampToBand(y)
        facingRight = x >= heroX
        walking = true
        lastFrame = System.currentTimeMillis()
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        val s = state ?: return
        val p = painter ?: return
        if (width == 0 || height == 0) return
        val now = System.currentTimeMillis()

        // In den niedrig aufgelösten Puffer malen …
        val bmp = ensureBuffer(width, height)
        val bc = bufferCanvas ?: return
        val w = bmp.width.toFloat()
        val h = bmp.height.toFloat()

        // Puffer bleibt zwischen Frames bestehen — vor dem Neuzeichnen leeren.
        bmp.eraseColor(Color.BLACK)
        p.draw(bc, w, h, s, now - startTime)

        // Ausgangs-Pfeile
        drawExits(bc, s, w, h)

        // Sichtbare NPC-Figuren einsammeln (Bedingungen wie Licht/Ereignis
        // stecken schon in hotspotsFor; Krächz malt die Hafenszene selbst).
        val npcs = GameWorld.hotspotsFor(s, s.locationId)
            .filter { it.npc != null && it.npc != NpcId.KRAECHZ }
            .map { it to NpcSprite.placement(it) }

        // Held frei über die Boden-Bande bewegen (diagonal zum Zielpunkt).
        if (walking) {
            val dt = (now - lastFrame).coerceAtMost(64L) / 1000f
            lastFrame = now
            val dx = targetX - heroX
            val dy = targetY - heroY
            val dist = hypot(dx, dy)
            val step = walkSpeed * dt
            if (dist <= step || dist < 1e-4f) {
                heroX = targetX
                heroY = targetY
                walking = false
                s.heroX = heroX
                s.heroY = heroY
                arriveAtTarget()
            } else {
                heroX += dx / dist * step
                heroY += dy / dist * step
                walkPhase += dt * 10f
            }
        }

        // Maler-Reihenfolge: NPCs hinter der Heldin zuerst, davor stehende danach.
        val elapsed = now - startTime
        npcs.filter { it.second.groundY <= heroY }.forEach { drawNpc(bc, w, h, it, elapsed) }

        // Heldin am Bodenpunkt, Größe nach Tiefe skaliert.
        HeroSprite.draw(
            bc, w, h, heroX, heroY, heightNorm = heroHeightNorm(),
            phase = walkPhase, facingRight = facingRight, walking = walking,
        )

        npcs.filter { it.second.groundY > heroY }.forEach { drawNpc(bc, w, h, it, elapsed) }

        // … und pixelscharf hochskalieren.
        canvas.drawBitmap(bmp, srcRect(bmp), dstRect(), blitPaint)

        // Nächstes Frame anfordern — treibt Lauf- und Ambient-Animation
        // vsync-getaktet. Solange die View sichtbar ist, läuft das weiter;
        // bei pausierter Activity stellt das System das Zeichnen ein.
        if (animate) postInvalidateOnAnimation()
    }

    private val srcRectCache = android.graphics.Rect()
    private val dstRectCache = android.graphics.Rect()
    private fun srcRect(bmp: android.graphics.Bitmap): android.graphics.Rect =
        srcRectCache.apply { set(0, 0, bmp.width, bmp.height) }
    private fun dstRect(): android.graphics.Rect =
        dstRectCache.apply { set(0, 0, width, height) }

    /** Erlaubt der Activity, die Animationsschleife bei onPause anzuhalten. */
    var animate: Boolean = true
        set(value) {
            field = value
            if (value) postInvalidateOnAnimation()
        }

    /** Malt eine NPC-Figur; eine optionale Sichtkante klippt den Unterkörper
     *  (Griet hinter der Theke). */
    private fun drawNpc(
        canvas: Canvas, w: Float, h: Float,
        npc: Pair<Hotspot, NpcSprite.Placement>, timeMs: Long,
    ) {
        val (hotspot, place) = npc
        val id = hotspot.npc ?: return
        val clip = place.clipBottomNorm
        if (clip != null) {
            canvas.save()
            canvas.clipRect(0f, 0f, w, clip * h)
        }
        NpcSprite.draw(canvas, w, h, id, place.x, place.groundY, heightNormAt(place.groundY), timeMs)
        if (clip != null) canvas.restore()
    }

    private fun arriveAtTarget() {
        val viaDoubleTap = defaultOnArrive
        defaultOnArrive = false
        pendingHotspot?.let { hs ->
            pendingHotspot = null
            if (viaDoubleTap) listener?.onHotspotDefaultAction(hs)
            else listener?.onHotspotReached(hs)
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
