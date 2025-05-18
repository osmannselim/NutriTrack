package com.learningroots.nutriTrackApp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.learningroots.nutriTrackApp.navigation.Screen // Assuming Screen.AdminView.route will be created

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianLoginScreen(navController: NavController) {
    var clinicianKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val correctKey = "dollar-entry-apples"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Clinician Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = clinicianKey,
            onValueChange = {
                clinicianKey = it
                errorMessage = null // Clear error when user types
            },
            label = { Text("Clinician Key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (clinicianKey == correctKey) {
                    navController.navigate(Screen.AdminView.route) {
                        popUpTo(Screen.ClinicianLogin.route) { inclusive = true }
                    }
                } else {
                    errorMessage = "Invalid Clinician Key. Please try again."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Clinician Login")
        }
    }
} 