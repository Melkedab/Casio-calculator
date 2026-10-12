package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HistoryTapeScreen
import com.example.ui.screens.UnitConverterScreen
import com.example.ui.theme.CasioCasingBevel
import com.example.ui.theme.CasioCasingDark
import com.example.ui.theme.CasioCasingLight
import com.example.ui.theme.CasioCasingMid
import com.example.ui.theme.CasioCasingShadow
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.CasioLogoWhite
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.CalculatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: CalculatorViewModel) {
    val calcState by viewModel.calcState.collectAsStateWithLifecycle()
    val converterState by viewModel.converterState.collectAsStateWithLifecycle()
    val solarState by viewModel.solarState.collectAsStateWithLifecycle()
    val historyList by viewModel.calculationHistory.collectAsStateWithLifecycle()

    var showSettings by remember { mutableStateOf(false) }

    // Handle system back navigation if in secondary screen
    if (calcState.appMode != AppMode.CALCULATOR) {
        BackHandler {
            viewModel.setAppMode(AppMode.CALCULATOR)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CasioCasingDark),
        containerColor = CasioCasingDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CasioCasingMid,
                            CasioCasingDark,
                            CasioCasingShadow
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Vintage Mechanical Mode Selector Switch (Casio Slider Bar)
            CasioModeSwitch(
                selectedMode = calcState.appMode,
                historyCount = historyList.size,
                onModeSelected = { viewModel.setAppMode(it) }
            )

            // Main Active Screen Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (calcState.appMode) {
                    AppMode.CALCULATOR -> {
                        CalculatorScreen(
                            calcState = calcState,
                            solarState = solarState,
                            onDigit = { viewModel.onDigit(it) },
                            onOperator = { viewModel.onOperator(it) },
                            onEquals = { viewModel.onEquals() },
                            onAllClear = { viewModel.onAllClear() },
                            onClear = { viewModel.onClear() },
                            onBackspace = { viewModel.onBackspace() },
                            onToggleSign = { viewModel.onToggleSign() },
                            onPercent = { viewModel.onPercent() },
                            onSquareRoot = { viewModel.onSquareRoot() },
                            onSquare = { viewModel.onSquare() },
                            onReciprocal = { viewModel.onReciprocal() },
                            onMemoryAdd = { viewModel.onMemoryAdd() },
                            onMemorySubtract = { viewModel.onMemorySubtract() },
                            onMemoryRecall = { viewModel.onMemoryRecall() },
                            onMemoryClear = { viewModel.onMemoryClear() },
                            onGrandTotal = { viewModel.onGrandTotal() },
                            onTaxPlus = { viewModel.onTaxPlus() },
                            onTaxMinus = { viewModel.onTaxMinus() },
                            onToggleScientific = { viewModel.toggleScientific() },
                            onToggleSound = { viewModel.toggleSound() },
                            onOpenSettings = { showSettings = true },
                            onToggleTorch = { viewModel.toggleSolarTorch() }
                        )
                    }

                    AppMode.CONVERTER -> {
                        UnitConverterScreen(
                            converterState = converterState,
                            solarState = solarState,
                            onSelectCategory = { viewModel.selectConverterCategory(it) },
                            onSelectFromUnit = { viewModel.selectFromUnit(it) },
                            onSelectToUnit = { viewModel.selectToUnit(it) },
                            onSwapUnits = { viewModel.swapConverterUnits() },
                            onDigit = { viewModel.onConverterDigit(it) },
                            onBackspace = { viewModel.onConverterBackspace() },
                            onClear = { viewModel.onConverterClear() },
                            onSendToCalc = { viewModel.sendConverterResultToCalc() },
                            onImportFromCalc = { viewModel.importCalcDisplayToConverter() },
                            onToggleTorch = { viewModel.toggleSolarTorch() }
                        )
                    }

                    AppMode.HISTORY_TAPE -> {
                        HistoryTapeScreen(
                            historyList = historyList,
                            onRecallEntry = { viewModel.recallHistoryEntry(it) },
                            onDeleteEntry = { viewModel.deleteHistoryEntry(it) },
                            onClearHistory = { viewModel.clearHistory() },
                            onBackToCalc = { viewModel.setAppMode(AppMode.CALCULATOR) }
                        )
                    }
                }
            }
        }
    }

    // Settings & Solar Tuning Dialog
    if (showSettings) {
        SettingsDialog(
            taxRate = calcState.taxRate,
            isSoundEnabled = calcState.isSoundEnabled,
            isHapticsEnabled = calcState.isHapticsEnabled,
            solarState = solarState,
            onSetTaxRate = { viewModel.setTaxRate(it) },
            onToggleSound = { viewModel.toggleSound() },
            onToggleHaptics = { viewModel.toggleHaptics() },
            onToggleTorch = { viewModel.toggleSolarTorch() },
            onSetSimulatedLux = { viewModel.setSimulatedLux(it) },
            onDismiss = { showSettings = false }
        )
    }
}

/**
 * Authentic mechanical slider switch for Casio calculator modes
 */
@Composable
fun CasioModeSwitch(
    selectedMode: AppMode,
    historyCount: Int,
    onModeSelected: (AppMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .background(CasioCasingDark, RoundedCornerShape(12.dp))
            .border(1.dp, CasioCasingBevel, RoundedCornerShape(12.dp))
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ModeSwitchTab(
                title = "CALCULATOR",
                icon = Icons.Default.Calculate,
                badge = null,
                isSelected = selectedMode == AppMode.CALCULATOR,
                onClick = { onModeSelected(AppMode.CALCULATOR) },
                testTag = "tab_mode_calculator",
                modifier = Modifier.weight(1f)
            )

            ModeSwitchTab(
                title = "CONVERTER",
                icon = Icons.Default.Transform,
                badge = null,
                isSelected = selectedMode == AppMode.CONVERTER,
                onClick = { onModeSelected(AppMode.CONVERTER) },
                testTag = "tab_mode_converter",
                modifier = Modifier.weight(1f)
            )

            ModeSwitchTab(
                title = "TAPE LOG",
                icon = Icons.Default.ReceiptLong,
                badge = if (historyCount > 0) historyCount.toString() else null,
                isSelected = selectedMode == AppMode.HISTORY_TAPE,
                onClick = { onModeSelected(AppMode.HISTORY_TAPE) },
                testTag = "tab_mode_tape",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeSwitchTab(
    title: String,
    icon: ImageVector,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(CasioGoldText, Color(0xFFB88E3E))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.Black else CasioLogoWhite.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                color = if (isSelected) Color.Black else CasioLogoWhite.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color.Black else CasioGoldText)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = if (isSelected) CasioGoldText else Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
