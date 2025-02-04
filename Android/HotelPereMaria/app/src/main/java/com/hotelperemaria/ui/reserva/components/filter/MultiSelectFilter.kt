package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectFilter(
    modifier: Modifier = Modifier,
    title: String,
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    val localState by remember(selectedOptions) {
        mutableStateOf(selectedOptions.toList())
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        LaunchedEffect(localState) {
            onSelectionChanged(localState)
        }
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = selectedOptions.takeIf { it.isNotEmpty() }?.joinToString() ?: title,
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
                // 复用搜索框组件
                SearchField(title, searchText) { searchText = it }

                // 复用清除按钮组件
                if (selectedOptions.isNotEmpty()) {
                    ClearSelectionButton(
                        onClear = {
                            onSelectionChanged(emptyList())
                            searchText = ""
                            expanded = false
                        }
                    )
                }

                // 复用选项项组件（带多选逻辑适配）
                val filteredOptions by remember(searchText, options) {
                    derivedStateOf {
                        options.filter { it.contains(searchText, ignoreCase = true) }
                    }
                }

                filteredOptions.forEach { option ->
                    SelectionItem(
                        option = option,
                        isSelected = selectedOptions.contains(option),
                        onSelect = {
                            val newList = selectedOptions.toMutableList().apply {
                                if (contains(option)) remove(option) else add(option)
                            }
                            onSelectionChanged(newList)
                            searchText = ""
                        }
                    )
                }
            }
        }
    }
}