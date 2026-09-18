package com.example.docue.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.docue.ui.screens.create.AppPickerScreen
import com.example.docue.ui.screens.create.CreateReminderScreen
import com.example.docue.ui.screens.create.CreateReminderViewModel
import com.example.docue.ui.screens.reminders.RemindersScreen
import com.example.docue.ui.screens.settings.SettingsScreen
import com.example.docue.ui.screens.today.TodayScreen

object Routes {
    const val TODAY = "today"
    const val REMINDERS = "reminders"
    const val SETTINGS = "settings"
    const val CREATE_REMINDER = "create_reminder"
    const val EDIT_REMINDER = "edit_reminder/{reminderId}"
    const val APP_PICKER = "app_picker"

    fun editReminder(reminderId: Long) = "edit_reminder/$reminderId"
}

@Composable
fun DoCueNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.TODAY,
        modifier = modifier
    ) {
        composable(Routes.TODAY) {
            TodayScreen(
                onCreateReminder = {
                    navController.navigate(Routes.CREATE_REMINDER)
                },
                onReminderClick = { reminderId ->
                    navController.navigate(Routes.editReminder(reminderId))
                }
            )
        }

        composable(Routes.REMINDERS) {
            RemindersScreen(
                onReminderClick = { reminderId ->
                    navController.navigate(Routes.editReminder(reminderId))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }

        composable(
            route = Routes.CREATE_REMINDER,
        ) {
            val createViewModel = viewModel<CreateReminderViewModel>()

            CreateReminderWithAppPicker(
                viewModel = createViewModel,
                navController = navController
            )
        }

        composable(
            route = Routes.EDIT_REMINDER,
            arguments = listOf(
                navArgument("reminderId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getLong("reminderId")
            val editViewModel = viewModel<CreateReminderViewModel>()

            LaunchedEffect(reminderId) {
                if (reminderId != null && reminderId > 0) {
                    editViewModel.loadReminder(reminderId)
                }
            }

            CreateReminderWithAppPicker(
                viewModel = editViewModel,
                navController = navController
            )
        }

        composable(Routes.APP_PICKER) {
            AppPickerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppSelected = { packageName, appName ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("selected_package", packageName)
                        set("selected_app_name", appName)
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun CreateReminderWithAppPicker(
    viewModel: CreateReminderViewModel,
    navController: NavHostController
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.savedStateHandle?.let { handle ->
            val packageName = handle.get<String>("selected_package")
            val appName = handle.get<String>("selected_app_name")
            if (!packageName.isNullOrBlank() && !appName.isNullOrBlank()) {
                viewModel.updateTargetApp(packageName, appName)
                handle.remove<String>("selected_package")
                handle.remove<String>("selected_app_name")
            }
        }
    }

    CreateReminderScreen(
        viewModel = viewModel,
        onNavigateBack = {
            navController.popBackStack()
        },
        onNavigateToAppPicker = {
            navController.navigate(Routes.APP_PICKER)
        },
        onAppPickerResult = { _, _ -> }
    )
}
