package com.learningroots.osmanYuksel92351838.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.learningroots.osmanYuksel92351838.viewmodel.UserViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

/**
 * Author: Osman Yuksel
 * Created on: 30-03-2025 12.27 pm
 */

@Composable
fun InsightsScreen(userViewModel: UserViewModel) {
    val user = userViewModel.user.collectAsState().value

    user?.let {
        val isMale = it.sex.lowercase() == "male"

        // Select gender-specific scores
        val foodScores = listOf(
            "Vegetables" to it.vegetables,
            "Fruits" to it.fruits,
            "Grains & Cereals" to it.grains,
            "Whole Grains" to it.wholeGrains,
            "Meat & Alternatives" to it.meat,
            "Dairy" to it.dairy,
            "Water" to it.water,
            "Saturated Fats" to it.saturatedFat,
            "Unsaturated Fats" to it.unsaturatedFat,
            "Sodium" to it.sodium,
            "Sugar" to it.sugar,
            "Alcohol" to it.alcohol,
            "Discretionary Foods" to it.discretionary
        )

        val totalScore = it.totalScore

//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        )
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

//            foodScores.forEach { (label, value) ->
//                val maxScore = if (label in listOf("Water", "Alcohol")) 5f else 10f
//                val progress = ((value / maxScore).coerceIn(0.0, 1.0)).toFloat()
//
//
//                Column {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(label)
//                        Text("${value.toInt()}/${maxScore.toInt()}")
//                    }
//
//                    LinearProgressIndicator(
//                        progress = { progress },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(8.dp),
////                        color = Color(0xFF9C27B0) // Purple
//                        color = Color(0xFF0000FF) // Purple
//
//                    )
//                }
//            }

            foodScores.forEach { (label, value) ->
                val maxScore = if (label in listOf("Water", "Alcohol")) 5f else 10f
                val progress = ((value / maxScore).coerceIn(0.0, 1.0)).toFloat()

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
                        progress = { progress.toFloat() },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp),
                        color = Color(0xFF9C27B0)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("${value.toInt()}/${maxScore.toInt()}")
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

            // Buttons (not functional yet)
            Button(
                onClick = { /* Not implemented yet */ },
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
