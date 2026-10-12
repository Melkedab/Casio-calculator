package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.PowerSourceMode
import com.example.domain.SolarState
import com.example.ui.theme.CasioCasingBevel
import com.example.ui.theme.CasioCasingDark
import com.example.ui.theme.CasioCasingShadow
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.LcdBackground
import com.example.ui.theme.LcdBackgroundDark
import com.example.ui.theme.LcdBorderInner
import com.example.ui.theme.LcdBorderOuter
import com.example.ui.theme.LcdSegmentActive
import com.example.ui.theme.LcdSegmentGhost

@Composable
fun LcdDisplay(
    primaryValue: String,
    formulaValue: String,
    statusBadge: String,
    hasMemory: Boolean,
    hasGrandTotal: Boolean,
    solarState: SolarState,
    modifier: Modifier = Modifier
) {
    // Outer recessed metallic casing bevel
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(10.dp), clip = false)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CasioCasingDark, CasioCasingShadow)
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(CasioCasingBevel, CasioCasingDark)
                ),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(5.dp)
    ) {
        // Inner LCD Screen Glass
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(LcdBackground, LcdBackgroundDark)
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = LcdBorderInner,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Glass Reflection & Grid Overlay
            LcdGlassGlare(modifier = Modifier.matchParentSize())

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Annunciator & Formula Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Annunciators (Casio badges)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Solar / Battery badge
                        LcdBadge(
                            text = when (solarState.powerSource) {
                                PowerSourceMode.SOLAR_ONLY -> "☀ SOLAR"
                                PowerSourceMode.DUAL_POWER -> "☀ DUAL"
                                PowerSourceMode.BATTERY_BACKUP -> "🔋 BATT"
                            },
                            isActive = true
                        )

                        LcdBadge(
                            text = "M",
                            isActive = hasMemory
                        )

                        LcdBadge(
                            text = "GT",
                            isActive = hasGrandTotal
                        )

                        if (statusBadge.isNotEmpty() && statusBadge != "M" && statusBadge != "GT") {
                            LcdBadge(
                                text = statusBadge,
                                isActive = true
                            )
                        }
                    }

                    // Upper Formula line
                    Text(
                        text = if (formulaValue.isEmpty()) "CASIO fx-SOLAR" else formulaValue,
                        color = if (formulaValue.isEmpty()) LcdSegmentGhost.copy(alpha = 0.5f) else LcdSegmentActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .testTag("lcd_formula_text")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Main 7-Segment Digital Readout
                LcdDigitalRow(
                    displayValue = primaryValue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lcd_primary_display")
                )
            }
        }
    }
}

@Composable
private fun LcdBadge(
    text: String,
    isActive: Boolean
) {
    Text(
        text = text,
        color = if (isActive) LcdSegmentActive else LcdSegmentGhost,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier
            .background(
                color = if (isActive) Color(0x18000000) else Color.Transparent,
                shape = RoundedCornerShape(2.dp)
            )
            .padding(horizontal = 3.dp, vertical = 1.dp)
    )
}

/**
 * Renders the main display using authentic segmented LCD digits.
 * If text fits in 10-12 characters, draws custom 7-segment glyphs with ghost background.
 */
@Composable
fun LcdDigitalRow(
    displayValue: String,
    modifier: Modifier = Modifier
) {
    // Clean string for digits
    val clean = displayValue.ifEmpty { "0" }

    // Height of the 7-segment digit
    val digitHeight = 44.dp
    val digitWidth = 24.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(digitHeight),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Ghost 8888888888 background layer (10 digits)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(10) {
                SevenSegmentDigit(
                    char = '8',
                    hasDecimal = false,
                    forceGhost = true,
                    digitWidth = digitWidth,
                    digitHeight = digitHeight
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
        }

        // Active digits layer
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var i = 0
            while (i < clean.length) {
                val c = clean[i]
                val nextIsDot = (i + 1 < clean.length && clean[i + 1] == '.')
                val isMinus = (c == '-')

                SevenSegmentDigit(
                    char = c,
                    hasDecimal = nextIsDot,
                    forceGhost = false,
                    digitWidth = if (c == ',' || c == ' ') 8.dp else digitWidth,
                    digitHeight = digitHeight
                )

                if (nextIsDot) {
                    i++ // Skip the dot since it's rendered as the segment's decimal point
                }
                Spacer(modifier = Modifier.width(2.dp))
                i++
            }
        }
    }
}

