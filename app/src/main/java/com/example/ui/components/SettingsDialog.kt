package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.PowerSourceMode
import com.example.domain.SolarState
import com.example.ui.theme.CasioCasingBevel
import com.example.ui.theme.CasioCasingDark
import com.example.ui.theme.CasioCasingLight
import com.example.ui.theme.CasioCasingMid
import com.example.ui.theme.CasioCyanText
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.CasioLogoWhite
import com.example.ui.theme.KeycapEquals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    taxRate: Double,
    isSoundEnabled: Boolean,
    isHapticsEnabled: Boolean,
    solarState: SolarState,
    onSetTaxRate: (Double) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleTorch: () -> Unit,
    onSetSimulatedLux: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CasioCasingDark)
            .border(1.5.dp, CasioCasingBevel, RoundedCornerShape(16.dp))
            .padding(18.dp)
            .testTag("settings_dialog")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CASIO",
                        color = CasioLogoWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SETTINGS",
                        color = CasioGoldText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CasioLogoWhite
                    )
                }
            }

            // TAX Rate Selector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CasioCasingMid, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "TAX RATE: $taxRate%",
                    color = CasioGoldText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(5.0, 7.5, 8.25, 10.0, 15.0, 20.0).forEach { rate ->
                        val isSelected = (rate == taxRate)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) CasioGoldText else CasioCasingLight)
                                .clickable { onSetTaxRate(rate) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${rate.toInt()}%",
                                color = if (isSelected) Color.Black else CasioLogoWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tactile & Sound Switches
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CasioCasingMid, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sound Click Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Sound",
                            tint = CasioCyanText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Key Click Audio",
                            color = CasioLogoWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CasioGoldText,
                            checkedTrackColor = CasioCasingLight
                        )
                    )
                }

                // Vibration Haptics Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptics",
                            tint = CasioCyanText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Tactile Key Haptics",
                            color = CasioLogoWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = isHapticsEnabled,
                        onCheckedChange = { onToggleHaptics() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CasioGoldText,
                            checkedTrackColor = CasioCasingLight
                        )
                    )
                }
            }

            // Solar Power System Status & Light Simulation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CasioCasingMid, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Solar",
                            tint = CasioGoldText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "SOLAR POWER SYSTEM",
                            color = CasioGoldText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Simulated Torch Boost Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (solarState.isSimulatedTorchActive) Color(0xFFFBBF24) else CasioCasingLight)
                            .clickable(onClick = onToggleTorch)
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Lamp Boost",
                            tint = if (solarState.isSimulatedTorchActive) Color.Black else CasioGoldText,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (solarState.isSimulatedTorchActive) "LAMP ON" else "LAMP BOOST",
                            color = if (solarState.isSimulatedTorchActive) Color.Black else CasioLogoWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Mode: ${
                        when (solarState.powerSource) {
                            PowerSourceMode.SOLAR_ONLY -> "100% Photovoltaic Solar Power"
                            PowerSourceMode.DUAL_POWER -> "Dual Power (Solar + Battery Assist)"
                            PowerSourceMode.BATTERY_BACKUP -> "Battery Backup Mode (Low Ambient Light)"
                        }
                    }",
                    color = CasioLogoWhite,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "Lux: ${solarState.currentLux.toInt()} lx | Solar Efficiency: ${solarState.solarEfficiencyPercent}%",
                    color = CasioCyanText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Ambient light slider for emulator / manual testing
                Slider(
                    value = solarState.simulatedLightLevel,
                    onValueChange = onSetSimulatedLux,
                    valueRange = 0f..1000f,
                    colors = SliderDefaults.colors(
                        thumbColor = CasioGoldText,
                        activeTrackColor = CasioGoldText,
                        inactiveTrackColor = CasioCasingLight
                    )
                )
            }

            // Close button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("DONE", color = CasioGoldText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
