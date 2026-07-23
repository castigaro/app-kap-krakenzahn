package com.appsonar.kapkrakenzahn.api

import android.content.Context
import com.appsonar.common.llm.ModelPricing
import com.appsonar.common.llm.ProviderSettings
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Gemeinsamer HTTP-Kern für Anthropic (Messages API) und OpenAI (Chat
 * Completions). Schickt System-Prompt plus eine Reihe abwechselnder
 * Gesprächsbeiträge (Nutzer/Assistent), bucht die geschätzten Kosten und
 * liefert den rohen Antworttext. Gleiche Bauart wie in den anderen
 * AppSonar-Apps.
 */
object LlmClient {

    private val JSON = "application/json".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    class ApiException(message: String) : IOException(message)

    /** Ein Gesprächsbeitrag für die Chat-Anfrage. */
    class Turn(val role: String, val text: String) {
        companion object {
            const val ROLE_USER = "user"
            const val ROLE_ASSISTANT = "assistant"
        }
    }

    /** Für Tests überschreibbar (MockWebServer statt echter Anbieter-URLs). */
    var anthropicUrl = "https://api.anthropic.com/v1/messages"
    var openAiUrl = "https://api.openai.com/v1/chat/completions"

    fun chat(context: Context, systemPrompt: String, turns: List<Turn>): String {
        val config = ProviderSettings.activeConfig(context)
            ?: throw ApiException("Kein aktiver API-Key — in den Einstellungen hinterlegen oder aktivieren.")
        return if (config.provider == ProviderSettings.PROVIDER_OPENAI) {
            requestOpenAi(context, config, systemPrompt, turns)
        } else {
            requestAnthropic(context, config, systemPrompt, turns)
        }
    }

    private fun trackCost(
        context: Context,
        provider: String,
        model: String,
        inputTokens: Long,
        outputTokens: Long,
    ) {
        val micros = ModelPricing.estimateCostMicros(provider, model, inputTokens, outputTokens)
        ProviderSettings.addCostMicros(context, provider, micros)
    }

    private fun requestAnthropic(
        context: Context,
        config: ProviderSettings.ActiveConfig,
        systemPrompt: String,
        turns: List<Turn>,
    ): String {
        val messages = JSONArray()
        turns.forEach { turn ->
            messages.put(JSONObject().apply {
                put("role", turn.role)
                put("content", turn.text)
            })
        }
        val body = JSONObject().apply {
            put("model", config.model)
            put("max_tokens", 1024)
            put("system", systemPrompt)
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(anthropicUrl)
            .header("x-api-key", config.apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(body.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw ApiException(apiErrorMessage(responseText, response.code))
            }
            val json = JSONObject(responseText)
            json.optJSONObject("usage")?.let { usage ->
                trackCost(
                    context, ProviderSettings.PROVIDER_ANTHROPIC, config.model,
                    inputTokens = usage.optLong("input_tokens", 0L),
                    outputTokens = usage.optLong("output_tokens", 0L),
                )
            }
            if (json.optString("stop_reason") == "refusal") {
                throw ApiException("Die Anfrage wurde vom Modell abgelehnt.")
            }
            val contentArr = json.getJSONArray("content")
            for (i in 0 until contentArr.length()) {
                val block = contentArr.getJSONObject(i)
                if (block.getString("type") == "text") {
                    return block.getString("text").trim()
                }
            }
            throw ApiException("Leere Antwort vom Modell.")
        }
    }

    private fun requestOpenAi(
        context: Context,
        config: ProviderSettings.ActiveConfig,
        systemPrompt: String,
        turns: List<Turn>,
    ): String {
        val messages = JSONArray().put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })
        turns.forEach { turn ->
            messages.put(JSONObject().apply {
                put("role", turn.role)
                put("content", turn.text)
            })
        }
        val body = JSONObject().apply {
            put("model", config.model)
            put("max_tokens", 1024)
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(openAiUrl)
            .header("Authorization", "Bearer ${config.apiKey}")
            .post(body.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw ApiException(apiErrorMessage(responseText, response.code))
            }
            val json = JSONObject(responseText)
            json.optJSONObject("usage")?.let { usage ->
                trackCost(
                    context, ProviderSettings.PROVIDER_OPENAI, config.model,
                    inputTokens = usage.optLong("prompt_tokens", 0L),
                    outputTokens = usage.optLong("completion_tokens", 0L),
                )
            }
            return json.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content").trim()
        }
    }

    private fun apiErrorMessage(responseText: String, code: Int): String {
        val detail = runCatching {
            JSONObject(responseText).getJSONObject("error").getString("message")
        }.getOrNull()
        return when {
            detail != null -> "API-Fehler ($code): $detail"
            code == 401 -> "API-Key ungültig (401)."
            code == 429 -> "Rate-Limit erreicht (429) — bitte kurz warten."
            else -> "API-Fehler ($code)."
        }
    }

    /** Zieht das JSON-Objekt aus der Modellantwort (toleriert ```-Zäune und Fließtext). */
    fun extractJson(text: String): JSONObject {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start < 0 || end <= start) throw ApiException("Antwort nicht lesbar: $text")
        return JSONObject(text.substring(start, end + 1))
    }
}
