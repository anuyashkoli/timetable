package com.app.timetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Text
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.timetable.ui.screens.AddTaskScreen
import com.app.timetable.ui.theme.TimetableTheme
import dagger.hilt.android.AndroidEntryPoint
import com.app.timetable.ui.screens.HomeScreen
import com.app.timetable.ui.screens.AddSubjectScreen
import com.app.timetable.ui.screens.SettingsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimetableTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {

        // Home Screen
        composable("home") {
            HomeScreen(
                onAddTaskClick = { navController.navigate("add_task") },
                onTaskClick = { taskId ->
                    navController.navigate("add_task?taskId=$taskId") // Pass ID
                },
                onAddSubjectClick = { navController.navigate("add_subject") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable(
            route = "add_task?taskId={taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                    defaultValue = -1 // Default to -1 (New Task)
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            AddTaskScreen(
                taskId = taskId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Add Task Screen
        composable("add_subject") {
            AddSubjectScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}