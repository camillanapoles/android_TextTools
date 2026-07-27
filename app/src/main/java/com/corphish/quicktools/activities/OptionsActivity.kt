package com.corphish.quicktools.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.corphish.quicktools.actions.ActionIcons
import com.corphish.quicktools.actions.ActionStore
import com.corphish.quicktools.actions.handler.ActionContext
import com.corphish.quicktools.actions.handler.ActionResult
import com.corphish.quicktools.actions.handler.HandlerRegistry
import com.corphish.quicktools.actions.model.ActionType
import com.corphish.quicktools.actions.model.ResultMode
import com.corphish.quicktools.actions.model.TextAction
import com.corphish.quicktools.data.Constants
import com.corphish.quicktools.ui.theme.QuickToolsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Única entrada do menu de contexto (alias habilitado em modo SINGLE).
 *
 * Renderiza o launcher dinâmico ELE MESMO (padrão do TextTools original:
 * translucent + setContent), lendo as ações do [ActionStore] em runtime.
 * Ao escolher uma ação, executa o handler e devolve o resultado ao sistema
 * para substituição in-place. Sem delegação for-result pra outra activity
 * (que era frágil com activity translucent + BAL).
 */
@AndroidEntryPoint
class OptionsActivity : NoUIActivity() {

    @Inject lateinit var store: ActionStore
    @Inject lateinit var registry: HandlerRegistry

    private var input: String = ""
    private var readOnly: Boolean = false
    private var forceCopy: Boolean = false

    override fun handleIntent(intent: Intent): Boolean {
        if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
            input = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
            readOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
            forceCopy = intent.getBooleanExtra(Constants.INTENT_FORCE_COPY, false)

            setContent {
                QuickToolsTheme {
                    val actions = store.enabled()
                    var loading by remember { mutableStateOf(false) }
                    LauncherScreen(
                        actions = actions,
                        inputPreview = input,
                        loading = loading,
                        isImplemented = { type -> registry.isImplemented(type) },
                        onPick = { action ->
                            if (loading) return@LauncherScreen
                            loading = true
                            runAction(action)
                        },
                        onCancel = { finish() },
                    )
                }
            }
            return false // mantém vivo; a UI finaliza após a ação
        }
        return true
    }

    private fun runAction(action: TextAction) {
        lifecycleScope.launch {
            val handler = registry.resolve(action.type)
            val result = if (handler == null) {
                ActionResult.Error("Handler '${action.type.name}' indisponível nesta build")
            } else {
                val config = runCatching { JSONObject(action.configJson) }.getOrElse { JSONObject() }
                runCatching {
                    handler.execute(ActionContext(input, readOnly || forceCopy, this@OptionsActivity), config)
                }.getOrElse { ActionResult.Error("Falha: ${it.message}") }
            }
            applyResult(action, result)
        }
    }

    private fun applyResult(action: TextAction, result: ActionResult) {
        when (result) {
            is ActionResult.Text -> when (action.resultMode) {
                ResultMode.REPLACE -> if (!readOnly && !forceCopy) returnText(result.value)
                else copy(result.value, action.name)
                ResultMode.COPY -> copy(result.value, action.name)
                ResultMode.SHARE -> share(result.value)
                ResultMode.DISPLAY -> showDisplay(action.name, result.value)
            }
            is ActionResult.Display -> showDisplay(result.title, result.sections.joinToString("\n") { "${it.first}: ${it.second}" })
            is ActionResult.Share -> share(result.value)
            is ActionResult.Error -> toast(result.message)
        }
        finish()
    }

    private fun returnText(text: String) {
        // Devolve nos DOIS extras: EXTRA_PROCESS_TEXT (como o app original, funcional)
        // e EXTRA_PROCESS_TEXT_RESULT (constante documentada). Cobertura máxima p/ replace.
        val data = Intent()
            .putExtra(Intent.EXTRA_PROCESS_TEXT, text)
            .putExtra("android.intent.extra.PROCESS_TEXT_RESULT", text)
        setResult(RESULT_OK, data)
    }

    private fun copy(text: String, label: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        toast("Copiado")
    }

    private fun share(text: String) {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                null,
            )
        )
    }

    private fun showDisplay(title: String, text: String) {
        startActivity(
            Intent(this, DisplayResultActivity::class.java).apply {
                putExtra(DisplayResultActivity.EXTRA_DISPLAY_TITLE, title)
                putExtra(DisplayResultActivity.EXTRA_DISPLAY_TEXT, text)
            }
        )
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LauncherScreen(
    actions: List<TextAction>,
    inputPreview: String,
    loading: Boolean,
    isImplemented: (ActionType) -> Boolean,
    onPick: (TextAction) -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Quick Actions") }) }) { pad ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().padding(pad),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp).align(Alignment.Center))
            }
            LazyColumn {
                if (inputPreview.isNotEmpty()) {
                    item {
                        Text(
                            text = inputPreview,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        HorizontalDivider()
                    }
                }
                items(actions, key = { it.id }) { action ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { onPick(action) }.padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Icon(
                            painter = painterResource(ActionIcons.resolve(action.iconKey)),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isImplemented(action.type)) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(text = action.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (isImplemented(action.type)) action.type.name.lowercase()
                                else "${action.type.name.lowercase()} (indisponível)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
