package com.castigaro.kapkrakenzahn.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.castigaro.common.llm.ProviderSettings
import com.castigaro.kapkrakenzahn.world.Effect
import com.castigaro.kapkrakenzahn.world.GameWorld
import com.castigaro.kapkrakenzahn.world.ItemId
import com.castigaro.kapkrakenzahn.world.NpcId
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DialogApiTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var server: MockWebServer

    @Before fun setup() {
        server = MockWebServer()
        server.start()
        LlmClient.anthropicUrl = server.url("/anthropic").toString()
        LlmClient.openAiUrl = server.url("/openai").toString()
    }

    @After fun teardown() {
        server.shutdown()
    }

    private fun anthropicKey() =
        ProviderSettings.save(context, ProviderSettings.PROVIDER_ANTHROPIC, "k", "claude-sonnet-5", true, "", "", false)

    private fun openAiKey() =
        ProviderSettings.save(context, ProviderSettings.PROVIDER_OPENAI, "", "", false, "k", "gpt-4o-mini", true)

    @Test fun `Anthropic-Antwort wird geparst`() {
        anthropicKey()
        val json = """{"sag":"Moin.","optionen":["Frage","Tschüss."],"effekte":[]}"""
        server.enqueue(
            MockResponse().setBody(
                """{"content":[{"type":"text","text":${org.json.JSONObject.quote(json)}}],"usage":{"input_tokens":5,"output_tokens":9}}""",
            ),
        )
        val state = GameWorld.newGame(1L, "T")
        val turn = runBlocking { DialogApi.turn(context, NpcId.YOLA, state, null) }
        assertEquals("Moin.", turn.sag)
        assertEquals(2, turn.optionen.size)
    }

    @Test fun `OpenAI-Antwort wird geparst`() {
        openAiKey()
        val json = """{"sag":"Hey.","optionen":["A","B","Tschüss."],"effekte":[]}"""
        server.enqueue(
            MockResponse().setBody(
                """{"choices":[{"message":{"content":${org.json.JSONObject.quote(json)}}}],"usage":{"prompt_tokens":3,"completion_tokens":4}}""",
            ),
        )
        val state = GameWorld.newGame(1L, "T")
        val turn = runBlocking { DialogApi.turn(context, NpcId.YOLA, state, null) }
        assertEquals("Hey.", turn.sag)
        assertEquals(3, turn.optionen.size)
    }

    @Test fun `nicht gewhitelistete Effekte werden verworfen`() {
        val state = GameWorld.newGame(1L, "T")
        val allowed = GameWorld.allowedEffectsFor(state, NpcId.YOLA)
        // STINKFISCH ist bei Yola erlaubt, das Feuer-Flag niemals.
        val raw = """```json
            {"sag":"...","optionen":["Danke","Tschüss."],
             "effekte":["item:STINKFISCH","flag:FEUER_ENTZUENDET"]}
            ```"""
        val turn = DialogApi.parseTurn(raw, state, NpcId.YOLA, allowed)
        assertEquals(1, turn.effects.size)
        assertTrue(turn.effects.first() is Effect.AddItem)
        assertEquals(ItemId.STINKFISCH, (turn.effects.first() as Effect.AddItem).item)
    }

    @Test fun `leere Optionen werden mit Abschied aufgefuellt`() {
        val state = GameWorld.newGame(1L, "T")
        val allowed = GameWorld.allowedEffectsFor(state, NpcId.GRIET)
        val turn = DialogApi.parseTurn("""{"sag":"Hm.","optionen":[],"effekte":[]}""", state, NpcId.GRIET, allowed)
        assertTrue(turn.optionen.isNotEmpty())
    }

    @Test(expected = LlmClient.ApiException::class)
    fun `API-Fehler wird als ApiException gemeldet`() {
        anthropicKey()
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":{"message":"bad key"}}"""))
        val state = GameWorld.newGame(1L, "T")
        runBlocking { DialogApi.turn(context, NpcId.YOLA, state, null) }
    }
}
