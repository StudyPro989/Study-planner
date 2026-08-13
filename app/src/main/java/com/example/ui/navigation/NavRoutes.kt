package com.example.ui.navigation

sealed class Screen(val route: String, val title: String, val iconName: String) {
    data object Dashboard : Screen("dashboard", "Dashboard", "home")
    data object Subjects : Screen("subjects", "Subjects", "menu_book")
    data object Schedule : Screen("schedule", "Schedule", "calendar_month")
    data object AiCoach : Screen("ai_coach", "AI Coach", "psychology")
    data object Settings : Screen("settings", "Settings", "settings")

    data object SubjectDetail : Screen("subject_detail/{subjectId}", "Chapters", "folder") {
        fun createRoute(subjectId: String): String = "subject_detail/$subjectId"
    }
}
