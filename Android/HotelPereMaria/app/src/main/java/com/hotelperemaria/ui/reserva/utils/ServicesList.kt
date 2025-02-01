package com.hotelperemaria.ui.reserva.utils

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.ui.reserva.ReservaViewModel
import java.math.BigDecimal


@Composable
fun ServicesList(reservaViewModel: ReservaViewModel) {
    val sortedServices =
        reservaViewModel.extraServices.sortedBy { !reservaViewModel.selectedServices.contains(it) }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(elevation = 10.dp, shape = MaterialTheme.shapes.medium)
            .background(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Precio por Noche: ${reservaViewModel.nightlyPrice}€",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        // 额外服务列表（可滑动）
        if (sortedServices.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 18.dp)
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // 限制最大高度
            ) {
                itemsIndexed(sortedServices) { index, service ->
                    val isChecked = reservaViewModel.selectedServices.contains(service)
                    ServiceItem(
                        extraService = service,
                        isChecked = isChecked,
                        onCheckedChange = {
                            reservaViewModel.toggleServiceSelection(service, it)
                        }
                    )
                    if (index < sortedServices.lastIndex) {
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
        // 最后一行：总价格
        Text(
            text = "Precio total: ${reservaViewModel.totalPrice}€",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }

}

@Composable
fun ServiceItem(
    extraService: ExtraService,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {

    //动态阴影动画
    val elevation by animateDpAsState(
        targetValue = if (isChecked) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = EaseIn),
        label = "changeShadowElevation"
    )

    val cicleElevation by animateDpAsState(
        targetValue = if (isChecked) 10.dp else 2.dp,
        animationSpec = tween(durationMillis = 300, easing = EaseIn),
        label = "changeShadowElevation"
    )

    // 自定义点击效果
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        Modifier
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(30.dp)
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),// 自定义涟漪效果
            ) {
                onCheckedChange(!isChecked)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .fillMaxWidth(0.1f)
                .shadow(elevation = cicleElevation, shape = CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
                .border(1.dp, Color.Black, CircleShape)
                .padding(3.dp)
        ) {
            Crossfade(
                targetState = isChecked,
                animationSpec = tween(durationMillis = 300, easing = EaseOutCubic),
                label = "changeSelectedState"
            ) { checked ->
                Icon(
                    if (checked) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.Center),
                    tint = if (checked) Color.Black else Color.Red
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight()
                .shadow(elevation, RoundedCornerShape(topEnd = 15.dp, bottomEnd = 15.dp))
                .background(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    RoundedCornerShape(topEnd = 15.dp, bottomEnd = 15.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // 绘制额外服务的名称和价格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = extraService.nombre,
                    color = Color.Black,
                    fontSize = 10.sp
                )
                Text(
                    text = "${extraService.precio}€",
                    color = Color.Black,
                    fontSize = 10.sp
                )
            }

        }
    }
}


@Preview
@Composable
fun ServiceListPreview() {
    val reservaViewModel: ReservaViewModel = hiltViewModel()
    ServicesList(reservaViewModel)
}


@Preview
@Composable
fun ServiceItemPreview() {
    Box(Modifier.background(Color.White)) {
        var state by remember { mutableStateOf(false) }
        ServiceItem(extraService = ExtraService("Test", BigDecimal.valueOf(10)), state) {
            state = it
        }
    }
}