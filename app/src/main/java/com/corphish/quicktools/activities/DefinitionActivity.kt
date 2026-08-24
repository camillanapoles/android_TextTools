package com.corphish.quicktools.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.corphish.quicktools.R
import com.corphish.quicktools.repository.DefinitionSource
import com.corphish.quicktools.repository.WordDefinition
import com.corphish.quicktools.ui.common.CustomTopAppBar
import com.corphish.quicktools.ui.theme.BrandFontFamily
import com.corphish.quicktools.ui.theme.QuickToolsTheme
import com.corphish.quicktools.ui.theme.TypographyV2
import com.corphish.quicktools.viewmodels.DefinitionState
import com.corphish.quicktools.viewmodels.DefinitionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DefinitionActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""

        setContent {
            QuickToolsTheme {
                DefinitionScreen(
                    initialText = text.toString(),
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinitionScreen(
    initialText: String,
    onBack: () -> Unit,
    viewModel: DefinitionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var searchText by remember { mutableStateOf(initialText) }

    LaunchedEffect(initialText) {
        if (initialText.isNotEmpty()) {
            viewModel.getDefinition(initialText)
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                id = R.string.definition_lookup_title,
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
            TextField(
                value = searchText,
                onValueChange = { 
                    searchText = it
                    viewModel.getDefinition(it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.word)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (state) {
                is DefinitionState.Idle -> {}
                is DefinitionState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DefinitionState.Success -> {
                    DefinitionList((state as DefinitionState.Success).results)
                }
                is DefinitionState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_definition_found, (state as DefinitionState.Error).message),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        NetLingoButton(searchText)
                    }
                }
            }
        }
    }
}

@Composable
fun NetLingoButton(word: String) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    androidx.compose.material3.Button(
        onClick = { 
            uriHandler.openUri("https://www.netlingo.com/search.php?term=${java.net.URLEncoder.encode(word, "UTF-8")}")
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.material3.Icon(
            androidx.compose.ui.res.painterResource(R.drawable.ic_open_in_new),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.search_on_netlingo))
    }
}

@Composable
fun DefinitionList(results: List<WordDefinition>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results) { wordDef ->
            WordDefinitionItem(wordDef)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (results.isNotEmpty()) {
            item {
                NetLingoButton(results[0].word)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun WordDefinitionItem(wordDef: WordDefinition) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = wordDef.word,
                style = TypographyV2.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = BrandFontFamily
            )
            
            Text(
                text = stringResource(
                    when (wordDef.source) {
                        DefinitionSource.DICTIONARY_API -> R.string.source_dictionary
                        DefinitionSource.WIKTIONARY -> R.string.source_wiktionary
                        DefinitionSource.LOCAL_SLANG -> R.string.source_local_slang
                    }
                ),
                style = TypographyV2.labelSmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            wordDef.phonetic?.let {
                Text(
                    text = it,
                    style = TypographyV2.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            wordDef.meanings.forEach { meaning ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = meaning.partOfSpeech.uppercase(),
                    style = TypographyV2.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                
                meaning.definitions.forEach { def ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "• ${def.definition}",
                            style = TypographyV2.bodyLarge
                        )
                        def.example?.let {
                            Text(
                                text = "\"$it\"",
                                style = TypographyV2.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
