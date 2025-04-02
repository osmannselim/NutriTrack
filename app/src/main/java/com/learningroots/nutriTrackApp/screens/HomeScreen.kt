package com.learningroots.nutriTrackApp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import com.learningroots.nutriTrackApp.navigation.Screen
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import com.learningroots.nutriTrackApp.R
@Composable
fun HomeScreen(userViewModel: UserViewModel) {
    val user by userViewModel.user.collectAsState()
    val navController = rememberNavController()

    user?.let {
        val score = it.totalScore

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text("Hello,", style = MaterialTheme.typography.bodyLarge)
            Text(it.userId, style = MaterialTheme.typography.headlineMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "You've already filled in your Food Intake Questionnaire,",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "but you can change details here:",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { navController.navigate(Screen.Questionnaire.route) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text("Edit", maxLines = 1)
                }
            }


            Image(
                painter = painterResource(id = R.drawable.veggies_no_background),
                contentDescription = "Healthy food plate",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 16.dp)
            )

            Text("My Score", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Food Quality score", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${score.toInt()}/100",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4CAF50) // Green
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("What is the Food Quality Score?", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Your Food Quality Score provides a snapshot of how well your eating patterns align with established food guidelines, helping you identify both strengths and opportunities for improvement in your diet.\n\nThis personalized measurement considers various food groups including vegetables, fruits, whole grains, and proteins to give you practical insights for making healthier food choices.",
                style = MaterialTheme.typography.bodySmall
            )

        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No user data available.")
        }
    }
}
