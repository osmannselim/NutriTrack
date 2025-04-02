package com.learningroots.nutriTrackApp.screens

import com.learningroots.nutriTrackApp.utils.SharedPrefs

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
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.edit
import com.learningroots.nutriTrackApp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(navController: NavController, userViewModel: UserViewModel) {

    val context = LocalContext.current

    val personaImageMap = mapOf(
        "Health Devotee" to R.drawable.persona_1,
        "Mindful Eater" to R.drawable.persona_2,
        "Wellness Striver" to R.drawable.persona_3,
        "Balance Seeker" to R.drawable.persona_4,
        "Health Procrastinator" to R.drawable.persona_5,
        "Food Carefree" to R.drawable.persona_6
    )

    val personaList = listOf(
        "Health Devotee",
        "Mindful Eater",
        "Wellness Striver",
        "Balance Seeker",
        "Health Procrastinator",
        "Food Carefree"
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

    val personaButtons = listOf(
        listOf("Health Devotee", "Mindful Eater", "Wellness Striver"),
        listOf("Balance Seeker", "Health Procrastinator", "Food Carefree")
    )

    val prefs = context.getSharedPreferences("QuestionnairePrefs", Context.MODE_PRIVATE)
    val hasSaved = prefs.getBoolean("hasQuestionnaireSaved", false)

    LaunchedEffect(Unit) {
        if (hasSaved) {
            selectedPersona = prefs.getString("persona", "") ?: ""
            biggestMealTime = prefs.getString("biggestMeal", "") ?: ""
            sleepTime = prefs.getString("sleep", "") ?: ""
            wakeTime = prefs.getString("wake", "") ?: ""

            val savedFoods = prefs.getStringSet("selectedFoods", emptySet()) ?: emptySet()
            savedFoods.forEach { food ->
                selectedOptions[food] = true
            }
        }
    }


    fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _: TimePicker, hour: Int, minute: Int ->
                onTimeSelected(String.format("%02d:%02d", hour, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
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
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back"
                )
            }
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
        // Time pickers as OutlinedTextFields (example)

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

        Spacer(modifier = Modifier.height(24.dp))

        // "Save & Continue" button (centered)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {

                prefs.edit() { putBoolean("hasQuestionnaireSaved", true) }

                SharedPrefs.saveQuestionnaireData(
                    context = context,
                    foodSelections = selectedOptions,
                    selectedPersona = selectedPersona,
                    biggestMealTime = biggestMealTime,
                    sleepTime = sleepTime,
                    wakeTime = wakeTime
                )
                navController.navigate(Screen.Home.route)
            }) {
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
                        Text("This persona typically eats like this: [Description for $modalPersona]")
                    }
                }
            )
        }
    }
}