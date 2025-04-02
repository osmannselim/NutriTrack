package com.learningroots.nutriTrackApp.navigation

/**
 * Author: Osman Yuksel
 */
sealed class Screen(val route: String) {
    object Home         : Screen("home_screen")
    object Insight      : Screen("insight_screen")
    object NutriCoach   : Screen("nutricoach_screen")
    object Setting      : Screen("setting_screen")
    object Login        : Screen("login")
    object Welcome      : Screen("welcome")
    object Questionnaire: Screen("questionnaire")
}