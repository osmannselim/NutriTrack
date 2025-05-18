package com.learningroots.nutriTrackApp.screens

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.learningroots.nutriTrackApp.navigation.Screen
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel
import java.util.*
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.runtime.LaunchedEffect
import com.learningroots.nutriTrackApp.R
import com.learningroots.nutriTrackApp.data.mapper.FoodIntakeMapper
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(navController: NavController, userViewModel: UserViewModel) {

    val user by userViewModel.patient.collectAsState()

    if (user == null) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
        return
    }

    val context = LocalContext.current

    val personaImageMap = mapOf(
        "Health Devotee"        to R.drawable.persona_1,
        "Mindful Eater"         to R.drawable.persona_2,
        "Wellness Striver"      to R.drawable.persona_3,
        "Balance Seeker"        to R.drawable.persona_4,
        "Health Procrastinator" to R.drawable.persona_5,
        "Food Carefree"         to R.drawable.persona_6
    )

    val personaList = listOf(
        "Health Devotee",
        "Mindful Eater",
        "Wellness Striver",
        "Balance Seeker",
        "Health Procrastinator",
        "Food Carefree"
    )

    val personaDescriptionMap = mapOf(
        "Health Devotee"        to "I'm passionate about healthy eating & health plays a big part in my life. I use social media to follow active lifestyle personalities or get new recipes/exercise ideas. I may even buy superfoods or follow a particular type of diet. I like to think I am super healthy.",
        "Mindful Eater"         to "I'm health-conscious and being healthy and eating healthy is important to me. Although health means different things to different people, I make conscious lifestyle decisions about eating based on what I believe healthy means. I look for new recipes and healthy eating information on social media.",
        "Wellness Striver"      to "I aspire to be healthy (but struggle sometimes). Healthy eating is hard work! I've tried to improve my diet, but always find things that make it difficult to stick with the changes. Sometimes I notice recipe ideas or healthy eating hacks, and if it seems easy enough, I'll give it a go.",
        "Balance Seeker"        to "I try and live a balanced lifestyle, and I think that all foods are okay in moderation. I shouldn't have to feel guilty about eating a piece of cake now and again. I get all sorts of inspiration from social media like finding out about new restaurants, fun recipes and sometimes healthy eating tips.",
        "Health Procrastinator" to "I'm contemplating healthy eating but it's not a priority for me right now. I know the basics about what it means to be healthy, but it doesn't seem relevant to me right now. I have taken a few steps to be healthier but I am not motivated to make it a high priority because I have too many other things going on in my life.",
        "Food Carefree"         to "I'm not bothered about healthy eating. I don't really see the point and I don't think about it. I don't really notice healthy eating tips or recipes and I don't care what I eat."
    )

    val foodRows = listOf(
        listOf("Fruits", "Vegetable", "Grains"),
        listOf("Red Meat", "Seafood", "Poultry"),
        listOf("Fish", "Eggs", "Nuts/Seeds")
    )
    val selectedOptions = remember { mutableStateMapOf<String, Boolean>() }

    var selectedPersona by remember { mutableStateOf("") }
    var showModal by remember { mutableStateOf(false) }
    var modalPersona by remember { mutableStateOf("") }

    // Dropdown
    var expanded by remember { mutableStateOf(false) }

    // Time pickers
    var biggestMealTime by remember { mutableStateOf("") }
    var sleepTime by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val personaButtons = listOf(
        listOf("Health Devotee", "Mindful Eater", "Wellness Striver"),
        listOf("Balance Seeker", "Health Procrastinator", "Food Carefree")
    )

    LaunchedEffect(Unit) {
        val currentUser = userViewModel.patient.value
        currentUser?.let { u ->
            val savedIntake = userViewModel.getFoodIntakeForUser(u.userId)
            savedIntake?.let { entity ->
                val uiModel = FoodIntakeMapper.fromEntity(entity)
                selectedPersona = uiModel.persona
                biggestMealTime = uiModel.biggestMealTime
                sleepTime = uiModel.sleepTime
                wakeTime = uiModel.wakeTime
                uiModel.selectedFoods.forEach { food ->
                    selectedOptions[food] = true
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _: TimePicker, hour: Int, minute: Int ->
                onTimeSelected(String.format("%02d:%02d", hour, minute) )
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    // Helper function to parse time string "HH:mm" to a Pair(Hour, Minute)
    fun parseTime(timeString: String): Pair<Int, Int>? {
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                if (hour in 0..23 && minute in 0..59) { // Basic validation for hour/minute ranges
                    hour to minute
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("Food Intake Questionnaire", style = MaterialTheme.typography.titleLarge)
        }


        Text(
            "Tick all the food categories you can eat:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            mainAxisSpacing = 1.dp,
            crossAxisSpacing = 1.dp
        ) {
            foodRows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { food ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = selectedOptions[food] ?: false,
                                onCheckedChange = { selectedOptions[food] = it }
                            )
                            Text(food)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Your Persona", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "People can be broadly classified into 6 different types based on their eating preferences. Click on each button below to find out the different types, and select the type that best fits you!",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        personaButtons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { persona ->
                    Button(
                        onClick = { modalPersona = persona; showModal = true },
                        modifier = Modifier.width(100.dp),
                        content = { Text(persona, fontSize = 12.sp, maxLines = 2) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Persona dropdown (centered)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                TextField(
                    value = selectedPersona,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Choose Persona") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    // Optionally constrain width so it doesn't stretch full screen:
                    modifier = Modifier
                        .menuAnchor()
                        .width(240.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    personaList.forEach { persona ->
                        DropdownMenuItem(
                            text = { Text(text = persona) },
                            onClick = {
                                selectedPersona = persona
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time Pickers

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "What time of day approx. do you normally eat your biggest meal?",
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showTimePicker { biggestMealTime = it } }) {
                Text(biggestMealTime.ifEmpty { "--:--" })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "What time of day approx. do you go to sleep at night?",
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showTimePicker { sleepTime = it } }) {
                Text(sleepTime.ifEmpty { "--:--" })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "What time of day approx. do you wake up in the morning?",
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showTimePicker { wakeTime = it } }) {
                Text(wakeTime.ifEmpty { "--:--" })
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        val canSave = selectedOptions.values.any { it } &&
                selectedPersona.isNotBlank() &&
                biggestMealTime.isNotBlank() &&
                sleepTime.isNotBlank() &&
                wakeTime.isNotBlank()


        // "Save & Continue" button (centered)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                errorMessage = null

                val parsedWakeTime = parseTime(wakeTime)
                val parsedSleepTime = parseTime(sleepTime)
                val parsedBiggestMealTime = parseTime(biggestMealTime)

                if (parsedWakeTime == null || parsedSleepTime == null || parsedBiggestMealTime == null) {
                    errorMessage = "Please select valid times for wake, sleep, and biggest meal."
                    return@Button
                }

                val (wakeHour, wakeMinute) = parsedWakeTime
                val (sleepHour, sleepMinute) = parsedSleepTime
                val (mealHour, mealMinute) = parsedBiggestMealTime

                // Rule 1: Times cannot be identical
                if ((wakeHour == sleepHour && wakeMinute == sleepMinute) ||
                    (wakeHour == mealHour && wakeMinute == mealMinute) ||
                    (sleepHour == mealHour && sleepMinute == mealMinute)) {
                    errorMessage = "Wake, sleep, and biggest meal times must be distinct."
                    return@Button
                }

                // Convert to minutes from midnight for easier comparison
                val wakeTotalMinutes = wakeHour * 60 + wakeMinute
                val originalSleepTotalMinutes = sleepHour * 60 + sleepMinute // Store original for meal check if sleep crosses midnight
                val mealTotalMinutes = mealHour * 60 + mealMinute
                
                // Rule 2: Wake before Sleep (handled by checking duration)
                // If sleep time is on the next day (e.g. wake 7:00, sleep 1:00), adjust sleepTotalMinutes for duration calculation
                val adjustedSleepTotalMinutesForDuration = 
                    if (originalSleepTotalMinutes >= wakeTotalMinutes) originalSleepTotalMinutes 
                    else originalSleepTotalMinutes + 24 * 60
                
                val durationMinutes = adjustedSleepTotalMinutesForDuration - wakeTotalMinutes
                
                if (durationMinutes <= 0) { 
                    errorMessage = "Sleep time must be after wake-up time, forming a positive duration. Ensure times are logical (e.g., Wake 7:00, Sleep 23:00 or Wake 22:00, Sleep 6:00 next day)."
                    return@Button
                }

                // Rule 3: Biggest Meal between Wake and Sleep
                val isMealWithinPeriod: Boolean
                if (originalSleepTotalMinutes >= wakeTotalMinutes) { // Sleep on the same day (e.g. Wake 07:00, Sleep 23:00)
                    isMealWithinPeriod = mealTotalMinutes >= wakeTotalMinutes && mealTotalMinutes <= originalSleepTotalMinutes
                } else { // Sleep on the next day (e.g. Wake 22:00, Sleep 06:00)
                    // Meal can be from wake time till midnight OR from midnight till sleep time
                    isMealWithinPeriod = mealTotalMinutes >= wakeTotalMinutes || mealTotalMinutes <= originalSleepTotalMinutes
                }

                if (!isMealWithinPeriod) {
                    errorMessage = "Biggest meal time must be between wake-up and sleep times."
                    return@Button
                }

                val currentUser = userViewModel.patient.value
                if (currentUser != null) {
                    val intake = FoodIntakeMapper.toEntity(
                        patientId = currentUser.userId,
                        persona = selectedPersona,
                        biggestMealTime = biggestMealTime, // Store original string
                        sleepTime = sleepTime,           // Store original string
                        wakeTime = wakeTime,             // Store original string
                        selectedFoods = selectedOptions.filterValues { it }.keys
                    )
                    userViewModel.saveFoodIntake(intake)

                    // Save SharedPreferences flag
                    val prefs = context.getSharedPreferences("QuestionnairePrefs_${currentUser.userId}", Context.MODE_PRIVATE)
                    with(prefs.edit()) {
                        putBoolean("hasQuestionnaireSaved", true)
                        apply()
                    }

                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Questionnaire.route) { inclusive = true } // removes Questionnaire from back stack
                    }
                } else {
                    errorMessage = "User not found. Please log in again." // Should ideally not happen if screen is guarded by user check
                }
            },
                enabled = canSave   // disable the button if user did not fill in the questionnaire
            ) {
                Text("Save & Continue")
            }
        }

        // Persona Modal Popup
        if (showModal) {
            AlertDialog(
                onDismissRequest = { showModal = false },
                confirmButton = {
                    TextButton(onClick = { showModal = false }) {
                        Text("Close")
                    }
                },
                title = { Text(modalPersona) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val imageId = personaImageMap[modalPersona] ?: R.drawable.x
                        Image(
                            painter = painterResource(id = imageId),
                            contentDescription = modalPersona,
                            modifier = Modifier.size(160.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(personaDescriptionMap[modalPersona] ?: "No description available.")

                    }
                }
            )
        }
    }
}