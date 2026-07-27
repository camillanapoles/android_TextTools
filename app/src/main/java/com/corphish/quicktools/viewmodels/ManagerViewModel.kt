package com.corphish.quicktools.viewmodels

import androidx.lifecycle.ViewModel
import com.corphish.quicktools.actions.ActionStore
import com.corphish.quicktools.actions.model.TextAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val store: ActionStore,
) : ViewModel() {

    val actions: StateFlow<List<TextAction>> = store.actions

    fun allSorted(): List<TextAction> =
        store.all().sortedWith(compareBy({ it.order }, { it.name }))

    fun setEnabled(id: Long, enabled: Boolean) = store.setEnabled(id, enabled)
    fun delete(id: Long) = store.delete(id)
    fun add(action: TextAction): TextAction = store.add(action)
    fun update(action: TextAction): TextAction = store.update(action)
}
