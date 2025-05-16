package com.learningroots.nutriTrackApp.screens

import com.learningroots.nutriTrackApp.viewmodel.UserViewModel
import com.learningroots.nutriTrackApp.data.entity.NutriCoachTip
import com.learningroots.nutriTrackApp.data.model.FruitData
import com.learningroots.nutriTrackApp.viewmodel.GeminiUiState
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.rememberCoroutineScope
import coil3.request.crossfade

// Threshold for determining optimal fruit score. TODO: Replace with actual value from scoring guide.
// const val FRUIT_SCORE_OPTIMAL_THRESHOLD = 7.5 // Will be replaced by ViewModel logic

/**
 * Author: Osman Yuksel
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutriCoachScreen(userViewModel: UserViewModel) {

    // val APIkey = "AIzaSyCN6rpOVsO6BG-HcZsbs_fK_6Kb-ajaZ4g" // API Key is handled in ViewModel via BuildConfig

    val context = LocalContext.current
    val user by userViewModel.patient.collectAsState()
    val fruitDetails by userViewModel.fruitDetails.collectAsState()
    // val motivationalMessage by userViewModel.motivationalMessage.collectAsState() // Will use GeminiUiState.Success
    val allTips by userViewModel.allNutriCoachTips.collectAsState()
    val geminiState by userViewModel.geminiUiState.collectAsState()

    var fruitNameInput by remember { mutableStateOf("") }
    var showAllTipsDialog by remember { mutableStateOf(false) }
    // Control dialog visibility using ViewModel state
    val showTipsDialogVM by userViewModel.showAllTipsModal.collectAsState()


    val scope = rememberCoroutineScope()

    // Load all tips when the screen is first composed or user changes
    LaunchedEffect(user) {
        user?.userId?.let {
            userViewModel.loadTips(it)
        }
        // Clear previous fruit details and message when user changes.
        // Consider if this is the desired UX or if messages should persist until explicitly cleared or new ones are generated.
        // userViewModel.clearFruitDetails()
        // userViewModel.clearMotivationalMessage() // This also resets GeminiUiState to Initial
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
            if (!userViewModel.isFruitScoreOptimal(currentUser)) { // Use ViewModel function
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
                    // Show a message if search was attempted but no details found, only if a search has been made
                    // This condition can be improved by having a specific state for fruit search attempts.
                    if (fruitNameInput.isNotBlank() && geminiState !is GeminiUiState.Loading) { // Avoid showing this if AI is loading
                        // Text("Fruit details not found. Try another name.", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                // Optimal fruit score: Show random image
                Text("Your fruit intake is looking good!", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        // Seeded for consistency per user, can use a different seed if preferred
                        .data("https://picsum.photos/seed/${currentUser.userId.hashCode()}/800/400") 
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
                    userViewModel.clearMotivationalMessage() // Clear previous message and set GeminiUiState to Initial
                    scope.launch {
                         val foodIntake = userViewModel.getFoodIntakeForUser(currentUser.userId)
                         userViewModel.generateMotivationalMessage(currentUser, foodIntake)
                    }
                },
                enabled = geminiState !is GeminiUiState.Loading, // Disable button when loading
                modifier = Modifier.fillMaxWidth()
            ) {
                if (geminiState is GeminiUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Text("Get Motivational Message (AI)")
                }
            }

            // Display Gemini API results
            when (val state = geminiState) {
                is GeminiUiState.Success -> {
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(state.outputText, modifier = Modifier.padding(16.dp), textAlign = TextAlign.Start)
                    }
                }
                is GeminiUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Error: ${state.errorMessage}",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                is GeminiUiState.Initial -> {
                    // Optionally show a placeholder text or nothing
                    // Text("Click the button above to get a motivational tip!", modifier = Modifier.padding(vertical = 8.dp))
                }
                is GeminiUiState.Loading -> {
                    // Already handled by the button's content change
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { userViewModel.toggleAllTipsModal(true) }, // Use ViewModel to toggle
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show All Saved Tips (${allTips.size})")
            }

        } ?: run {
            // Show a more centered loading state for user data
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading user data...")
                }
            }
        }
    }

    // Observe ViewModel state for dialog
    if (showTipsDialogVM) {
        AllTipsDialog(tips = allTips, onDismiss = { userViewModel.toggleAllTipsModal(false) })
    }
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
                LazyColumn(modifier = Modifier.fillMaxHeight(0.7f)) { // Ensure dialog is not overly tall
                    items(tips.size) { index ->
                        val tip = tips[index]
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(tip.message, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Saved: ${dateFormatter.format(Date(tip.timestamp))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < tips.size - 1) {
                            Divider(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
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