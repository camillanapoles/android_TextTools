package com.corphish.quicktools.actions.model

/**
 * Tipo de handler que executa a ação. É a única fronteira compile-time:
 * adicionar um novo tipo exige código (+ rebuild); novas INSTÂNCIAS de tipo
 * existente são apenas dados no store (zero rebuild).
 */
enum class ActionType {
    TRANSFORM,
    EXTRACT,
    FIND_REPLACE,
    TEMPLATE,
    ANALYZE,
    TRANSLATE,
    SCRIPT,
    AI_PROMPT,
    PIPELINE;

    companion object {
        /** Tipos cujo handler já está implementado nesta build. */
        val IMPLEMENTED: Set<ActionType> = setOf(
            TRANSFORM, EXTRACT, FIND_REPLACE, TEMPLATE, ANALYZE
        )
    }
}

/** Destino do resultado da ação. */
enum class ResultMode {
    REPLACE,  // substitui in-place se campo editável, senão clipboard
    COPY,     // sempre clipboard
    DISPLAY,  // mostra em tela (análise)
    SHARE     // ACTION_SEND
}

/** Origem da ação. */
enum class Source { MANUAL, AI, IMPORTED }
