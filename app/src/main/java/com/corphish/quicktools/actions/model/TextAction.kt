package com.corphish.quicktools.actions.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * A funcionalidade como OBJETO. Persistida, mutável em runtime, sem rebuild.
 * É a unidade atômica do sistema: criar/ativar/desativar/editar uma ação
 * não toca no código do app.
 */
data class TextAction(
    val id: Long,
    val name: String,
    val description: String? = null,
    val type: ActionType,
    val resultMode: ResultMode = ResultMode.REPLACE,
    val configJson: String,
    val iconKey: String = "ic_action",
    val enabled: Boolean = true,
    val order: Int = 0,
    val tags: List<String> = emptyList(),
    val source: Source = Source.MANUAL,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("description", description ?: JSONObject.NULL)
        put("type", type.name)
        put("resultMode", resultMode.name)
        put("configJson", configJson)
        put("iconKey", iconKey)
        put("enabled", enabled)
        put("order", order)
        put("tags", JSONArray(tags))
        put("source", source.name)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    companion object {
        fun fromJson(j: JSONObject): TextAction = TextAction(
            id = j.getLong("id"),
            name = j.getString("name"),
            description = if (j.isNull("description")) null else j.optString("description", null),
            type = runCatching { ActionType.valueOf(j.getString("type")) }.getOrDefault(ActionType.TRANSFORM),
            resultMode = runCatching { ResultMode.valueOf(j.getString("resultMode")) }.getOrDefault(ResultMode.REPLACE),
            configJson = j.optString("configJson", "{}"),
            iconKey = j.optString("iconKey", "ic_action"),
            enabled = j.optBoolean("enabled", true),
            order = j.optInt("order", 0),
            tags = runCatching {
                val arr = j.getJSONArray("tags")
                buildList { for (i in 0 until arr.length()) add(arr.getString(i)) }
            }.getOrDefault(emptyList()),
            source = runCatching { Source.valueOf(j.getString("source")) }.getOrDefault(Source.MANUAL),
            createdAt = j.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = j.optLong("updatedAt", System.currentTimeMillis()),
        )

        /** Cria uma nova ação com id provisório (store atribui id definitivo). */
        fun draft(
            name: String,
            type: ActionType,
            configJson: String,
            resultMode: ResultMode = ResultMode.REPLACE,
        ) = TextAction(
            id = 0,
            name = name,
            type = type,
            resultMode = resultMode,
            configJson = configJson,
        )
    }
}
