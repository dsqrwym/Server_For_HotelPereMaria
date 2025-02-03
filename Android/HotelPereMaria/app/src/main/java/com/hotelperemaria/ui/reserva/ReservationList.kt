package com.hotelperemaria.ui.reserva

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotelperemaria.R
import com.hotelperemaria.ui.reserva.components.ReservaCard
import com.hotelperemaria.ui.reserva.components.TopBar
import com.hotelperemaria.ui.reserva.components.filter.FilterBar
import com.hotelperemaria.utils.LoadingScreen

@Composable
fun ReservationListScreen(viewModel: FilterReservaViewModel = hiltViewModel()) {
    Column(
        Modifier
            .fillMaxSize()
            .shadow(elevation = 10.dp, shape = MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(stringResource(R.string.top_bar_reservation_list)) {
            println("Back")
        }
        Box {
            ReservationCardList(viewModel)
            FilterBar(modifier = Modifier.padding(1.dp), viewModel)
        }
    }

    LoadingScreen(isLoading = viewModel.isLoading)
}

@Composable
fun ReservationCardList(viewModel: FilterReservaViewModel) {
    val results by viewModel.filteredReservas.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 130.dp)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        itemsIndexed(results) { index, reserva ->
            if (index == 0) {
                Spacer(modifier = Modifier.height(30.dp))
            }
            ReservaCard(
                reserva = reserva,
                onEdit = { },
                onCancel = { }
            )
            if (index < results.lastIndex) {
                HorizontalDivider(
                    Modifier.padding(horizontal = 18.dp),
                    color = Color.LightGray,
                    thickness = 1.dp
                )
            } else {
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }


}

@Preview
@Composable
fun ReservationListScreenPreview() {
    val viewModel: FilterReservaViewModel = viewModel()
    ReservationListScreen(viewModel)
}