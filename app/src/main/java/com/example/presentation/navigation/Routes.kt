package com.example.presentation.navigation

/**
 * Type-safe navigation routes for the FieldForce app.
 * All routes defined as constants to prevent typos.
 */
sealed class Screen(val route: String) {
    /**
     * Welcome/Login screen
     */
    object Welcome : Screen("welcome")

    /**
     * Main dashboard screen
     */
    object Dashboard : Screen("dashboard")

    /**
     * Tasks screen
     */
    object Tasks : Screen("tasks")

    /**
     * Attendance screen
     */
    object Attendance : Screen("attendance")

    /**
     * Files/Expenses screen
     */
    object Files : Screen("files")

    /**
     * Map screen
     */
    object Map : Screen("map")

    /**
     * Settings/Specs screen
     */
    object Settings : Screen("settings")

    /**
     * Task detail screen
     */
    object TaskDetail : Screen("task_detail/{taskId}") {
        /**
         * Creates the route with task ID parameter.
         */
        fun createRoute(taskId: Int): String {
            return "task_detail/$taskId"
        }
    }

    /**
     * Visit detail screen
     */
    object VisitDetail : Screen("visit_detail/{visitId}") {
        /**
         * Creates the route with visit ID parameter.
         */
        fun createRoute(visitId: Int): String {
            return "visit_detail/$visitId"
        }
    }

    companion object {
        /**
         * Navigation argument keys
         */
        const val ARG_TASK_ID = "taskId"
        const val ARG_VISIT_ID = "visitId"
        const val ARG_USER_ID = "userId"
        const val ARG_ROLE = "role"
    }
}

/**
 * Deep link routes for external navigation.
 */
object DeepLinks {
    const val SCHEME = "fieldforce"
    const val HOST = "app"

    const val DASHBOARD = "$SCHEME://$HOST/dashboard"
    const val TASKS = "$SCHEME://$HOST/tasks"
    const val ATTENDANCE = "$SCHEME://$HOST/attendance"
    const val FILES = "$SCHEME://$HOST/files"
}
