package com.myaiapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.myaiapp.ui.screens.budget.BudgetScreen
import com.myaiapp.ui.screens.calendar.CalendarScreen
import com.myaiapp.ui.screens.home.HomeScreen
import com.myaiapp.ui.screens.record.RecordScreen
import com.myaiapp.ui.screens.records.RecordsScreen
import com.myaiapp.ui.screens.savings.SavingsScreen
import com.myaiapp.ui.screens.settings.SettingsScreen
import com.myaiapp.ui.screens.settings.BackupScreen
import com.myaiapp.ui.screens.settings.ImportScreen
import com.myaiapp.ui.screens.settings.ReminderSettingsScreen
import com.myaiapp.ui.screens.settings.CurrencySettingsScreen
import com.myaiapp.ui.screens.search.SearchScreen
import com.myaiapp.ui.screens.statistics.StatisticsScreen

/**
 * 导航路由
 */
object Routes {
    const val HOME = "home"
    const val RECORDS = "records"
    const val STATISTICS = "statistics"
    const val RECORD = "record"
    const val BUDGET = "budget"
    const val SAVINGS = "savings"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val BACKUP = "backup"
    const val IMPORT = "import"
    const val SEARCH = "search"
    const val REMINDER_SETTINGS = "reminder_settings"
    const val CURRENCY_SETTINGS = "currency_settings"

    // 带参数的路由
    const val RECORD_EDIT = "record/{transactionId}"

    fun recordEdit(transactionId: String) = "record/$transactionId"
}

/**
 * 导航图
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    onShowRecordSheet: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // 首页
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToRecords = { navController.navigate(Routes.RECORDS) },
                onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) },
                onNavigateToBudget = { navController.navigate(Routes.BUDGET) },
                onNavigateToSavings = { navController.navigate(Routes.SAVINGS) },
                onNavigateToCalendar = { navController.navigate(Routes.CALENDAR) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onAddRecord = onShowRecordSheet
            )
        }

        // 明细列表
        composable(Routes.RECORDS) {
            RecordsScreen(
                onBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Routes.recordEdit(transactionId))
                },
                onAddRecord = onShowRecordSheet
            )
        }

        // 统计
        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 新增/编辑记录
        composable(Routes.RECORD) {
            RecordScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RECORD_EDIT,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            RecordScreen(
                transactionId = transactionId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // 预算
        composable(Routes.BUDGET) {
            BudgetScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 存钱计划
        composable(Routes.SAVINGS) {
            SavingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 日历
        composable(Routes.CALENDAR) {
            CalendarScreen(
                onBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Routes.recordEdit(transactionId))
                }
            )
        }

        // 设置
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToBackup = { navController.navigate(Routes.BACKUP) },
                onNavigateToImport = { navController.navigate(Routes.IMPORT) },
                onNavigateToReminder = { navController.navigate(Routes.REMINDER_SETTINGS) },
                onNavigateToCurrency = { navController.navigate(Routes.CURRENCY_SETTINGS) }
            )
        }

        // 备份与恢复
        composable(Routes.BACKUP) {
            BackupScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 导入账单
        composable(Routes.IMPORT) {
            ImportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 搜索
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Routes.recordEdit(transactionId))
                }
            )
        }

        // 提醒设置
        composable(Routes.REMINDER_SETTINGS) {
            ReminderSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 货币设置
        composable(Routes.CURRENCY_SETTINGS) {
            CurrencySettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
