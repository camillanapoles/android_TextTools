package com.corphish.quicktools.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.corphish.quicktools.R
import com.corphish.quicktools.functions.UnitConverter
import com.corphish.quicktools.ui.common.CustomTopAppBar
import com.corphish.quicktools.ui.theme.QuickToolsTheme
import com.corphish.quicktools.ui.theme.TypographyV2
import com.corphish.quicktools.viewmodels.CurrencyConversionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyConversionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialText = if (savedInstanceState == null && intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
            intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString()
        } else {
            ""
        }
        setContent {
            QuickToolsTheme {
                CurrencyConversionScreen(initialText) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConversionScreen(
    initialText: String,
    viewModel: CurrencyConversionViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val inputValue by viewModel.inputValue.collectAsState()
    val fromUnit by viewModel.fromUnit.collectAsState()
    val toUnit by viewModel.toUnit.collectAsState()
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(initialText) {
        viewModel.init(initialText)
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                id = R.string.currency_conversion,
                onNavigationClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_conversion_desc),
                style = TypographyV2.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Input Value
            TextField(
                value = inputValue,
                onValueChange = { viewModel.onInputValueChange(it) },
                label = { Text(stringResource(R.string.value)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // From Unit
                Column(modifier = Modifier.weight(1f)) {
                    CurrencyDropdown(
                        label = stringResource(R.string.from),
                        options = UnitConverter.Unit.entries.filter { it.category == UnitConverter.Category.CURRENCY },
                        selectedOption = fromUnit,
                        onOptionSelected = { viewModel.onFromUnitChange(it) },
                        optionLabel = { it.label }
                    )
                }

                androidx.compose.material3.IconButton(
                    onClick = { viewModel.swapUnits() },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_redo),
                        contentDescription = stringResource(R.string.swap)
                    )
                }

                // To Unit
                Column(modifier = Modifier.weight(1f)) {
                    CurrencyDropdown(
                        label = stringResource(R.string.to),
                        options = UnitConverter.Unit.entries.filter { it.category == UnitConverter.Category.CURRENCY },
                        selectedOption = toUnit,
                        onOptionSelected = { viewModel.onToUnitChange(it) },
                        optionLabel = { it.label }
                    )
                }
            }

            // Result
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.result),
                        style = TypographyV2.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = result.ifEmpty { "0" },
                        style = TypographyV2.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = toUnit.label,
                        style = TypographyV2.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CurrencyDropdown(
    label: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = optionLabel(selectedOption),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
