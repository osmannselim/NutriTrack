package com.learningroots.nutriTrackApp.navigation

/**
 * Author: Osman Yuksel
 */
sealed class Screen(val route: String) {
    object Home           : Screen("home_screen")
    object Insights       : Screen("insight_screen")
    object NutriCoach     : Screen("nutricoach_screen")
    object Settings       : Screen("setting_screen")
    object Login          : Screen("login")
    object Welcome        : Screen("welcome")
    object Questionnaire  : Screen("questionnaire")
    object Register       : Screen("register")
    object ClinicianLogin : Screen("clinician_login")
    object AdminView      : Screen("admin_view")
}