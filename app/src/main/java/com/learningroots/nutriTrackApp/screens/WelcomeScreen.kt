package com.learningroots.nutriTrackApp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.learningroots.nutriTrackApp.navigation.Screen
import com.learningroots.nutriTrackApp.R
@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // Logo and app name
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo), // Replace with your actual logo
                contentDescription = "NutriTrack Logo",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("NutriTrack", style = MaterialTheme.typography.headlineMedium)
        }

        // Disclaimer text
        Text(
            text = "This app provides general health and nutrition information for educational purposes only. It is not intended as medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional before making any changes to your diet, exercise, or health regimen. Use this app at your own risk. \n" +
                    "If you’d like to an Accredited Practicing Dietitian (APD), please visit the Monash Nutrition/Dietetics Clinic (discounted rates for students):\n" +
                    "https://www.monash.edu/medicine/scs/nutrition/clinics/nutrition",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Osman Yuksel - 35921838",
            style = MaterialTheme.typography.bodyLarge
        )


        // Login button
        Button(
            onClick = {
                navController.navigate(Screen.Login.route)
                      },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}
