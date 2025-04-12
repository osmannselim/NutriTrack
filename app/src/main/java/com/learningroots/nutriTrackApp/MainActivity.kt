package com.learningroots.nutriTrackApp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.learningroots.nutriTrackApp.navigation.BottomNavigationBar
import com.learningroots.nutriTrackApp.screens.NutriCoachScreen
import com.learningroots.nutriTrackApp.screens.HomeScreen
import com.learningroots.nutriTrackApp.screens.InsightsScreen
import com.learningroots.nutriTrackApp.navigation.Screen
import com.learningroots.nutriTrackApp.screens.LoginScreen
import com.learningroots.nutriTrackApp.screens.QuestionnaireScreen
import com.learningroots.nutriTrackApp.screens.SettingScreen
import com.learningroots.nutriTrackApp.screens.WelcomeScreen
import com.learningroots.nutriTrackApp.ui.theme.MyApplicationTheme
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
//import androidx.compose.runtime.remember
//import android.app.Activity
//import androidx.compose.ui.platform.LocalContext
//
//
//import androidx.navigation.NavController
//import androidx.navigation.NavDestination
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val userViewModel: UserViewModel = viewModel()
                val user by userViewModel.user.collectAsState()
                val context = LocalContext.current

                val startDestination = remember(user) {
                    if (user == null) {
                        Screen.Welcome.route // Or Screen.Login.route if that's your actual starting point
                    } else {
                        val prefs = context.getSharedPreferences("QuestionnairePrefs_${user!!.userId}", Context.MODE_PRIVATE)
                        val hasQuestionnaireSaved = prefs.getBoolean("hasQuestionnaireSaved", false)
                        if (hasQuestionnaireSaved) {
                            Screen.Home.route
                        } else {
                            Screen.Questionnaire.route
                        }
                    }
                }

                MainScreen(userViewModel = userViewModel, onFinishedActivity = { finish() }, startDestination = startDestination)
            }
        }
    }
}

@Composable
fun MainScreen(userViewModel: UserViewModel, onFinishedActivity: () -> Unit, startDestination: String) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (currentRoute in bottomBarScreens) {
        BackHandler(enabled = true) {
            if (currentRoute != Screen.Home.route) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            } else {
                onFinishedActivity()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination, // Use the dynamic startDestination here
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.Questionnaire.route) {
                QuestionnaireScreen(navController, userViewModel)
            }
            composable(route = Screen.Welcome.route) {
                WelcomeScreen(navController)
            }
            composable(route = Screen.Login.route) {
                LoginScreen(navController, userViewModel)
            }
            composable(route = Screen.NutriCoach.route) {
                NutriCoachScreen()
            }
            composable(route = Screen.Setting.route) {
                SettingScreen()
            }
            composable(route = Screen.Home.route) {
                HomeScreen(userViewModel, navController)
            }
            composable(route = Screen.Insight.route) {
                InsightsScreen(userViewModel)
            }
        }
    }
}

val bottomBarScreens = listOf(
    Screen.Home.route,
    Screen.Insight.route,
    Screen.NutriCoach.route,
    Screen.Setting.route
)