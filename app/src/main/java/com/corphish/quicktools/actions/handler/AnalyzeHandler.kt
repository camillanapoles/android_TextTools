package com.corphish.quicktools.actions.handler

import com.corphish.quicktools.actions.model.ActionType
import javax.inject.Inject
import org.json.JSONObject

/**
 * Calcula métricas do texto e retorna Display.
 * Config: `{"metrics":["chars","words","lines","letters","digits","whitespaces"]}`
 */
class AnalyzeHandler @Inject constructor() : ActionHandler {

    override val type: ActionType = ActionType.ANALYZE

    override suspend fun execute(ctx: ActionContext, config: JSONObject): ActionResult {
        val s = ctx.input
        val requested = runCatching {
            val arr = config.getJSONArray("metrics")
            buildList { for (i in 0 until arr.length()) add(arr.getString(i)) }
        }.getOrDefault(DEFAULT_METRICS)

        val available = mapOf(
            "chars" to ("Characters" to s.length.toString()),
            "letters" to ("Letters" to s.count { it.isLetter() }.toString()),
            "digits" to ("Digits" to s.count { it.isDigit() }.toString()),
            "whitespaces" to ("Whitespaces" to s.count { it.isWhitespace() }.toString()),
            "words" to ("Words" to s.split(Regex("\\s+")).filter { it.isNotEmpty() }.size.toString()),
            "lines" to ("Lines" to (if (s.isEmpty()) 0 else s.lines().size).toString()),
            "paragraphs" to ("Paragraphs" to s.split(Regex("\n\\s*\n")).filter { it.isNotBlank() }.size.toString()),
            "sentences" to ("Sentences" to s.split(Regex("[.!?]+")).filter { it.isNotBlank() }.size.toString()),
        )

        val sections = requested.mapNotNull { key -> available[key] }
            .ifEmpty { available.values.take(DEFAULT_METRICS.size) }

        return ActionResult.Display(title = "Text Analysis", sections = sections)
    }

    companion object {
        private val DEFAULT_METRICS = listOf("chars", "words", "lines", "letters")
    }
}
