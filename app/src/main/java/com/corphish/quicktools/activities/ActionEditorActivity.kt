package com.corphish.quicktools.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.corphish.quicktools.actions.ActionIcons
import com.corphish.quicktools.actions.model.ActionType
import com.corphish.quicktools.actions.model.ResultMode
import com.corphish.quicktools.actions.model.TextAction
import com.corphish.quicktools.ui.theme.QuickToolsTheme
import com.corphish.quicktools.viewmodels.ManagerViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Cria/edita uma ação. A config é JSON livre (power-user); o tipo define o schema
 * (mostrado como hint). Esta é a porta de "incrementar sem rebuild".
 */
@AndroidEntryPoint
class ActionEditorActivity : ComponentActivity() {

    private val viewModel: ManagerViewModel by viewModels()
    private var editingId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingId = intent.getLongExtra(EXTRA_ACTION_ID, -1L)
        val existing: TextAction? = if (editingId > 0) viewModel.allSorted().firstOrNull { it.id == editingId } else null

        setContent {
            QuickToolsTheme {
                EditorScreen(
                    initial = existing,
                    onSave = { action ->
                        if (existing != null) viewModel.update(action.copy(id = editingId))
                        else viewModel.add(action)
                        finish()
                    },
                    onDelete = {
                        if (existing != null) viewModel.delete(editingId)
                        finish()
                    },
                    onCancel = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_ACTION_ID = "action_id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorScreen(
    initial: TextAction?,
    onSave: (TextAction) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: ActionType.TRANSFORM) }
    var resultMode by remember { mutableStateOf(initial?.resultMode ?: ResultMode.REPLACE) }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: ActionIcons.keys.first()) }
    var configJson by remember { mutableStateOf(initial?.configJson ?: defaultConfig(type)) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (initial == null) "New Action" else "Edit Action") }) },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            PickerRow(
                label = "Type",
                value = type.name,
                options = ActionType.values().map { it.name },
                onSelect = {
                    type = ActionType.valueOf(it)
                    configJson = defaultConfig(type)
                },
            )

            PickerRow(
                label = "Result",
                value = resultMode.name,
                options = ResultMode.values().map { it.name },
                onSelect = { resultMode = ResultMode.valueOf(it) },
            )

            PickerRow(
                label = "Icon",
                value = iconKey,
                options = ActionIcons.keys,
                onSelect = { iconKey = it },
            )

            OutlinedTextField(
                value = configJson,
                onValueChange = { configJson = it },
                label = { Text("Config (JSON)") },
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    onSave(
                        TextAction.draft(name, type, configJson, resultMode)
                            .copy(iconKey = iconKey)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }

            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }

            if (initial != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PickerRow(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge)
        TextButton(onClick = { open = true }) { Text(value) }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(label) },
            text = {
                Column {
                    options.forEach { opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            RadioButton(selected = opt == value, onClick = { onSelect(opt); open = false })
                            Text(opt)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Close") } },
        )
    }
}

/** Config padrão (template) por tipo — ponto de partida editável. */
private fun defaultConfig(type: ActionType): String = when (type) {
    ActionType.TRANSFORM -> """{"op":"UPPERCASE"}"""
    ActionType.EXTRACT -> """{"pattern":"EMAIL","join":"\n","unique":true}"""
    ActionType.FIND_REPLACE -> """{"find":"","replace":"","regex":false,"ignoreCase":false,"all":true}"""
    ActionType.TEMPLATE -> """{"template":"{{text}}","encode":false}"""
    ActionType.ANALYZE -> """{"metrics":["chars","words","lines"]}"""
    ActionType.TRANSLATE -> """{"from":"en","to":"pt"}"""
    ActionType.SCRIPT -> """{"code":"return s.toUpperCase()","capabilities":[]}"""
    ActionType.AI_PROMPT -> """{"prompt":"{{text}}","model":""}"""
    ActionType.PIPELINE -> """{"steps":[]}"""
}
