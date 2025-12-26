package com.myaiapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.myaiapp.ui.screens.assets.AssetsScreen
import com.myaiapp.ui.screens.budget.BudgetScreen
import com.myaiapp.ui.screens.calendar.CalendarScreen
import com.myaiapp.ui.screens.home.HomeScreen
import com.myaiapp.ui.screens.record.RecordScreen
import com.myaiapp.ui.screens.records.RecordsScreen
import com.myaiapp.ui.screens.savings.SavingsScreen
import com.myaiapp.ui.screens.settings.SettingsScreen
import com.myaiapp.ui.screens.settings.AISettingsScreen
import com.myaiapp.ui.screens.settings.BackupScreen
import com.myaiapp.ui.screens.settings.BookManageScreen
import com.myaiapp.ui.screens.settings.CategoryManageScreen
import com.myaiapp.ui.screens.settings.ImportScreen
import com.myaiapp.ui.screens.statistics.StatisticsScreen

/**
 * 导航路由
 */
object Routes {
    const val HOME = "home"
    const val RECORDS = "records"
    const val STATISTICS = "statistics"
    const val ASSETS = "assets"
    const val RECORD = "record"
    const val BUDGET = "budget"
    const val SAVINGS = "savings"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val BOOK_MANAGE = "book_manage"
    const val CATEGORY_MANAGE = "category_manage"
    const val AI_SETTINGS = "ai_settings"
    const val BACKUP = "backup"
    const val IMPORT = "import"

    // 带参数的路由
    const val RECORD_EDIT = "record/{transactionId}"
    const val BUDGET_DETAIL = "budget/{budgetId}"
    const val SAVINGS_DETAIL = "savings/{planId}"

    fun recordEdit(transactionId: String) = "record/$transactionId"
    fun budgetDetail(budgetId: String) = "budget/$budgetId"
    fun savingsDetail(planId: String) = "savings/$planId"
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

        // 资产
        composable(Routes.ASSETS) {
            AssetsScreen(
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
                onNavigateToBookManage = { navController.navigate(Routes.BOOK_MANAGE) },
                onNavigateToCategoryManage = { navController.navigate(Routes.CATEGORY_MANAGE) },
                onNavigateToAISettings = { navController.navigate(Routes.AI_SETTINGS) },
                onNavigateToBackup = { navController.navigate(Routes.BACKUP) },
                onNavigateToImport = { navController.navigate(Routes.IMPORT) }
            )
        }

        // 账本管理
        composable(Routes.BOOK_MANAGE) {
            BookManageScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // 分类管理
        composable(Routes.CATEGORY_MANAGE) {
            CategoryManageScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // AI设置
        composable(Routes.AI_SETTINGS) {
            AISettingsScreen(
                onBack = { navController.popBackStack() }
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
    }
}
