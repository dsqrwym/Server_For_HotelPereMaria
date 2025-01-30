package com.hotelperemaria.ui.reserva.utils

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import com.hotelperemaria.ui.theme.HotelPereMariaTheme

@Composable
fun TopBar(title: String, onBack: (() -> Unit)? = null) {
    Box(
        Modifier
            .shadow(
                elevation = 16.dp
            )
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .fillMaxWidth()
    ) {
        Row(Modifier.height(48.dp)) {
            if (onBack != null) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    null,
                    Modifier
                        .clickable(onClick = onBack)
                        .align(Alignment.CenterVertically)
                        .size(36.dp)
                        .padding(8.dp),
                    MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.weight(1f))
        }
        Text(
            title,
            Modifier.align(Alignment.Center),
            MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
fun TopBarPreview() {
    TopBar("Reservacion") {
        println("Back")
    }
}