/**
 * Draws an authentic 7-segment LCD character with beveled hexagonal segments and italic slant
 */
@Composable
fun SevenSegmentDigit(
    char: Char,
    hasDecimal: Boolean,
    forceGhost: Boolean,
    digitWidth: Dp,
    digitHeight: Dp,
    modifier: Modifier = Modifier
) {
    // If it's a comma
    if (char == ',') {
        Canvas(modifier = modifier.width(digitWidth).height(digitHeight)) {
            val color = if (forceGhost) LcdSegmentGhost else LcdSegmentActive
            val w = size.width
            val h = size.height
            drawCircle(
                color = color,
                radius = 2.5f,
                center = Offset(w * 0.5f, h * 0.92f)
            )
            val path = Path().apply {
                moveTo(w * 0.5f, h * 0.92f)
                lineTo(w * 0.3f, h * 0.99f)
            }
            drawPath(path, color)
        }
        return
    }

    // 7 Segment Bitmask: A (top), B (top-right), C (bottom-right), D (bottom), E (bottom-left), F (top-left), G (middle)
    val segments = getSegmentMask(char)

    Canvas(
        modifier = modifier
            .width(digitWidth)
            .height(digitHeight)
    ) {
        val w = size.width
        val h = size.height

        // Slant angle: 6 degrees
        val slant = w * 0.08f
        val segThickness = w * 0.17f
        val gap = w * 0.04f

        val activeColor = if (forceGhost) LcdSegmentGhost else LcdSegmentActive
        val ghostColor = LcdSegmentGhost

        fun drawSegment(isActive: Boolean, path: Path) {
            drawPath(path = path, color = if (isActive && !forceGhost) activeColor else ghostColor)
        }

        val halfH = h * 0.5f

        // Segment A: Top horizontal
        val pathA = Path().apply {
            moveTo(segThickness + gap + slant * 0.8f, 0f)
            lineTo(w - segThickness - gap + slant * 0.8f, 0f)
            lineTo(w - segThickness * 1.6f + slant * 0.7f, segThickness)
            lineTo(segThickness * 1.6f + slant * 0.7f, segThickness)
            close()
        }
        drawSegment(segments[0], pathA)

        // Segment B: Top-right vertical
        val pathB = Path().apply {
            moveTo(w + slant * 0.8f, gap)
            lineTo(w + slant * 0.4f, halfH - gap)
            lineTo(w - segThickness + slant * 0.4f, halfH - segThickness * 0.7f)
            lineTo(w - segThickness + slant * 0.8f, segThickness * 1.2f)
            close()
        }
        drawSegment(segments[1], pathB)

        // Segment C: Bottom-right vertical
        val pathC = Path().apply {
            moveTo(w + slant * 0.4f, halfH + gap)
            lineTo(w, h - gap)
            lineTo(w - segThickness, h - segThickness * 1.2f)
            lineTo(w - segThickness + slant * 0.4f, halfH + segThickness * 0.7f)
            close()
        }
        drawSegment(segments[2], pathC)

        // Segment D: Bottom horizontal
        val pathD = Path().apply {
            moveTo(segThickness * 1.6f, h - segThickness)
            lineTo(w - segThickness * 1.6f, h - segThickness)
            lineTo(w - segThickness - gap, h)
            lineTo(segThickness + gap, h)
            close()
        }
        drawSegment(segments[3], pathD)

        // Segment E: Bottom-left vertical
        val pathE = Path().apply {
            moveTo(slant * 0.4f, halfH + gap)
            lineTo(segThickness + slant * 0.4f, halfH + segThickness * 0.7f)
            lineTo(segThickness, h - segThickness * 1.2f)
            lineTo(0f, h - gap)
            close()
        }
        drawSegment(segments[4], pathE)

        // Segment F: Top-left vertical
        val pathF = Path().apply {
            moveTo(slant * 0.8f, gap)
            lineTo(segThickness + slant * 0.8f, segThickness * 1.2f)
            lineTo(segThickness + slant * 0.4f, halfH - segThickness * 0.7f)
            lineTo(slant * 0.4f, halfH - gap)
            close()
        }
        drawSegment(segments[5], pathF)

        // Segment G: Middle horizontal
        val pathG = Path().apply {
            moveTo(slant * 0.4f + segThickness * 1.2f, halfH)
            lineTo(slant * 0.4f + segThickness * 1.8f, halfH - segThickness * 0.5f)
            lineTo(w + slant * 0.4f - segThickness * 1.8f, halfH - segThickness * 0.5f)
            lineTo(w + slant * 0.4f - segThickness * 1.2f, halfH)
            lineTo(w + slant * 0.4f - segThickness * 1.8f, halfH + segThickness * 0.5f)
            lineTo(slant * 0.4f + segThickness * 1.8f, halfH + segThickness * 0.5f)
            close()
        }
        drawSegment(segments[6], pathG)

        // Decimal Point (DP)
        if (hasDecimal || forceGhost) {
            val dpRadius = segThickness * 0.55f
            drawCircle(
                color = if (hasDecimal && !forceGhost) activeColor else ghostColor,
                radius = dpRadius,
                center = Offset(w + dpRadius * 1.1f, h - dpRadius)
            )
        }
    }
}

