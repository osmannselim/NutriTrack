@file:OptIn(ExperimentalMaterial3Api::class)

package com.learningroots.nutriTrackApp.screens

import com.learningroots.nutriTrackApp.utils.loadPatientsFromCSV
import com.learningroots.nutriTrackApp.navigation.Screen
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.learningroots.nutriTrackApp.viewmodel.RegistrationError

@Composable
fun RegisterScreen(navController: NavController, userViewModel: UserViewModel) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedUserId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val patientList = remember { loadPatientsFromCSV(context) }
    val userIds = patientList.map { it.userId }

    // Validation functions
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.all { it.isDigit() } && phone.length >= 10
    }

    fun validateInputs(): Boolean {
        if (selectedUserId.isBlank() || userName.isBlank() || phoneNumber.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Please fill in all fields"
            return false
        }
        if (!isValidPhoneNumber(phoneNumber)) {
            errorMessage = "Please enter a valid phone number (numbers only, minimum 10 digits)"
            return false
        }
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return false
        }
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Register",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // User ID Dropdown
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = selectedUserId,
                onValueChange = {},
                readOnly = true,
                label = { Text("My ID (Provided by your Clinician)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
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


        TextField(
            value = userName,
            onValueChange = { input ->
                // Only allow letters
                if (input.all { it.isLetter() } || input.isEmpty()) {
                    userName = input
                }
            },
            label = { Text("Name(letters only)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Phone Number Field
        TextField(
            value = phoneNumber,
            onValueChange = { input ->
                // Only allow digits
                if (input.all { it.isDigit() } || input.isEmpty()) {
                    phoneNumber = input
                }
            },
            label = { Text("Phone Number (digits only)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            isError = phoneNumber.isNotEmpty() && !isValidPhoneNumber(phoneNumber)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password again") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPassword.isNotEmpty() && password != confirmPassword
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "This app is only for pre-registered users. Please enter your phone number and password to claim your account.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Register Button
        Button(
            onClick = {
                if (!validateInputs()) {
                    println("burda patladi..")
                    return@Button
                }

                userViewModel.register(selectedUserId, userName, phoneNumber, password) { success, error ->
                    if (success) {
                        // After successful registration, attempt to log in
                        userViewModel.login(selectedUserId, phoneNumber, password) { loginSuccess, loginError, user ->
                            if (loginSuccess && user != null) {
                                userViewModel.setPatient(user)
                                navController.navigate(Screen.Questionnaire.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            } else {
                                errorMessage = "Registration successful but login failed. Please try logging in."
                                navController.navigate(Screen.Login.route)
                            }
                        }
                    } else {
                        errorMessage = when (error) {
                            RegistrationError.INVALID_USER -> "Invalid user ID. Please check your ID."
                            RegistrationError.PHONE_MISMATCH -> "Phone number doesn't match our records."
                            RegistrationError.ALREADY_REGISTERED -> "This account has already been registered. Please login instead."
                            RegistrationError.NONE -> "Registration failed. Please try again."
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Register")
        }

        // Login Button
        Button(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Login")
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