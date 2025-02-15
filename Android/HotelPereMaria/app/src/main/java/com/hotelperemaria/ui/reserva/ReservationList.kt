package com.hotelperemaria.ui.reserva

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotelperemaria.R
import com.hotelperemaria.data.model.ReservaForList
import com.hotelperemaria.ui.reserva.components.ReservaCard
import com.hotelperemaria.ui.reserva.components.TopBar
import com.hotelperemaria.ui.reserva.components.filter.FilterBar
import com.hotelperemaria.utils.LoadingScreen

@Composable
fun ReservationListScreen(viewModel: ReservationListViewModel = hiltViewModel()) {
    var expandedFilter by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(colorResource(R.color.fondo))
            .pointerInput(Unit) {
                detectTapGestures {
                    if (expandedFilter) expandedFilter = false
                }
            }
    ) {
        TopBar(stringResource(R.string.top_bar_reservation_list)) {
            println("Back")
        }
        Box {
            val originSouce by viewModel.originalReservas.collectAsState()
            val results by viewModel.filteredReservas.collectAsState()
            if (results.isEmpty()) {
                EmptyScreen(if (originSouce.isEmpty()) stringResource(R.string.reservation_no_result) else stringResource(R.string.reservation_no_filter_result))
            }else {
                ReservationCardList(
                    results,
                    onCancel = { viewModel.cancelReserva(it) },
                    onEdit = { viewModel.editReserva(it) }
                ) {
                    if (expandedFilter) expandedFilter = false
                }
            }
            FilterBar(
                modifier = Modifier
                    .pointerInput(Unit) { detectTapGestures { /*Para consumir el toque*/ } },
                viewModel,
                expanded = expandedFilter,
                toggleExpandState = { expandedFilter = !expandedFilter }
            )
        }
    }

    LoadingScreen(isLoading = viewModel.isLoading)
}

@Composable
fun EmptyScreen(s: String, onScroll: () -> Unit = {}) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 130.dp)
            .background(colorResource(R.color.fondo))
            .nestedScroll(object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    onScroll()
                    return super.onPreScroll(available, source)
                }
            }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(s, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Medium, color = colorResource(R.color.textbox_unfocused))
    }
}


@Preview
@Composable
fun EmptyScreenPreview() {
    EmptyScreen("no_results")
}

@Composable
fun ReservationCardList(
    results: List<ReservaForList>,
    onCancel: (ReservaForList) -> Unit = {},
    onEdit: (ReservaForList) -> Unit = {},
    onScroll: () -> Unit = {}
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 130.dp)
            .background(colorResource(R.color.fondo))
            .nestedScroll(object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    onScroll()
                    return super.onPreScroll(available, source)
                }
            })
    ) {
        itemsIndexed(results, key = { _, reserva -> reserva.id }) { index, reserva ->
            if (index == 0) {
                Spacer(modifier = Modifier.height(30.dp))
            }
            ReservaCard(
                reservaForList = reserva,
                onEdit = { onEdit(it) },
                onCancel = { onCancel(it) }
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
    val viewModel: ReservationListViewModel = viewModel()
    ReservationListScreen(viewModel)
}