private fun getSegmentMask(c: Char): BooleanArray {
    return when (c) {
        '0' -> booleanArrayOf(true, true, true, true, true, true, false)
        '1' -> booleanArrayOf(false, true, true, false, false, false, false)
        '2' -> booleanArrayOf(true, true, false, true, true, false, true)
        '3' -> booleanArrayOf(true, true, true, true, false, false, true)
        '4' -> booleanArrayOf(false, true, true, false, false, true, true)
        '5' -> booleanArrayOf(true, false, true, true, false, true, true)
        '6' -> booleanArrayOf(true, false, true, true, true, true, true)
        '7' -> booleanArrayOf(true, true, true, false, false, false, false)
        '8' -> booleanArrayOf(true, true, true, true, true, true, true)
        '9' -> booleanArrayOf(true, true, true, true, false, true, true)
        '-' -> booleanArrayOf(false, false, false, false, false, false, true)
        'E', 'e' -> booleanArrayOf(true, false, false, true, true, true, true)
        'C', 'c' -> booleanArrayOf(true, false, false, true, true, true, false)
        'H', 'h' -> booleanArrayOf(false, true, true, false, true, true, true)
        'L', 'l' -> booleanArrayOf(false, false, false, true, true, true, false)
        'o' -> booleanArrayOf(false, false, true, true, true, false, true)
        'P', 'p' -> booleanArrayOf(true, true, false, false, true, true, true)
        'r' -> booleanArrayOf(false, false, false, false, true, false, true)
        else -> booleanArrayOf(false, false, false, false, false, false, false)
    }
}

@Composable
private fun LcdGlassGlare(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // Diagonal gloss streak
        val path = Path().apply {
            moveTo(size.width * 0.15f, 0f)
            lineTo(size.width * 0.45f, 0f)
            lineTo(size.width * 0.25f, size.height)
            lineTo(size.width * 0.05f, size.height)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(Color(0x18FFFFFF), Color(0x05FFFFFF)),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
    }
}
