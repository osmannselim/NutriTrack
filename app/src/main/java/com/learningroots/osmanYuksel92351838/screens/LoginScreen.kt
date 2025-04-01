@file:OptIn(ExperimentalMaterial3Api::class)

package com.learningroots.osmanYuksel92351838.screens

import androidx.compose.ui.platform.LocalContext
import com.learningroots.osmanYuksel92351838.utils.loadUserDataFromCSV

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.learningroots.osmanYuksel92351838.navigation.Screen
import com.learningroots.osmanYuksel92351838.viewmodel.UserViewModel

@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel) {

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedUserId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

//    val userIds = listOf("123456", "234567", "345678")   // dummy

    val context = LocalContext.current
    val userDataList = remember { loadUserDataFromCSV(context) }
    val userIds = userDataList.map { it.userId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // User ID dropdown
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedUserId,
                onValueChange = {},
                readOnly = true,
                label = { Text("User ID") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                userIds.forEach { userId ->
                    DropdownMenuItem(
                        text = { Text(userId) },
                        onClick = {
                            selectedUserId = userId
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phone number input
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Phone
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Disclaimer text
        Text(
            text = "This app is only for pre-registered users. Please have your ID and phone number before continuing",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(

            onClick = {
                val matchedUser = userDataList.find { it.userId == selectedUserId && it.phoneNumber == phoneNumber }

                if (matchedUser != null) {
                    errorMessage = null
                    userViewModel.setUser(matchedUser)
                    navController.navigate(Screen.Questionnaire.route)

                } else {
                    errorMessage = "Invalid User ID or Phone Number"
                }
            }
            ,
            modifier = Modifier.padding(16.dp)
        )
        {
            Text("Continue")
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


