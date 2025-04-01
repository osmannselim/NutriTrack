package com.learningroots.osmanYuksel92351838.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size

import com.learningroots.osmanYuksel92351838.R



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
    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }

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
            route = Screen.Insight.route
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
            route = Screen.Setting.route
        )
    )


    NavigationBar(
        containerColor = Color.White
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
                    selectedNavigationIndex.intValue = index
                    // current route  and backstackentry
                    navController.navigate(item.route)
                },
                icon = {
                    item.icon()
                },
                label = {
                    Text(
                        item.title,
                        color = if (index == selectedNavigationIndex.intValue)
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
