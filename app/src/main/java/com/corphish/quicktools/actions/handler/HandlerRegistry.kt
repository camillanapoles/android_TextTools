package com.corphish.quicktools.actions.handler

import com.corphish.quicktools.actions.model.ActionType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolve [ActionType] -> [ActionHandler] em runtime.
 *
 * Tipos não implementados nesta build (TRANSLATE, SCRIPT, AI_PROMPT, PIPELINE)
 * retornam null — o launcher apresenta "handler indisponível" de forma honesta,
 * sem stub. Esses tipos entram em builds futuras pelo MESMO registry, sem
 * mudança arquitetural.
 */
@Singleton
class HandlerRegistry @Inject constructor(
    transform: TransformHandler,
    extract: ExtractHandler,
    findReplace: FindReplaceHandler,
    template: TemplateHandler,
    analyze: AnalyzeHandler,
) {
    private val handlers: Map<ActionType, ActionHandler> = mapOf(
        ActionType.TRANSFORM to transform,
        ActionType.EXTRACT to extract,
        ActionType.FIND_REPLACE to findReplace,
        ActionType.TEMPLATE to template,
        ActionType.ANALYZE to analyze,
    )

    fun resolve(type: ActionType): ActionHandler? = handlers[type]

    fun isImplemented(type: ActionType): Boolean = type in handlers
}
