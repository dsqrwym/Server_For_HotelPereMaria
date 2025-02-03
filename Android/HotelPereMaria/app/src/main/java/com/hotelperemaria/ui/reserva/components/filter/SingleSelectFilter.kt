package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleSelectFilter(
    modifier: Modifier = Modifier,
    title: String,
    options: List<String>,
    selectedOption: String?,
    onSelectionChanged: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val filteredOptions = remember(searchText, options) {
        options.filter { it.contains(searchText, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer),
            readOnly = true,
            value = selectedOption ?: title,
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SearchField(title, searchText) { searchText = it }

                if (selectedOption != null) {
                    ClearSelectionButton(
                        onClear = {
                            onSelectionChanged(null)
                            searchText = ""
                            expanded = false
                        }
                    )
                }

                filteredOptions.forEach { option ->
                    SelectionItem(
                        option = option,
                        isSelected = selectedOption == option,
                        onSelect = {
                            onSelectionChanged(if (selectedOption == option) null else option)
                            expanded = false
                            searchText = ""
                        }
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSingleSelectFilter() {
    SingleSelectFilter(
        title = "Tipo de habitación",
        options = listOf("Individual", "Doble", "Suite"),
        selectedOption = "Doble",
        onSelectionChanged = {}
    )
}