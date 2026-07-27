package com.corphish.quicktools.actions.handler

import com.corphish.quicktools.actions.model.ActionType
import javax.inject.Inject
import org.json.JSONObject

/**
 * Busca e substitui na seleção.
 * Config: `{"find":"foo","replace":"bar","regex":false,"ignoreCase":true,"all":true}`
 */
class FindReplaceHandler @Inject constructor() : ActionHandler {

    override val type: ActionType = ActionType.FIND_REPLACE

    override suspend fun execute(ctx: ActionContext, config: JSONObject): ActionResult {
        val find = config.optString("find")
        if (find.isEmpty()) return ActionResult.Error("find ausente")
        val replace = config.optString("replace", "")
        val useRegex = config.optBoolean("regex", false)
        val ignoreCase = config.optBoolean("ignoreCase", false)
        val all = config.optBoolean("all", true)

        val out = runCatching {
            if (useRegex) {
                val opts = if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
                val re = Regex(find, opts)
                if (all) ctx.input.replace(re, replace) else ctx.input.replaceFirst(re, replace)
            } else {
                if (all) ctx.input.replace(find, replace, ignoreCase)
                else ctx.input.replaceFirst(find, replace, ignoreCase)
            }
        }.getOrElse { return ActionResult.Error("regex inválido: ${it.message}") }
        return ActionResult.Text(out)
    }
}
