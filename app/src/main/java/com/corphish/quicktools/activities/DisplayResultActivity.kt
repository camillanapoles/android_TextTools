package com.corphish.quicktools.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.corphish.quicktools.R
import com.corphish.quicktools.actions.ActionIcons
import com.corphish.quicktools.actions.model.TextAction
import com.corphish.quicktools.ui.theme.QuickToolsTheme

/**
 * Mostra o resultado de uma ação [ResultMode.DISPLAY] (ex.: análise de texto).
 * Recebe título + texto pré-formatado via extras.
 */
class DisplayResultActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent?.getStringExtra(EXTRA_DISPLAY_TITLE) ?: "Resultado"
        val text = intent?.getStringExtra(EXTRA_DISPLAY_TEXT).orEmpty()
        setContent {
            QuickToolsTheme {
                Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { pad ->
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
                        item {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_DISPLAY_TITLE = "display_title"
        const val EXTRA_DISPLAY_TEXT = "display_text"
    }
}
