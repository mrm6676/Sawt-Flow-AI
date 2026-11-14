package com.example.sawtflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.ui.components.SawtFlowHeader
import com.example.sawtflow.ui.screens.AuditScreen
import com.example.sawtflow.ui.screens.HistoryScreen
import com.example.sawtflow.ui.screens.OrdersScreen
import com.example.sawtflow.ui.screens.OverviewScreen
import com.example.sawtflow.ui.screens.StudioScreen
import com.example.sawtflow.ui.theme.AmberAccent
import com.example.sawtflow.ui.theme.CyanAccent
import com.example.sawtflow.ui.theme.NavyCard
import com.example.sawtflow.ui.theme.NavyPrimary
import com.example.sawtflow.ui.theme.SawtFlowTheme
import com.example.sawtflow.ui.viewmodel.AppTab
import com.example.sawtflow.ui.viewmodel.SawtFlowViewModel
import com.example.sawtflow.util.configureAppCheck

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            SawtFlowTheme(darkTheme = true) {
                val viewModel: SawtFlowViewModel = viewModel(
                    factory = SawtFlowViewModel.provideFactory(application)
                )
                SawtFlowApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SawtFlowApp(viewModel: SawtFlowViewModel) {
    val state by viewModel.uiState.collectAsState()
    val isAr = state.language == AppLanguage.AR
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr

    // Handle Android system back button when not on primary Studio tab
    if (state.currentTab != AppTab.STUDIO) {
        BackHandler {
            viewModel.selectTab(AppTab.STUDIO)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                SawtFlowHeader(
                    currentLanguage = state.language,
                    onToggleLanguage = { viewModel.toggleLanguage() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = NavyCard,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        Triple(AppTab.STUDIO, if (isAr) "الاستوديو" else "Studio", Icons.Default.Mic),
                        Triple(AppTab.HISTORY, if (isAr) "السجل" else "History", Icons.Default.History),
                        Triple(AppTab.ORDERS, if (isAr) "الطلبات" else "Orders", Icons.Default.Dataset),
                        Triple(AppTab.AUDIT, if (isAr) "التدقيق" else "Audit Log", Icons.Default.Assessment),
                        Triple(AppTab.OVERVIEW, if (isAr) "نظرة عامة" else "Overview", Icons.Default.Info)
                    )

                    items.forEach { (tab, label, icon) ->
                        val isSelected = state.currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyanAccent,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent.copy(alpha = 0.15f),
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            },
            containerColor = NavyPrimary
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (state.currentTab) {
                    AppTab.STUDIO -> StudioScreen(state = state, viewModel = viewModel)
                    AppTab.HISTORY -> HistoryScreen(viewModel = viewModel, language = state.language)
                    AppTab.ORDERS -> OrdersScreen(viewModel = viewModel, language = state.language)
                    AppTab.AUDIT -> AuditScreen(viewModel = viewModel, language = state.language)
                    AppTab.OVERVIEW -> OverviewScreen(state = state, viewModel = viewModel)
                }
            }
        }
    }
}
