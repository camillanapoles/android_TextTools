package com.corphish.quicktools.actions.handler

import com.corphish.quicktools.actions.model.ActionType
import com.corphish.quicktools.functions.TextFunctions
import javax.inject.Inject
import org.json.JSONObject

/**
 * Aplica uma das transformações puras de [TextFunctions] selecionada por `op`.
 * Config: `{"op":"UPPERCASE"}` ou `{"op":"ADD_PREFIX","arg":"> "}`.
 */
class TransformHandler @Inject constructor(
    private val fn: TextFunctions,
) : ActionHandler {

    override val type: ActionType = ActionType.TRANSFORM

    override suspend fun execute(ctx: ActionContext, config: JSONObject): ActionResult {
        val op = config.optString("op").uppercase().ifEmpty { return ActionResult.Error("op ausente") }
        val arg = config.optString("arg")
        val s = ctx.input
        val out = runCatching {
            when (op) {
                "UPPERCASE" -> fn.changeCase(s, 0)
                "LOWERCASE" -> fn.changeCase(s, 1)
                "TITLE_CASE" -> fn.changeCase(s, 2)
                "TITLE_FIRST" -> fn.changeCase(s, 3)
                "RANDOM_CASE" -> fn.changeCase(s, 4)
                "WRAP_SINGLE" -> fn.presetWrap(s, 0)
                "WRAP_DOUBLE" -> fn.presetWrap(s, 1)
                "WRAP_PAREN" -> fn.presetWrap(s, 2)
                "WRAP_BRACE" -> fn.presetWrap(s, 3)
                "WRAP_BRACKET" -> fn.presetWrap(s, 4)
                "WRAP_CUSTOM" -> if (arg.isNotEmpty()) fn.customWrap(s, arg) else s
                "SORT_LINES" -> fn.sortLines(s)
                "REVERSE_LINES" -> fn.reverseLines(s)
                "REVERSE_CHARS" -> fn.reverseText(s)
                "REVERSE_WORDS" -> fn.reverseWords(s)
                "NUMBER_LINES" -> fn.numberLines(s)
                "REMOVE_EMPTY_LINES" -> fn.removeEmptyLines(s)
                "REMOVE_DUPLICATE_LINES" -> fn.removeDuplicateLines(s)
                "REMOVE_DUPLICATE_WORDS" -> fn.removeDuplicateWords(s)
                "REMOVE_WHITESPACES" -> fn.removeWhiteSpaces(s)
                "REMOVE_LINEBREAKS" -> fn.removeLineBreaks(s)
                "TRIM" -> s.trim()
                "ADD_PREFIX" -> if (arg.isNotEmpty()) fn.addPrefix(s, arg) else s
                "ADD_SUFFIX" -> if (arg.isNotEmpty()) fn.addSuffix(s, arg) else s
                "REPEAT" -> arg.toIntOrNull()?.let { fn.repeatText(s, it) } ?: s
                "BOLD_SERIF" -> fn.boldSerif(s)
                "ITALIC_SERIF" -> fn.italicSerif(s)
                "BOLD_ITALIC_SERIF" -> fn.boldItalicSerif(s)
                "BOLD_SANS" -> fn.boldSans(s)
                "ITALIC_SANS" -> fn.italicSans(s)
                "BOLD_ITALIC_SANS" -> fn.boldItalicSans(s)
                "STRIKETHROUGH_SHORT" -> fn.shortStrikethrough(s)
                "STRIKETHROUGH_LONG" -> fn.longStrikethrough(s)
                "CURSIVE" -> fn.cursive(s)
                "MONOSPACE" -> fn.monospaceFont(s)
                "CLEAR_UNICODE" -> fn.clearUnicodeFormatting(s)
                "LINEBREAK_BY_WORDS" -> arg.toIntOrNull()?.let { fn.lineBreakByWords(s, it) } ?: s
                "SQUEEZE" -> arg.toIntOrNull()?.let { fn.squeeze(s, it) } ?: s
                "PREPEND_LINES" -> if (arg.isNotEmpty()) fn.prependLines(s, arg) else s
                "APPEND_LINES" -> if (arg.isNotEmpty()) fn.appendLines(s, arg) else s
                else -> return ActionResult.Error("op desconhecida: $op")
            }
        }.getOrElse { return ActionResult.Error("falha: ${it.message}") }
        return ActionResult.Text(out)
    }
}
