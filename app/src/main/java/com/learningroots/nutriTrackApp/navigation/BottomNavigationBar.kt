package com.learningroots.nutriTrackApp.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.navigation.compose.currentBackStackEntryAsState
import com.learningroots.nutriTrackApp.R
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 *
 * Author: Osman Yuksel
 *
 * references
 * https://github.com/santosh5432/BottomBar_Navigation
 */

@Composable
fun BottomNavigationBar(
    navController: NavController
) {

    val navigationItems = listOf(
        NavigationItem(
            title = "Home",
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.size(30.dp)
                )
            },
            route = Screen.Home.route
        ),
        NavigationItem(
            title = "Insights",
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.insights),
                    contentDescription = "Insights",
                    modifier = Modifier.size(41.dp)
                )
            },
            route = Screen.Insights.route
        ),
        NavigationItem(
            title = "NutriCoach",
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.house_agent),
                    contentDescription = "NutriCoach",
                    modifier = Modifier.size(41.dp)

                )
            },
            route = Screen.NutriCoach.route
        ),
        NavigationItem(
            title = "Settings",
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(30.dp)

                )
            },
            route = Screen.Settings.route
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to avoid building up a large
                            // stack of destinations on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true // Save state of screens being popped
                            }
                            // Avoid multiple copies of the same destination when reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true // Needs saveState=true in popUpTo to work effectively
                        }
                    }
                },
                icon = {
                    item.icon()
                },
                label = {
                    Text(
                        item.title,
                        color = if (currentRoute == item.route)
                            Color.Black
                        else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: @Composable () -> Unit,
    val route: String
)
