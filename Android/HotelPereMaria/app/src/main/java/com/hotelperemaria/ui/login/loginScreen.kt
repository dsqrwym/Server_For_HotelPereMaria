package com.hotelperemaria.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.hotelperemaria.R.color

@Composable
fun loginScreen(viewModel: loginViewModel, navRegistro: () -> Unit, navPrincipal:  () -> Unit){
    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(id = color.fondo))
    ) {
        Login(
            Modifier.align(Alignment.Center),
            viewModel,
            navRegistro,
            navPrincipal
        )
    }



}





@Composable
private  fun Login(
    modifier: Modifier,
    viewModel: loginViewModel,
    navRegistro: () -> Unit,
    navPrincipal: () -> Unit
) {

    val email : String by viewModel.email.observeAsState("")
    val pas : String by viewModel.pas.observeAsState("")

    val loginEnb:Boolean by viewModel.loginEnb.observeAsState(false)
    val loading :Boolean by viewModel.isLoading.observeAsState(false)

    val coroutineScope = rememberCoroutineScope()
    val registro = navRegistro
    val correcto:Boolean by viewModel.correcto.observeAsState(false)

    LaunchedEffect(correcto) {
        if (correcto){
            navPrincipal()
        }
    }


    if (loading){
        Box(Modifier.fillMaxSize()){
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }else{
        Column(modifier = modifier.padding(horizontal = 16.dp)) {
            HeaderImage()
            Spacer(modifier = Modifier.padding(16.dp))
            EmailField(email) { viewModel.onLoginChanged(it, pas) }
            Spacer(modifier = Modifier.padding(16.dp))
            PaswordField(pas) { viewModel.onLoginChanged(email, it) }
            Spacer(modifier = Modifier.padding(8.dp))
            resgistrarse(Modifier.align(Alignment.End),registro)
            Spacer(modifier = Modifier.padding(16.dp))
            LogBotn(loginEnb){
                coroutineScope.launch {
                    viewModel.onLoginSelected()
                }
            }

        }
    }
}


@Composable
private fun LogBotn(loginEnb: Boolean, onLoginSelected: () -> Unit) {
    Button(onClick = {onLoginSelected()}, modifier = Modifier
        .fillMaxWidth()
        .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = color.btnPrin),
            disabledContainerColor = colorResource(id= color.borde),
            contentColor = colorResource(id = color.negro),
            disabledContentColor = colorResource(id = color.blanco)
        ), enabled = loginEnb
    ) {
        Text(text = "Iniciar sesion")
    }
}

@Composable
private fun resgistrarse(align: Modifier, registro: () -> Unit) {
    Text(
        text = "¿Quieres registarte?",
        modifier = align.clickable {registro()},
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFF9933)

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaswordField(pas: String, onTextFieldChanged: (String) -> Unit) {
    OutlinedTextField(
        value = pas,
        onValueChange = {onTextFieldChanged(it)},
        modifier = Modifier
            .fillMaxWidth(),
        placeholder = {
            Text(text = "Contraseña")
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        maxLines = 1,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = colorResource(color.textbox_focused),  // Color de fondo del TextField
            focusedIndicatorColor = colorResource(color.negro), // Color del borde cuando está enfocado
            unfocusedIndicatorColor = Color.Gray // Color del borde cuando no está enfocado
        )

    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailField(email: String, onTextFieldChanged:(String) ->Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = {onTextFieldChanged(it)},
        modifier = Modifier
            .fillMaxWidth()
            ,
        placeholder = {
            Text(text = "Email/Username")
        }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        maxLines = 1,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = colorResource(color.textbox_focused),  // Color de fondo del TextField
            focusedIndicatorColor = colorResource(color.negro), // Color del borde cuando está enfocado
            unfocusedIndicatorColor = Color.Gray // Color del borde cuando no está enfocado
        )

    )
}

@Composable
private fun HeaderImage(){
}


