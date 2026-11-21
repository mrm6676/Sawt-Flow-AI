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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun showShortcutHint(msg: String) {
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle Android system back button when not on primary Studio tab
    if (state.currentTab != AppTab.STUDIO) {
        BackHandler {
            viewModel.selectTab(AppTab.STUDIO)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier.onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val modPressed = keyEvent.isCtrlPressed || keyEvent.isMetaPressed
                val key = keyEvent.key
                when {
                    // Ctrl+Shift+Enter -> Approve & Dispatch
                    modPressed && keyEvent.isShiftPressed && (key == Key.Enter || key == Key.NumPadEnter) -> {
                        viewModel.selectTab(AppTab.STUDIO)
                        viewModel.approveAction()
                        showShortcutHint(
                            if (isAr) "تم اعتماد وإرسال الرد (Ctrl+Shift+Enter)" else "Response approved & dispatched (Ctrl+Shift+Enter)"
                        )
                        true
                    }
                    // Ctrl+Enter -> Trigger audio transcription (Gemini)
                    modPressed && !keyEvent.isShiftPressed && !keyEvent.isAltPressed && (key == Key.Enter || key == Key.NumPadEnter) -> {
                        viewModel.selectTab(AppTab.STUDIO)
                        viewModel.transcribeAudioWithGemini(context)
                        showShortcutHint(
                            if (isAr) "جارٍ تفريغ الصوت (Ctrl+Enter)" else "Transcribing audio (Ctrl+Enter)"
                        )
                        true
                    }
                    // Ctrl+S -> Save current transcript to History
                    modPressed && !keyEvent.isShiftPressed && !keyEvent.isAltPressed && key == Key.S -> {
                        viewModel.saveCurrentTranscriptToHistory("Shortcut Saved")
                        showShortcutHint(
                            if (isAr) "تم حفظ التفريغ في السجل (Ctrl+S)" else "Transcript saved to History (Ctrl+S)"
                        )
                        true
                    }
                    // Alt+L -> Toggle language
                    keyEvent.isAltPressed && !modPressed && key == Key.L -> {
                        viewModel.toggleLanguage()
                        true
                    }
                    // Alt+1..5 -> Switch tabs
                    keyEvent.isAltPressed && !modPressed && key == Key.One -> { viewModel.selectTab(AppTab.STUDIO); true }
                    keyEvent.isAltPressed && !modPressed && key == Key.Two -> { viewModel.selectTab(AppTab.HISTORY); true }
                    keyEvent.isAltPressed && !modPressed && key == Key.Three -> { viewModel.selectTab(AppTab.ORDERS); true }
                    keyEvent.isAltPressed && !modPressed && key == Key.Four -> { viewModel.selectTab(AppTab.AUDIT); true }
                    keyEvent.isAltPressed && !modPressed && key == Key.Five -> { viewModel.selectTab(AppTab.OVERVIEW); true }
                    else -> false
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
