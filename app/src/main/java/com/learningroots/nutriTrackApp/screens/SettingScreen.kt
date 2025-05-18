package com.learningroots.nutriTrackApp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MedicalServices // Placeholder for Clinician/Admin
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel
import com.learningroots.nutriTrackApp.navigation.Screen // Assuming Screen.Login.route exists

@Composable
fun SettingScreen(userViewModel: UserViewModel, navController: NavController) {
    val patient by userViewModel.patient.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Account Section
        Text(
            text = "ACCOUNT",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        patient?.let {
            UserInfoRow(icon = Icons.Filled.AccountCircle, text = it.userName ?: "N/A")
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            UserInfoRow(icon = Icons.Filled.Phone, text = it.phoneNumber ?: "N/A")
        } ?: run {
            UserInfoRow(icon = Icons.Filled.AccountCircle, text = "Loading...")
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            UserInfoRow(icon = Icons.Filled.Phone, text = "Loading...")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Other Settings Section
        Text(
            text = "OTHER SETTINGS",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SettingsButton(
            icon = Icons.Filled.ExitToApp,
            text = "Logout",
            onClick = {
                userViewModel.logout() // Call the new logout function
                // Navigation will be handled by MainActivity's observation of patient state
                // No explicit navigation here is needed if MainActivity handles redirection to Welcome/Login
                // However, to be safe and ensure immediate UI update for the settings screen itself or if it's part of a NavGraph that doesn't auto-clear,
                // navigating to a known 'logged out' start destination is still a good practice.
                // The NavHost in MainActivity will pick the correct start (Welcome) after logout.
                navController.navigate(Screen.Welcome.route) { // Navigate to Welcome, which will then decide if Login is needed
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        SettingsButton(
            icon = Icons.Filled.MedicalServices, // Using MedicalServices as a placeholder
            text = "Clinician Login",
            onClick = {
                navController.navigate(Screen.ClinicianLogin.route)
            }
        )
    }
}

@Composable
fun UserInfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SettingsButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}