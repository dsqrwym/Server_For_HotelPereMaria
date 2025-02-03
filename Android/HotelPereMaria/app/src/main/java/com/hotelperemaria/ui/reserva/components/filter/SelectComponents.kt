package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R

@Composable
fun SearchField(title: String, text: String, onTextChange: (String) -> Unit) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text("${stringResource(R.string.search)} $title") },
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .fillMaxWidth()
    )
}

@Composable
fun ClearSelectionButton(onClear: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.reservation_filter_clear_selection),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        },
        onClick = onClear,
        leadingIcon = {
            Icon(
                Icons.Default.Clear,
                contentDescription = stringResource(R.string.reservation_filter_clear_selection),
                tint = MaterialTheme.colorScheme.error
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SelectionItem(option: String, isSelected: Boolean, onSelect: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        onClick = onSelect,
        leadingIcon = {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.reservation_field_additional_services_checked),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    )
}
