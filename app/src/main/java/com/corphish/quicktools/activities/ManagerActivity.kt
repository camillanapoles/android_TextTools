package com.corphish.quicktools.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.corphish.quicktools.actions.ActionIcons
import com.corphish.quicktools.actions.model.TextAction
import com.corphish.quicktools.ui.theme.QuickToolsTheme
import com.corphish.quicktools.viewmodels.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Gestão absoluta das ações: ativar/desativar, editar, excluir, criar.
 * Tudo refletido no launcher SEM rebuild — só persistência.
 */
@AndroidEntryPoint
class ManagerActivity : ComponentActivity() {

    private val viewModel: ManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickToolsTheme {
                val actions by viewModel.actions.collectAsState()
                val sorted = actions.sortedWith(compareBy({ it.order }, { it.name }))

                ManagerScreen(
                    actions = sorted,
                    onToggle = { id, enabled -> viewModel.setEnabled(id, enabled) },
                    onEdit = { action -> openEditor(action.id) },
                    onDelete = { id -> viewModel.delete(id) },
                    onAdd = { openEditor(null) },
                )
            }
        }
    }

    private fun openEditor(id: Long?) {
        val intent = Intent(this, ActionEditorActivity::class.java)
        if (id != null) intent.putExtra(ActionEditorActivity.EXTRA_ACTION_ID, id)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagerScreen(
    actions: List<TextAction>,
    onToggle: (Long, Boolean) -> Unit,
    onEdit: (TextAction) -> Unit,
    onDelete: (Long) -> Unit,
    onAdd: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Actions Manager") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad)) {
            items(actions, key = { it.id }) { action ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(ActionIcons.resolve(action.iconKey)),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onEdit(action) }
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(text = action.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${action.type.name} • ${action.resultMode.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    IconButton(onClick = { onEdit(action) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(action.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    Switch(
                        checked = action.enabled,
                        onCheckedChange = { onToggle(action.id, it) },
                    )
                }
            }
        }
    }
}
