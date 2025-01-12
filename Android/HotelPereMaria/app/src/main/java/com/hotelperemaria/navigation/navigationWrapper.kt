package com.hotelperemaria.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hotelperemaria.ui.login.loginScreen
import com.hotelperemaria.ui.registro.registerScreen
import com.hotelperemaria.ui.principal.principalScreen
@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(navController=navController, startDestination = Login ){
        composable<Login> {
            loginScreen(viewModel(), {navController.navigate(Registro)},{navController.navigate(Principal)})
        }
        composable<Registro> {
            registerScreen(viewModel()) { navController.navigate(Login) }
        }
        composable<Principal> {
            principalScreen()
        }


    }
}

