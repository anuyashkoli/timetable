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
import com.app.timetable.ui.screens.StatsScreen
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
import com.app.timetable.ui.screens.SubjectsListScreen
import com.app.timetable.ui.screens.TimerScreen

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
                onAddSubjectClick = { navController.navigate("subjects_list") },
                onSettingsClick = { navController.navigate("settings") },
                onStartTaskClick = { taskId ->
                    navController.navigate("timer?taskId=$taskId")
                },
                onStatsClick = { navController.navigate("stats") }
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

        composable("subjects_list") {
            SubjectsListScreen(
                onAddSubjectClick = { navController.navigate("add_subject") },
                onEditSubjectClick = { id ->
                    navController.navigate("add_subject?subjectId=$id")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_subject?subjectId={subjectId}",
            arguments = listOf(
                navArgument("subjectId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: -1
            AddSubjectScreen(
                subjectId = subjectId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "timer?taskId={taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: -1
            TimerScreen(
                taskId = taskId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("stats") {
            StatsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}