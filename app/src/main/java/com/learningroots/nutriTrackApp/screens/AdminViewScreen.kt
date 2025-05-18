package com.learningroots.nutriTrackApp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminViewScreen(navController: NavController, userViewModel: UserViewModel) {
    val analyticsState by userViewModel.adminAnalytics.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { // Load scores when the screen is first launched
        userViewModel.loadAverageHeifaScores()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinician Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Average HEIFA Scores",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ScoreDisplayRow(
                label = "Average HEIFA (Male):",
                score = analyticsState.averageHeifaMale?.format(1) ?: "--"
            )
            Spacer(modifier = Modifier.height(8.dp))
            ScoreDisplayRow(
                label = "Average HEIFA (Female):",
                score = analyticsState.averageHeifaFemale?.format(1) ?: "--"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { userViewModel.generateAdminInsights() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Find Data Pattern", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (analyticsState.isLoading && analyticsState.genAiInsights == null) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 20.dp))
            }

            analyticsState.error?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            analyticsState.genAiInsights?.let {
                if (it.isNotEmpty()){
                    Text(
                        "AI-Powered Data Analysis:",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).align(Alignment.Start)
                    )
                    it.forEach { insight ->
                        InsightCard(insightText = insight)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                } else if (!analyticsState.isLoading) {
                     Text("No insights generated yet. Click 'Find Data Pattern'.", modifier = Modifier.padding(vertical = 10.dp))
                }
            }
            
            Spacer(modifier = Modifier.weight(1f)) // Pushes Done button to bottom if content is short

            Button(
                onClick = { navController.popBackStack() }, // Or navigate to a specific screen like Settings
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Done", fontSize = 16.sp)
            }
             Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ScoreDisplayRow(label: String, score: String) {
    OutlinedTextField(
        value = score,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End, fontSize = 18.sp, fontWeight = FontWeight.Bold),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun InsightCard(insightText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = insightText,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 15.sp
        )
    }
}

// Helper extension function to format Double to a specific number of decimal places
// Ensure this is accessible or defined here/in a common utils file
 fun Double.format(digits: Int) = "%.${digits}f".format(this)
// This is already in AdminViewScreen.kt, but if moved to ViewModel, ensure it's accessible
// Or define it in a common place. For now, assuming it's fine if ViewModel uses it too.
