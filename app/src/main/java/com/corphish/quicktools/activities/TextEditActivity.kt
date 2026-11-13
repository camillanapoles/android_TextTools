package com.corphish.quicktools.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.corphish.quicktools.R
import com.corphish.quicktools.data.Constants
import com.corphish.quicktools.ui.common.CustomTopAppBar
import com.corphish.quicktools.ui.theme.QuickToolsTheme
import com.corphish.quicktools.viewmodels.TextEditViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TextEditActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
        val readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        val forceCopy = intent.getBooleanExtra(Constants.INTENT_FORCE_COPY, false)

        setContent {
            QuickToolsTheme {
                TextEditScreen(
                    initialText = text.toString(),
                    canBeApplied = !readonly && !forceCopy,
                    onBack = { finish() },
                    onApply = {
                        val resultIntent = Intent()
                        resultIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, it)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onCopy = {
                        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, it)
                        }
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditScreen(
    initialText: String,
    canBeApplied: Boolean,
    onBack: () -> Unit,
    onApply: (String) -> Unit,
    onCopy: () -> Unit,
    onShare: (String) -> Unit,
    viewModel: TextEditViewModel = hiltViewModel()
) {
    val text by viewModel.text.collectAsState()

    LaunchedEffect(initialText) {
        viewModel.setText(initialText)
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                id = R.string.edit_and_copy_title,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                OutlinedButton(
                    onClick = { onShare(text) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(stringResource(R.string.share))
                }

                Button(
                    onClick = {
                        viewModel.copyToClipboard()
                        onCopy()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.copy))
                }

                if (canBeApplied) {
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = { onApply(text) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.apply))
                    }
                }
            }

            TextField(
                value = text,
                onValueChange = { viewModel.setText(it) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.input)) }
            )
        }
    }
}
