package com.myaiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myaiapp.ui.components.BottomNavBar
import com.myaiapp.ui.navigation.AppNavGraph
import com.myaiapp.ui.navigation.Routes
import com.myaiapp.ui.screens.record.RecordScreen
import com.myaiapp.ui.theme.AppColors
import com.myaiapp.ui.theme.MyAIAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边缘到边缘显示
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MyAIAppTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.HOME

    // 控制记账弹窗显示
    var showRecordSheet by remember { mutableStateOf(false) }

    // 需要显示底部导航的页面
    val showBottomBar = currentRoute in listOf(
        Routes.HOME,
        Routes.RECORDS,
        Routes.STATISTICS,
        Routes.ASSETS
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onAddClick = { showRecordSheet = true }
                )
            }
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp
                )
        ) {
            AppNavGraph(
                navController = navController,
                onShowRecordSheet = { showRecordSheet = true }
            )
        }
    }

    // 记账底部弹窗
    if (showRecordSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecordSheet = false },
            containerColor = AppColors.Background,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            RecordScreen(
                onBack = { showRecordSheet = false },
                onSaved = { showRecordSheet = false }
            )
        }
    }
}
