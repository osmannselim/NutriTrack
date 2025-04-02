package com.learningroots.nutriTrackApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen() {

    val userViewModel = remember { UserViewModel() }

    val navController = rememberNavController()

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->

        val graph =
            navController.createGraph(startDestination = Screen.Welcome.route) {

                composable (route = Screen.Questionnaire.route) {
                    QuestionnaireScreen(navController,userViewModel)
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
                    HomeScreen(userViewModel)
                }
                composable(route = Screen.Insight.route) {
                    InsightsScreen(userViewModel)
                }
            }
        NavHost(
            navController = navController,
            graph = graph,
            modifier = Modifier.padding(innerPadding)
        )

    }
}

val bottomBarScreens = listOf(
    Screen.Home.route,
    Screen.Insight.route,
    Screen.NutriCoach.route,
    Screen.Setting.route
)


