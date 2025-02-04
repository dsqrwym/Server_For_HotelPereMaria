package com.hotelperemaria.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(isLoading: Boolean) {
    if (isLoading) {
        var dotCount by remember { mutableIntStateOf(0) }

        // 让点的数量在 0-3 之间循环
        LaunchedEffect(Unit) {
            while (true) {
                dotCount = (dotCount + 1) % 4
                delay(500) // 每 500ms 变化一次
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)), // 半透明背景
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.padding(10.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(stringResource(R.string.loading_state_loading) + ".".repeat(dotCount), color = MaterialTheme.colorScheme.primary)
        }
    }
}
