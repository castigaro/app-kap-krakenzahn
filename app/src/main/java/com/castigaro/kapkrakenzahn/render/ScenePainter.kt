package com.castigaro.kapkrakenzahn.render

import android.graphics.Canvas
import com.castigaro.kapkrakenzahn.data.GameState
import com.castigaro.kapkrakenzahn.render.scenes.BeachScene
import com.castigaro.kapkrakenzahn.render.scenes.CaveScene
import com.castigaro.kapkrakenzahn.render.scenes.CliffScene
import com.castigaro.kapkrakenzahn.render.scenes.HarborScene
import com.castigaro.kapkrakenzahn.render.scenes.JungleScene
import com.castigaro.kapkrakenzahn.render.scenes.LighthouseScene
import com.castigaro.kapkrakenzahn.render.scenes.MarketScene
import com.castigaro.kapkrakenzahn.render.scenes.TavernScene
import com.castigaro.kapkrakenzahn.world.LocationId

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
