package com.learningroots.nutriTrackApp.screens

import com.learningroots.nutriTrackApp.viewmodel.UserViewModel
import com.learningroots.nutriTrackApp.data.entity.NutriCoachTip
import com.learningroots.nutriTrackApp.data.model.FruitData
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.rememberCoroutineScope
import coil3.request.crossfade

// Threshold for determining optimal fruit score. TODO: Replace with actual value from scoring guide.
const val FRUIT_SCORE_OPTIMAL_THRESHOLD = 2.0

/**
 * Author: Osman Yuksel
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriCoachScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current
    val user by userViewModel.patient.collectAsState()
    val fruitDetails by userViewModel.fruitDetails.collectAsState()
    val motivationalMessage by userViewModel.motivationalMessage.collectAsState()
    val allTips by userViewModel.allNutriCoachTips.collectAsState()

    var fruitNameInput by remember { mutableStateOf("") }
    var showAllTipsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load all tips when the screen is first composed or user changes
    LaunchedEffect(user) {
        user?.userId?.let {
            userViewModel.loadTips(it)
        }
        // Clear previous fruit details and message when user changes or screen recomposes without specific action
        // userViewModel.clearFruitDetails() // Decide if this is desired UX
        // userViewModel.clearMotivationalMessage() // Decide if this is desired UX
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("NutriCoach", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        user?.let { currentUser ->
            // Fruits Section (Conditional)
            if (currentUser.fruits < FRUIT_SCORE_OPTIMAL_THRESHOLD) {
                Text("Explore Fruits", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = fruitNameInput,
                    onValueChange = { fruitNameInput = it },
                    label = { Text("Enter fruit name (e.g., banana)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (fruitNameInput.isNotBlank()) {
                            userViewModel.getFruitDetails(fruitNameInput)
                        }
                    })
                )
                Button(
                    onClick = { 
                        if (fruitNameInput.isNotBlank()) {
                            userViewModel.getFruitDetails(fruitNameInput) 
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Details")
                }

                fruitDetails?.let { details ->
                    FruitDetailsCard(details)
                } ?: run {
                    if (fruitNameInput.isNotBlank() && userViewModel.fruitDetails.value == null) {
                        // Show a message if search was attempted but no details found
                         Text("Fruit details not found. Try another name.", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                // Optimal fruit score: Show random image
                Text("Your fruit intake is looking good!", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://picsum.photos/seed/${currentUser.userId}/400/200") // Seeded for consistency per user
                        .crossfade(true)
                        .build(),
                    contentDescription = "Random encouragement image",
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GenAI Section
            Text("Motivational Corner", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
            Button(
                onClick = {
                    // TODO: This should ideally pass more comprehensive patient data and food intake details
                    // For now, it uses what's available in UserViewModel.generateMotivationalMessage
                    scope.launch {
                         val foodIntake = userViewModel.getFoodIntakeForUser(currentUser.userId)
                         userViewModel.generateMotivationalMessage(currentUser, foodIntake)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Motivational Message (AI)")
            }

            motivationalMessage?.let {
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(it, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAllTipsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show All Tips (${allTips.size})")
            }

        } ?: run {
            Text("Loading user data... Please ensure you are logged in.")
        }
    }

    if (showAllTipsDialog) {
        AllTipsDialog(tips = allTips, onDismiss = { showAllTipsDialog = false })
    }
    // TODO: Implement actual API call for FruityVice
    // TODO: Implement actual API call for Google Gemini, passing relevant patient data
}

@Composable
fun FruitDetailsCard(fruit: FruitData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(fruit.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FruitDetailRow("Family", fruit.family)
            FruitDetailRow("Calories", "${fruit.calories} kcal")
            FruitDetailRow("Fat", "${fruit.fat} g")
            FruitDetailRow("Sugar", "${fruit.sugar} g")
            FruitDetailRow("Carbohydrates", "${fruit.carbohydrates} g")
            FruitDetailRow("Protein", "${fruit.protein} g")
            fruit.nutritions.forEach { (key, value) ->
                 FruitDetailRow(key, "$value") // Display other nutritions
            }
        }
    }
}

@Composable
fun FruitDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label:", style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun AllTipsDialog(tips: List<NutriCoachTip>, onDismiss: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All Saved Motivational Tips") },
        text = {
            if (tips.isEmpty()) {
                Text("No tips saved yet.")
            } else {
                LazyColumn {
                    items(tips.size) { index ->
                        val tip = tips[index]
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(tip.message, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Saved: ${dateFormatter.format(Date(tip.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (index < tips.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Add this to your imports if not already present:
// import kotlinx.coroutines.launch (already there in viewmodel, add here if needed for direct launch from composable)
// import androidx.compose.foundation.lazy.LazyColumn (added this)
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Info