package com.corphish.quicktools.actions.handler

import com.corphish.quicktools.actions.model.ActionType
import java.net.URLEncoder
import javax.inject.Inject
import org.json.JSONObject

/**
 * Substitui o placeholder `{{text}}` pelo texto selecionado.
 * Config: `{"template":"https://google.com/search?q={{text}}","encode":true}`
 */
class TemplateHandler @Inject constructor() : ActionHandler {

    override val type: ActionType = ActionType.TEMPLATE

    override suspend fun execute(ctx: ActionContext, config: JSONObject): ActionResult {
        val template = config.optString("template").ifEmpty { return ActionResult.Error("template ausente") }
        val encode = config.optBoolean("encode", false)
        val value = if (encode) URLEncoder.encode(ctx.input, "UTF-8") else ctx.input
        return ActionResult.Text(template.replace("{{text}}", value))
    }
}
