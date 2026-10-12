package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ConversionUnit
import com.example.domain.SolarState
import com.example.domain.UnitCategory
import com.example.domain.UnitConverterEngine
import com.example.ui.components.CasioButton
import com.example.ui.components.KeyAction
import com.example.ui.components.SolarPanel
import com.example.ui.theme.CasioCasingBevel
import com.example.ui.theme.CasioCasingDark
import com.example.ui.theme.CasioCasingLight
import com.example.ui.theme.CasioCasingMid
import com.example.ui.theme.CasioCasingShadow
import com.example.ui.theme.CasioCyanText
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.CasioLogoWhite
import com.example.ui.theme.KeycapCharcoal
import com.example.ui.theme.KeycapEquals
import com.example.ui.theme.KeycapOrangeC
import com.example.ui.viewmodel.ConverterUiState

@Composable
fun UnitConverterScreen(
    converterState: ConverterUiState,
    solarState: SolarState,
    onSelectCategory: (UnitCategory) -> Unit,
    onSelectFromUnit: (ConversionUnit) -> Unit,
    onSelectToUnit: (ConversionUnit) -> Unit,
    onSwapUnits: () -> Unit,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSendToCalc: () -> Unit,
    onImportFromCalc: () -> Unit,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    val categoryUnits = remember(converterState.selectedCategory) {
        UnitConverterEngine.unitsByCategory[converterState.selectedCategory] ?: emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Solar Panel Top Strip
        SolarPanel(
            solarState = solarState,
            onToggleTorch = onToggleTorch
        )

        // Categories Horizontal Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("converter_category_row"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(UnitConverterEngine.categories) { category ->
                val isSelected = category == converterState.selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CasioGoldText else CasioCasingLight)
                        .border(
                            1.dp,
                            if (isSelected) CasioGoldText else CasioCasingBevel,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("cat_${category.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.displayName,
                        color = if (isSelected) Color.Black else CasioLogoWhite,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Two LCD Displays for FROM and TO
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(10.dp))
                .background(CasioCasingMid, RoundedCornerShape(10.dp))
                .border(1.5.dp, CasioCasingBevel, RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "FROM" Box
            ConverterLcdBox(
                label = "FROM",
                unit = converterState.fromUnit,
                value = converterState.inputValue,
                onUnitClick = { showFromMenu = true },
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Value", converterState.inputValue))
                    Toast.makeText(context, "Copied input", Toast.LENGTH_SHORT).show()
                },
                testTag = "converter_from_box"
            )

            // Dropdown menu for FROM units
            DropdownMenu(
                expanded = showFromMenu,
                onDismissRequest = { showFromMenu = false },
                modifier = Modifier.background(CasioCasingDark)
            ) {
                categoryUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${unit.name} (${unit.symbol})",
                                color = if (unit == converterState.fromUnit) CasioGoldText else CasioLogoWhite,
                                fontWeight = if (unit == converterState.fromUnit) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelectFromUnit(unit)
                            showFromMenu = false
                        }
                    )
                }
            }

            // Swap Button & Quick bridge row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Import from Calc
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable(onClick = onImportFromCalc)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "From Calc",
                        tint = CasioCyanText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text("FROM CALC", color = CasioCyanText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // Swap Button
                IconButton(
                    onClick = onSwapUnits,
                    modifier = Modifier
                        .size(34.dp)
                        .background(CasioCasingLight, RoundedCornerShape(17.dp))
                        .border(1.dp, CasioGoldText, RoundedCornerShape(17.dp))
                        .testTag("btn_swap_units")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap Units",
                        tint = CasioGoldText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Send to Calc
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable(onClick = onSendToCalc)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_send_to_calc"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("SEND TO CALC", color = CasioGoldText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "To Calc",
                        tint = CasioGoldText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // "TO" Box
            ConverterLcdBox(
                label = "TO",
                unit = converterState.toUnit,
                value = converterState.resultValue,
                onUnitClick = { showToMenu = true },
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Result", converterState.resultValue))
                    Toast.makeText(context, "Copied result: ${converterState.resultValue}", Toast.LENGTH_SHORT).show()
                },
                testTag = "converter_to_box"
            )

            // Dropdown menu for TO units
            DropdownMenu(
                expanded = showToMenu,
                onDismissRequest = { showToMenu = false },
                modifier = Modifier.background(CasioCasingDark)
            ) {
                categoryUnits.forEach { unit ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${unit.name} (${unit.symbol})",
                                color = if (unit == converterState.toUnit) CasioGoldText else CasioLogoWhite,
                                fontWeight = if (unit == converterState.toUnit) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelectToUnit(unit)
                            showToMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Compact Converter Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Row 1: 7, 8, 9, C
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CasioButton(
                    action = KeyAction(label = "7", testTag = "conv_btn_7", onClick = { onDigit("7") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(label = "8", testTag = "conv_btn_8", onClick = { onDigit("8") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(label = "9", testTag = "conv_btn_9", onClick = { onDigit("9") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(
                        label = "C",
                        baseColor = KeycapOrangeC,
                        textColor = Color.White,
                        testTag = "conv_btn_c",
                        onClick = onClear
                    ),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
            }

            // Row 2: 4, 5, 6, ⌫
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CasioButton(
                    action = KeyAction(label = "4", testTag = "conv_btn_4", onClick = { onDigit("4") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(label = "5", testTag = "conv_btn_5", onClick = { onDigit("5") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(label = "6", testTag = "conv_btn_6", onClick = { onDigit("6") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(
                        label = "⌫",
                        baseColor = KeycapCharcoal,
                        testTag = "conv_btn_del",
                        onClick = onBackspace
                    ),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
            }

            // Row 3: 1, 2, 3, ±
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CasioButton(
                    action = KeyAction(label = "1", testTag = "conv_btn_1", onClick = { onDigit("1") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(label = "2", testTag = "conv_btn_2", onClick = { onDigit("2") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(label = "3", testTag = "conv_btn_3", onClick = { onDigit("3") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(
                        label = "00",
                        testTag = "conv_btn_00",
                        onClick = { onDigit("00") }
                    ),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
            }

            // Row 4: 0, ., = (Send to Calc)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CasioButton(
                    action = KeyAction(label = "0", testTag = "conv_btn_0", onClick = { onDigit("0") }),
                    modifier = Modifier.weight(1.5f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(label = ".", testTag = "conv_btn_dot", onClick = { onDigit(".") }),
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                CasioButton(
                    action = KeyAction(
                        label = "TO CALC →",
                        baseColor = KeycapEquals,
                        textColor = Color.White,
                        testTag = "conv_btn_calc",
                        onClick = onSendToCalc
                    ),
                    modifier = Modifier.weight(1.5f),
                    height = 46.dp
                )
            }
        }
    }
}

@Composable
private fun ConverterLcdBox(
    label: String,
    unit: ConversionUnit,
    value: String,
    onUnitClick: () -> Unit,
    onCopy: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8B9D7E), Color(0xFF7E9071))
                )
            )
            .border(1.dp, Color(0xFF323B40), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unit Selector pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x25000000))
                        .clickable(onClick = onUnitClick)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$label: ${unit.name} (${unit.symbol}) ▼",
                        color = Color(0xFF121B13),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Copy button
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Value",
                        tint = Color(0xFF121B13),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Large LCD Value Readout
            Text(
                text = value,
                color = Color(0xFF121B13),
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
