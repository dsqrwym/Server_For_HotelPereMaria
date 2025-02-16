package com.hotelperemaria.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hotelperemaria.ui.login.loginScreen
import com.hotelperemaria.ui.login.loginViewModel
import com.hotelperemaria.ui.principal.PrincipalScreen
import com.hotelperemaria.ui.registro.registerScreen
import com.hotelperemaria.ui.reserva.ReservationListScreen
import com.hotelperemaria.ui.reserva.ReservationModifyScreen
import com.hotelperemaria.ui.reserva.ReservationScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val loginViewModel: loginViewModel = hiltViewModel()
    val isLoggedIn by loginViewModel.sessionState.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(Login) {
                popUpTo(0) // 清空返回栈，防止回到已退出的页面
            }
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavHost(navController = navController, startDestination = Login) {
            composable<Login> {
                loginScreen(
                    hiltViewModel(),
                    { navController.navigate(Registro) },
                    { navController.navigate(ReservationList) })
            }
            composable<Registro> {
                registerScreen(viewModel()) { navController.navigate(Login) }
            }
            composable<Principal> {
                PrincipalScreen{ navController.navigate(Reservation) }
            }
            composable<ReservationList> {
                ReservationListScreen(onBack = {}){navController.navigate(ReservationModify)}
            }
            composable<Reservation> {
                ReservationScreen{ navController.navigate(Principal) }
            }
            composable<ReservationModify> {
                ReservationModifyScreen{ navController.navigate(ReservationList) }
            }
        }
    }
}

