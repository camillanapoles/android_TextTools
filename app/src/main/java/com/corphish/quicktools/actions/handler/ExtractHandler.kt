package com.corphish.quicktools.actions.handler

import com.corphish.quicktools.actions.model.ActionType
import javax.inject.Inject
import org.json.JSONObject

/**
 * Extrai matches de um padrão (preset nomeado ou regex cru) e os junta.
 * Config: `{"pattern":"EMAIL","join":"\n","unique":true}`
 *         ou `{"pattern":"\\b\\d+\\b","join":", ","unique":false}`.
 */
class ExtractHandler @Inject constructor() : ActionHandler {

    override val type: ActionType = ActionType.EXTRACT

    override suspend fun execute(ctx: ActionContext, config: JSONObject): ActionResult {
        val patternKey = config.optString("pattern").ifEmpty { return ActionResult.Error("pattern ausente") }
        val join = config.optString("join", "\n")
        val unique = config.optBoolean("unique", true)
        val regex = PRESETS[patternKey] ?: runCatching { Regex(patternKey) }
            .getOrElse { return ActionResult.Error("regex inválido: ${it.message}") }

        val matches = regex.findAll(ctx.input).map { it.value }.toList()
        val result = (if (unique) matches.distinct() else matches).joinToString(join)
        return ActionResult.Text(result)
    }

    companion object {
        private val PRESETS: Map<String, Regex> = mapOf(
            "EMAIL" to Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),
            "URL" to Regex("https?://[\\w\\d:#@%/\\$()~_?+\\-=\\\\.&]*"),
            "PHONE" to Regex("(\\+?\\d{1,3}[- ]?)?(\\d{10}|\\d{3}[- ]\\d{3}[- ]\\d{4}|\\d{3}[- ]\\d{4})"),
            "IP" to Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"),
            "DATE" to Regex("\\b\\d{1,4}[-/.]\\d{1,2}[-/.]\\d{1,4}\\b"),
            "TIME" to Regex("\\b\\d{1,2}:\\d{2}(?::\\d{2})?\\b"),
            "HEX" to Regex("\\b(?:0x)?[A-F0-9]{2,}\\b", RegexOption.IGNORE_CASE),
            "BINARY" to Regex("\\b[01]{8,}\\b"),
        )
    }
}
