package com.appsonar.kapkrakenzahn.render

import android.graphics.Canvas
import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.render.scenes.BeachScene
import com.appsonar.kapkrakenzahn.render.scenes.CaveScene
import com.appsonar.kapkrakenzahn.render.scenes.CliffScene
import com.appsonar.kapkrakenzahn.render.scenes.HarborScene
import com.appsonar.kapkrakenzahn.render.scenes.JungleScene
import com.appsonar.kapkrakenzahn.render.scenes.LighthouseScene
import com.appsonar.kapkrakenzahn.render.scenes.MarketScene
import com.appsonar.kapkrakenzahn.render.scenes.TavernScene
import com.appsonar.kapkrakenzahn.world.LocationId

/**
 * Malt den Hintergrund eines Schauplatzes (ohne Held, Hotspots und HUD).
 * Jede Szene zeichnet flächig in drei Ebenen; Animationen kommen aus
 * [timeMs]. Der [GameState] steuert zustandsabhängige Details (Sturm,
 * offene Tür, Lichtquelle in der Höhle).
 */
interface ScenePainter {
    fun draw(canvas: Canvas, w: Float, h: Float, state: GameState, timeMs: Long)

    companion object {
        fun forLocation(id: LocationId): ScenePainter = when (id) {
            LocationId.HAFEN -> HarborScene
            LocationId.TAVERNE -> TavernScene
            LocationId.MARKT -> MarketScene
            LocationId.STRAND -> BeachScene
            LocationId.DSCHUNGEL -> JungleScene
            LocationId.KLIPPEN -> CliffScene
            LocationId.HOEHLE -> CaveScene
            LocationId.LEUCHTTURM -> LighthouseScene
        }
    }
}
