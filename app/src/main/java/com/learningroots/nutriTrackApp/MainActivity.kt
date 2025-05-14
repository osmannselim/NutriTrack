package com.learningroots.nutriTrackApp

import com.learningroots.nutriTrackApp.data.db.AppDatabase
import com.learningroots.nutriTrackApp.navigation.BottomNavigationBar
import com.learningroots.nutriTrackApp.screens.NutriCoachScreen
import com.learningroots.nutriTrackApp.screens.HomeScreen
import com.learningroots.nutriTrackApp.screens.InsightsScreen
import com.learningroots.nutriTrackApp.navigation.Screen
import com.learningroots.nutriTrackApp.screens.LoginScreen
import com.learningroots.nutriTrackApp.screens.QuestionnaireScreen
import com.learningroots.nutriTrackApp.screens.RegisterScreen
import com.learningroots.nutriTrackApp.screens.SettingScreen
import com.learningroots.nutriTrackApp.screens.WelcomeScreen
import com.learningroots.nutriTrackApp.ui.theme.MyApplicationTheme
import com.learningroots.nutriTrackApp.viewmodel.UserViewModel
import com.learningroots.nutriTrackApp.data.repository.Repository
import com.learningroots.nutriTrackApp.utils.loadPatientsFromCSV

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)

        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("firstLaunch", true)

        if (isFirstLaunch) {
            val patients = loadPatientsFromCSV(this)
            lifecycleScope.launch {
                db.patientDao().insertAll(patients)
                prefs.edit().putBoolean("firstLaunch", false).apply()
            }
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {

                val repository = Repository(db.patientDao(), db.foodIntakeDao(), db.nutriCoachDao())
                val userViewModel: UserViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return UserViewModel(repository) as T
                    }
                })

                val user by userViewModel.patient.collectAsState()
                val context = LocalContext.current

                val startDestination = remember(user) {
                    if (user == null) {
                        Screen.Welcome.route
                    } else {
                        val prefs = context.getSharedPreferences("QuestionnairePrefs_${user?.userId ?: ""}", Context.MODE_PRIVATE)
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
            startDestination = startDestination,
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
            composable(route = Screen.Register.route) {
                RegisterScreen(navController, userViewModel)
            }
            composable(route = Screen.NutriCoach.route) {
                NutriCoachScreen(userViewModel)
            }
            composable(route = Screen.Setting.route) {
                SettingScreen()
            }
            composable(route = Screen.Home.route) {
                HomeScreen(userViewModel, navController)
            }
            composable(route = Screen.Insight.route) {
                InsightsScreen(navController, userViewModel)
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