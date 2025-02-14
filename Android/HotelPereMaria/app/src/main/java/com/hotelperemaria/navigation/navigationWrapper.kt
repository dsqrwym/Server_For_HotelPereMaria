package com.hotelperemaria.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.hotelperemaria.ui.principal.PrincipalScreen
@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(navController=navController, startDestination = Principal ){

        composable<Principal> {
            PrincipalScreen()
        }


    }
}

