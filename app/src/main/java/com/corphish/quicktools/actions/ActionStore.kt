package com.corphish.quicktools.actions

import android.content.Context
import com.corphish.quicktools.actions.model.ActionType
import com.corphish.quicktools.actions.model.ResultMode
import com.corphish.quicktools.actions.model.TextAction
import org.json.JSONArray
import java.io.File
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fonte de verdade das ações: arquivo JSON em filesDir.
 *
 * Persistência leve e zero-dependência (org.json, embarcado no Android) para
 * maximizar a robustez do build neste ambiente. A interface é a mesma de um
 * repository Room — trocar o backend depois é invisível para handlers/UI.
 */
@Singleton
class ActionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val file: File by lazy { File(context.filesDir, FILE_NAME) }

    private val _actions = MutableStateFlow<List<TextAction>>(emptyList())
    val actions: StateFlow<List<TextAction>> = _actions.asStateFlow()

    private val prefs by lazy {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    init {
        load()
    }

    /** Carrega do disco (e semeia defaults no primeiro launch). */
    @Synchronized
    fun load() {
        val list = if (file.exists()) {
            runCatching {
                val arr = JSONArray(file.readText())
                buildList {
                    for (i in 0 until arr.length()) add(TextAction.fromJson(arr.getJSONObject(i)))
                }
            }.getOrDefault(emptyList())
        } else emptyList()

        _actions.value = if (list.isEmpty() && !prefs.getBoolean(KEY_SEEDED, false)) {
            val seeded = ActionSeeder.defaults()
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
            persist(seeded)
            seeded
        } else {
            list
        }
    }

    fun enabled(): List<TextAction> =
        _actions.value.filter { it.enabled }.sortedWith(compareBy({ it.order }, { it.name }))

    fun all(): List<TextAction> = _actions.value

    fun byId(id: Long): TextAction? = _actions.value.firstOrNull { it.id == id }

    @Synchronized
    fun add(action: TextAction): TextAction {
        val withId = action.copy(id = nextId(), createdAt = now(), updatedAt = now())
        val updated = _actions.value + withId
        persist(updated)
        return withId
    }

    @Synchronized
    fun update(action: TextAction): TextAction {
        val now = now()
        val updated = _actions.value.map { if (it.id == action.id) action.copy(updatedAt = now) else it }
        persist(updated)
        return action.copy(updatedAt = now)
    }

    @Synchronized
    fun delete(id: Long) {
        persist(_actions.value.filterNot { it.id == id })
    }

    @Synchronized
    fun setEnabled(id: Long, enabled: Boolean) {
        val now = now()
        persist(_actions.value.map {
            if (it.id == id) it.copy(enabled = enabled, updatedAt = now) else it
        })
    }

    @Synchronized
    fun setOrder(id: Long, order: Int) {
        val now = now()
        persist(_actions.value.map {
            if (it.id == id) it.copy(order = order, updatedAt = now) else it
        })
    }

    @Synchronized
    fun reorder(orderedIds: List<Long>) {
        val byId = _actions.value.associateBy { it.id }
        val now = now()
        val reordered = orderedIds.mapIndexedNotNull { idx, id ->
            byId[id]?.copy(order = idx, updatedAt = now)
        }
        val untouched = _actions.value.filter { it.id !in orderedIds }
        persist(reordered + untouched)
    }

    private fun nextId(): Long = (_actions.value.maxOfOrNull { it.id } ?: 0) + 1
    private fun now(): Long = System.currentTimeMillis()

    @Synchronized
    private fun persist(list: List<TextAction>) {
        val arr = JSONArray()
        list.sortedWith(compareBy({ it.order }, { it.name })).forEach { arr.put(it.toJson()) }
        file.parentFile?.mkdirs()
        file.writeText(arr.toString())
        _actions.value = list
    }

    companion object {
        private const val FILE_NAME = "textactions.json"
        private const val PREFS = "texttools_x"
        private const val KEY_SEEDED = "seeded_v1"
    }
}

/** Ações padrão plantadas no primeiro launch (editáveis/removíveis pelo usuário). */
object ActionSeeder {
    fun defaults(): List<TextAction> = listOf(
        TextAction.draft("UPPERCASE", ActionType.TRANSFORM, """{"op":"UPPERCASE"}""")
            .copy(id = 1, order = 0, iconKey = "ic_text_transform"),
        TextAction.draft("lowercase", ActionType.TRANSFORM, """{"op":"LOWERCASE"}""")
            .copy(id = 2, order = 1, iconKey = "ic_text_transform"),
        TextAction.draft("Title Case", ActionType.TRANSFORM, """{"op":"TITLE_CASE"}""")
            .copy(id = 3, order = 2, iconKey = "ic_text_transform"),
        TextAction.draft("Sort Lines", ActionType.TRANSFORM, """{"op":"SORT_LINES"}""")
            .copy(id = 4, order = 3, iconKey = "ic_text_transform"),
        TextAction.draft("Remove Empty Lines", ActionType.TRANSFORM, """{"op":"REMOVE_EMPTY_LINES"}""")
            .copy(id = 5, order = 4, iconKey = "ic_text_transform"),
        TextAction.draft("Trim Whitespace", ActionType.TRANSFORM, """{"op":"TRIM"}""")
            .copy(id = 6, order = 5, iconKey = "ic_text_transform"),
        TextAction.draft("Wrap in Quotes", ActionType.TRANSFORM, """{"op":"WRAP_DOUBLE"}""")
            .copy(id = 7, order = 6, iconKey = "ic_text_transform"),
        TextAction.draft("Bold Sans", ActionType.TRANSFORM, """{"op":"BOLD_SANS"}""")
            .copy(id = 8, order = 7, iconKey = "ic_text_transform"),
        TextAction.draft("Extract Emails", ActionType.EXTRACT,
            """{"pattern":"EMAIL","join":"\n","unique":true}""",
            resultMode = ResultMode.COPY,
        ).copy(id = 9, order = 8, iconKey = "ic_text_count"),
        TextAction.draft("Extract URLs", ActionType.EXTRACT,
            """{"pattern":"URL","join":"\n","unique":true}""",
            resultMode = ResultMode.COPY,
        ).copy(id = 10, order = 9, iconKey = "ic_text_count"),
        TextAction.draft("Word & Char Count", ActionType.ANALYZE,
            """{"metrics":["chars","words","lines","letters"]}""",
            resultMode = ResultMode.DISPLAY,
        ).copy(id = 11, order = 10, iconKey = "ic_text_count"),
        TextAction.draft("Google It", ActionType.TEMPLATE,
            """{"template":"https://google.com/search?q={{text}}","encode":true}""",
            resultMode = ResultMode.SHARE,
        ).copy(id = 12, order = 11, iconKey = "ic_edit_note"),
    )
}
