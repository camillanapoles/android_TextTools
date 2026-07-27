package com.corphish.quicktools.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corphish.quicktools.actions.ActionStore
import com.corphish.quicktools.actions.handler.ActionContext
import com.corphish.quicktools.actions.handler.ActionHandler
import com.corphish.quicktools.actions.handler.ActionResult
import com.corphish.quicktools.actions.handler.HandlerRegistry
import com.corphish.quicktools.actions.model.ActionType
import com.corphish.quicktools.actions.model.TextAction
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.json.JSONObject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val store: ActionStore,
    private val registry: HandlerRegistry,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val enabledActions: StateFlow<List<TextAction>> = store.actions
        .map { list -> list.filter { it.enabled }.sortedWith(compareBy({ it.order }, { it.name })) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun isImplemented(type: ActionType): Boolean = registry.isImplemented(type)

    suspend fun execute(action: TextAction, input: String, readOnly: Boolean): ActionResult {
        val handler: ActionHandler = registry.resolve(action.type)
            ?: return ActionResult.Error("Handler '${action.type.name}' indisponível nesta build")
        val config = runCatching { JSONObject(action.configJson) }.getOrElse { JSONObject() }
        return runCatching { handler.execute(ActionContext(input, readOnly, context), config) }
            .getOrElse { ActionResult.Error("Falha: ${it.message}") }
    }
}
