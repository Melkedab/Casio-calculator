package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.SolarState
import com.example.ui.components.CasioKeypad
import com.example.ui.components.LcdDisplay
import com.example.ui.components.SolarPanel
import com.example.ui.theme.CasioCasingBevel
import com.example.ui.theme.CasioCasingDark
import com.example.ui.theme.CasioCasingLight
import com.example.ui.theme.CasioCasingMid
import com.example.ui.theme.CasioCasingShadow
import com.example.ui.theme.CasioCyanText
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.CasioLogoWhite
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.CalculatorUiState

@Composable
fun CalculatorScreen(
    calcState: CalculatorUiState,
    solarState: SolarState,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onAllClear: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onPercent: () -> Unit,
    onSquareRoot: () -> Unit,
    onSquare: () -> Unit,
    onReciprocal: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    onGrandTotal: () -> Unit,
    onTaxPlus: () -> Unit,
    onTaxMinus: () -> Unit,
    onToggleScientific: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Solar Panel Header
        SolarPanel(
            solarState = solarState,
            onToggleTorch = onToggleTorch
        )

        // Authentic LCD Segmented Screen
        LcdDisplay(
            primaryValue = calcState.displayPrimary,
            formulaValue = calcState.displayFormula,
            statusBadge = calcState.statusBadge,
            hasMemory = calcState.hasMemory,
            hasGrandTotal = calcState.hasGrandTotal,
            solarState = solarState
        )

        // Utility Bar: Scientific toggle, Sound, Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(CasioCasingMid)
                .border(1.dp, CasioCasingBevel, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scientific Tray Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (calcState.isScientificExpanded) CasioGoldText else CasioCasingLight)
                    .clickable(onClick = onToggleScientific)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("btn_toggle_scientific"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = "Scientific Functions",
                    tint = if (calcState.isScientificExpanded) Color.Black else CasioLogoWhite,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (calcState.isScientificExpanded) "SCI ON" else "SCI OFF",
                    color = if (calcState.isScientificExpanded) Color.Black else CasioLogoWhite,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Sub info label
            Text(
                text = "ELECTRONIC CALCULATOR",
                color = CasioGoldText.copy(alpha = 0.8f),
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Right icons: Sound & Settings
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier.size(28.dp).testTag("btn_toggle_sound")
                ) {
                    Icon(
                        imageVector = if (calcState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Toggle Sound",
                        tint = if (calcState.isSoundEnabled) CasioCyanText else CasioLogoWhite.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(28.dp).testTag("btn_open_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = CasioLogoWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Full Casio Tactile Keypad
        CasioKeypad(
            onDigit = onDigit,
            onOperator = onOperator,
            onEquals = onEquals,
            onAllClear = onAllClear,
            onClear = onClear,
            onBackspace = onBackspace,
            onToggleSign = onToggleSign,
            onPercent = onPercent,
            onSquareRoot = onSquareRoot,
            onSquare = onSquare,
            onReciprocal = onReciprocal,
            onMemoryAdd = onMemoryAdd,
            onMemorySubtract = onMemorySubtract,
            onMemoryRecall = onMemoryRecall,
            onMemoryClear = onMemoryClear,
            onGrandTotal = onGrandTotal,
            onTaxPlus = onTaxPlus,
            onTaxMinus = onTaxMinus,
            isScientificExpanded = calcState.isScientificExpanded,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}
