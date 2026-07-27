package com.corphish.quicktools.actions.handler

import android.content.Context
import org.json.JSONObject

/** Contexto de execução entregue a todo handler. */
data class ActionContext(
    val input: String,
    val readOnly: Boolean,
    val context: Context,
)

/** Resultado selado: o launcher decide o destino conforme [resultMode] da ação. */
sealed interface ActionResult {
    /** Texto transformado — para REPLACE (in-place/clipboard) ou COPY. */
    data class Text(val value: String) : ActionResult
    /** Conteúdo estruturado para DISPLAY (chave -> valor). */
    data class Display(val title: String, val sections: List<Pair<String, String>>) : ActionResult
    /** Texto para SHARE via ACTION_SEND. */
    data class Share(val value: String, val mimeType: String = "text/plain") : ActionResult
    /** Falha de execução. */
    data class Error(val message: String) : ActionResult
}

/**
 * Strategy por [com.corphish.quicktools.actions.model.ActionType].
 * Cada implementação é compilada no APK; o [HandlerRegistry] resolve em runtime.
 */
interface ActionHandler {
    val type: com.corphish.quicktools.actions.model.ActionType

    /**
     * Executa a ação sobre [ctx.input] usando [config] (JSON específico do tipo).
     */
    suspend fun execute(ctx: ActionContext, config: JSONObject): ActionResult
}
