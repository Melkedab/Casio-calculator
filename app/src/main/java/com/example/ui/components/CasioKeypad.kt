package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CasioCyanText
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.CasioLogoWhite
import com.example.ui.theme.CasioSubLabel
import com.example.ui.theme.KeycapCharcoal
import com.example.ui.theme.KeycapCharcoalBorder
import com.example.ui.theme.KeycapEquals
import com.example.ui.theme.KeycapMemory
import com.example.ui.theme.KeycapOperator
import com.example.ui.theme.KeycapOrangeC
import com.example.ui.theme.KeycapRedAc
import com.example.ui.theme.KeycapScientific

data class KeyAction(
    val label: String,
    val subLabel: String = "",
    val subLabelColor: Color = CasioGoldText,
    val baseColor: Color = KeycapCharcoal,
    val textColor: Color = CasioLogoWhite,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun CasioKeypad(
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
    isScientificExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Optional Scientific functions tray when toggled
        if (isScientificExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CasioButton(
                    action = KeyAction(
                        label = "x²",
                        subLabel = "POW",
                        baseColor = KeycapScientific,
                        testTag = "btn_square",
                        onClick = onSquare
                    ),
                    modifier = Modifier.weight(1f)
                )
                CasioButton(
                    action = KeyAction(
                        label = "1/x",
                        subLabel = "RECIP",
                        baseColor = KeycapScientific,
                        testTag = "btn_reciprocal",
                        onClick = onReciprocal
                    ),
                    modifier = Modifier.weight(1f)
                )
                CasioButton(
                    action = KeyAction(
                        label = "π",
                        subLabel = "CONST",
                        baseColor = KeycapScientific,
                        testTag = "btn_pi",
                        onClick = { onDigit("3.14159265") }
                    ),
                    modifier = Modifier.weight(1f)
                )
                CasioButton(
                    action = KeyAction(
                        label = "(",
                        subLabel = "L-PAR",
                        baseColor = KeycapScientific,
                        testTag = "btn_paren_open",
                        onClick = { onDigit("(") }
                    ),
                    modifier = Modifier.weight(1f)
                )
                CasioButton(
                    action = KeyAction(
                        label = ")",
                        subLabel = "R-PAR",
                        baseColor = KeycapScientific,
                        testTag = "btn_paren_close",
                        onClick = { onDigit(")") }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 1: TAX-, TAX+, √, %, +/-
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CasioButton(
                action = KeyAction(
                    label = "TAX−",
                    subLabel = "EXCL",
                    textColor = CasioGoldText,
                    testTag = "btn_tax_minus",
                    onClick = onTaxMinus
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "TAX+",
                    subLabel = "INCL",
                    textColor = CasioGoldText,
                    testTag = "btn_tax_plus",
                    onClick = onTaxPlus
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "√",
                    subLabel = "ROOT",
                    testTag = "btn_sqrt",
                    onClick = onSquareRoot
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "%",
                    subLabel = "DELTA",
                    testTag = "btn_percent",
                    onClick = onPercent
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "±",
                    subLabel = "CHS",
                    testTag = "btn_sign",
                    onClick = onToggleSign
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: MC, MR, M-, M+, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CasioButton(
                action = KeyAction(
                    label = "MC",
                    subLabel = "CLEAR",
                    baseColor = KeycapMemory,
                    testTag = "btn_mc",
                    onClick = onMemoryClear
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "MR",
                    subLabel = "RECALL",
                    baseColor = KeycapMemory,
                    testTag = "btn_mr",
                    onClick = onMemoryRecall
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "M−",
                    subLabel = "SUB",
                    baseColor = KeycapMemory,
                    testTag = "btn_m_minus",
                    onClick = onMemorySubtract
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "M+",
                    subLabel = "ADD",
                    baseColor = KeycapMemory,
                    testTag = "btn_m_plus",
                    onClick = onMemoryAdd
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "÷",
                    subLabel = "DIV",
                    baseColor = KeycapOperator,
                    textColor = CasioCyanText,
                    testTag = "btn_div",
                    onClick = { onOperator("÷") }
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 7, 8, 9, GT, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CasioButton(
                action = KeyAction(
                    label = "7",
                    testTag = "btn_digit_7",
                    onClick = { onDigit("7") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "8",
                    testTag = "btn_digit_8",
                    onClick = { onDigit("8") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "9",
                    testTag = "btn_digit_9",
                    onClick = { onDigit("9") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "GT",
                    subLabel = "TOTAL",
                    baseColor = KeycapCharcoal,
                    textColor = CasioGoldText,
                    testTag = "btn_gt",
                    onClick = onGrandTotal
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "×",
                    subLabel = "MUL",
                    baseColor = KeycapOperator,
                    textColor = CasioCyanText,
                    testTag = "btn_mul",
                    onClick = { onOperator("×") }
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 4, 5, 6, C, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CasioButton(
                action = KeyAction(
                    label = "4",
                    testTag = "btn_digit_4",
                    onClick = { onDigit("4") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "5",
                    testTag = "btn_digit_5",
                    onClick = { onDigit("5") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "6",
                    testTag = "btn_digit_6",
                    onClick = { onDigit("6") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "C",
                    subLabel = "ENTRY",
                    baseColor = KeycapOrangeC,
                    textColor = Color.White,
                    testTag = "btn_c",
                    onClick = onClear
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "−",
                    subLabel = "SUB",
                    baseColor = KeycapOperator,
                    textColor = CasioCyanText,
                    testTag = "btn_sub",
                    onClick = { onOperator("−") }
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: 1, 2, 3, AC, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CasioButton(
                action = KeyAction(
                    label = "1",
                    testTag = "btn_digit_1",
                    onClick = { onDigit("1") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "2",
                    testTag = "btn_digit_2",
                    onClick = { onDigit("2") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "3",
                    testTag = "btn_digit_3",
                    onClick = { onDigit("3") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "AC",
                    subLabel = "ALL CLR",
                    baseColor = KeycapRedAc,
                    textColor = Color.White,
                    testTag = "btn_ac",
                    onClick = onAllClear
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "+",
                    subLabel = "ADD",
                    baseColor = KeycapOperator,
                    textColor = CasioCyanText,
                    testTag = "btn_add",
                    onClick = { onOperator("+") }
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 6: 0, 00, ., ⌫, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CasioButton(
                action = KeyAction(
                    label = "0",
                    testTag = "btn_digit_0",
                    onClick = { onDigit("0") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "00",
                    testTag = "btn_digit_00",
                    onClick = { onDigit("00") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = ".",
                    testTag = "btn_dot",
                    onClick = { onDigit(".") }
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "▶",
                    subLabel = "DEL",
                    subLabelColor = CasioSubLabel,
                    baseColor = KeycapCharcoal,
                    testTag = "btn_backspace",
                    onClick = onBackspace
                ),
                modifier = Modifier.weight(1f)
            )
            CasioButton(
                action = KeyAction(
                    label = "=",
                    subLabel = "EXEC",
                    baseColor = KeycapEquals,
                    textColor = Color.White,
                    testTag = "btn_equals",
                    onClick = onEquals
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Molded 3D Casio Keycap with top highlight, bottom drop-shadow, and pressed state depression
 */
@Composable
fun CasioButton(
    action: KeyAction,
    modifier: Modifier = Modifier,
    height: Dp = 54.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val yOffset by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        label = "key_depression"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        label = "key_elevation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Printed sub-label on faceplate above key
        if (action.subLabel.isNotEmpty()) {
            Text(
                text = action.subLabel,
                color = action.subLabelColor,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(11.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(11.dp))
        }

        // Keycap Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = yOffset)
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(8.dp),
                    clip = false
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isPressed) {
                            listOf(action.baseColor, action.baseColor.copy(alpha = 0.85f))
                        } else {
                            listOf(
                                action.baseColor.lighter(0.12f),
                                action.baseColor,
                                action.baseColor.darker(0.18f)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x35FFFFFF),
                            action.baseColor.darker(0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = action.onClick
                )
                .testTag(action.testTag),
            contentAlignment = Alignment.Center
        ) {
            // Key Legend Text
            Text(
                text = action.label,
                color = action.textColor,
                fontSize = when {
                    action.label.length >= 4 -> 12.sp
                    action.label.length >= 3 -> 14.sp
                    action.label in listOf("÷", "×", "+", "−", "=") -> 22.sp
                    else -> 19.sp
                },
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Color tint helpers
private fun Color.lighter(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

private fun Color.darker(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}
