package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.PowerSourceMode
import com.example.domain.SolarState
import com.example.ui.theme.CasioCasingBevel
import com.example.ui.theme.CasioCasingDark
import com.example.ui.theme.CasioCasingShadow
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.CasioLogoWhite
import com.example.ui.theme.SolarBusbar
import com.example.ui.theme.SolarCellBase
import com.example.ui.theme.SolarCellGrid
import com.example.ui.theme.SolarCellHighlight

@Composable
fun SolarPanel(
    solarState: SolarState,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "solar_sheen")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "solar_shimmer"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Casio Brand Bar & Two Way Power Label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Iconic CASIO branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CASIO",
                    color = CasioLogoWhite,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("brand_casio_logo")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "fx-991s",
                    color = CasioGoldText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Power Mode Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33000000))
                    .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleTorch
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                    .testTag("solar_lamp_toggle")
            ) {
                // Active indicator LED
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            if (solarState.isSimulatedTorchActive) Color(0xFFFBBF24)
                            else if (solarState.powerSource == PowerSourceMode.SOLAR_ONLY) Color(0xFF10B981)
                            else if (solarState.powerSource == PowerSourceMode.DUAL_POWER) Color(0xFF3B82F6)
                            else Color(0xFF6B7280)
                        )
                )

                Text(
                    text = when (solarState.powerSource) {
                        PowerSourceMode.SOLAR_ONLY -> "SOLAR 100%"
                        PowerSourceMode.DUAL_POWER -> "DUAL POWER"
                        PowerSourceMode.BATTERY_BACKUP -> "BATTERY BACKUP"
                    },
                    color = CasioGoldText,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (solarState.isSimulatedTorchActive) Icons.Default.FlashOn else Icons.Default.WbSunny,
                    contentDescription = "Simulated Lamp",
                    tint = if (solarState.isSimulatedTorchActive) Color(0xFFFBBF24) else CasioGoldText.copy(alpha = 0.6f),
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Physical Solar Cell Glass Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(4.dp), clip = false)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(CasioCasingDark, CasioCasingShadow)
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(CasioCasingBevel, CasioCasingDark)
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(2.dp)
                .clip(RoundedCornerShape(3.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleTorch
                )
                .testTag("solar_panel_strip")
        ) {
            // Photovoltaic Wafer Cells Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val numCells = 5
                val cellWidth = w / numCells

                // Base silicon wafer deep purple color
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF241629),
                            Color(0xFF140D17),
                            Color(0xFF28182D)
                        )
                    )
                )

                // Draw each photovoltaic silicon cell with fine gridlines
                for (i in 0 until numCells) {
                    val x = i * cellWidth

                    // Vertical cell division gap
                    if (i > 0) {
                        drawLine(
                            color = Color(0xFF0A070D),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = SolarBusbar,
                            start = Offset(x + 1f, 0f),
                            end = Offset(x + 1f, h),
                            strokeWidth = 1f
                        )
                    }

                    // Micro fingers (horizontal contact grid lines)
                    val numFingers = 4
                    for (j in 1..numFingers) {
                        val y = (h / (numFingers + 1)) * j
                        drawLine(
                            color = Color(0x35FFFFFF),
                            start = Offset(x + 2f, y),
                            end = Offset(x + cellWidth - 2f, y),
                            strokeWidth = 0.8f
                        )
                    }
                }

                // Glass diagonal sheen animation
                val sheenX = w * shimmerOffset
                val sheenPath = Path().apply {
                    moveTo(sheenX, 0f)
                    lineTo(sheenX + 40f, 0f)
                    lineTo(sheenX + 20f, h)
                    lineTo(sheenX - 20f, h)
                    close()
                }
                drawPath(
                    path = sheenPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0x35E0C2F5), Color.Transparent),
                        start = Offset(sheenX - 20f, 0f),
                        end = Offset(sheenX + 40f, h)
                    )
                )

                // Top glass rim highlight
                drawLine(
                    color = Color(0x40FFFFFF),
                    start = Offset(0f, 1f),
                    end = Offset(w, 1f),
                    strokeWidth = 1f
                )
            }

            // Vintage subtext over solar panel
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TWO WAY POWER",
                    color = Color(0x60FFFFFF),
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (solarState.isSimulatedTorchActive) "LAMP BOOST ☼" else "${solarState.currentLux.toInt()} lx",
                    color = if (solarState.isSimulatedTorchActive) Color(0xFFFDE68A) else Color(0x70FFFFFF),
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
