package com.learningroots.nutriTrackApp.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext

/**
 * Author: Osman Yuksel
 * Created on: 30-03-2025 12.27 pm
 */

@Composable
fun InsightsScreen(userViewModel: UserViewModel) {
    val user = userViewModel.user.collectAsState().value

    user?.let {

        // Select gender-specific scores
        val foodScores = listOf(
            "Discretionary Foods" to it.discretionary,
            "Vegetables" to it.vegetables,
            "Fruits" to it.fruits,
            "Grains & Cereals" to it.grains,
            "Whole Grains" to it.wholeGrains,
            "Meat & Alternatives" to it.meat,
            "Dairy" to it.dairy,
            "Sodium" to it.sodium,
            "Alcohol" to it.alcohol,
            "Water" to it.water,
            "Sugar" to it.sugar,
            "Saturated Fats" to it.saturatedFat,
            "Unsaturated Fats" to it.unsaturatedFat
        )

        val totalScore = it.totalScore
        val context = LocalContext.current

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            Text("Insights: Food Score", style = MaterialTheme.typography.titleLarge)

            foodScores.forEach { (label, value) ->
                val progress = ((value / 10f).coerceIn(0.0, 1.0)).toFloat()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.width(130.dp)
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = Color(0xFF9C27B0)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(String.format("%.2f/10", value))
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Total Score Bar
            Text("Total Food Quality Score", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LinearProgressIndicator(
                    progress = { ((totalScore / 100).coerceIn(0.0, 1.0)).toFloat() },
                            modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = Color(0xFF9C27B0)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${totalScore.toInt()}/100")
            }

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "My Food Quality Score is $totalScore out of 100! 🥦🍎 How's yours?"
                        )
                    }
                    context.startActivity(
                        Intent.createChooser(shareIntent, "Share your score via")
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share with someone")
            }

            Button(
                onClick = { /* Not implemented yet */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Improve my diet!")
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No user data available.")
        }
    }
}
