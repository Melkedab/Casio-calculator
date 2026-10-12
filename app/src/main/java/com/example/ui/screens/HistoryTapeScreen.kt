package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalculationEntity
import com.example.ui.theme.CasioCasingBevel
import com.example.ui.theme.CasioCasingDark
import com.example.ui.theme.CasioCasingLight
import com.example.ui.theme.CasioCasingMid
import com.example.ui.theme.CasioCyanText
import com.example.ui.theme.CasioGoldText
import com.example.ui.theme.CasioLogoWhite
import com.example.ui.theme.KeycapCharcoal
import com.example.ui.theme.KeycapRedAc
import com.example.ui.theme.TapeInkBlue
import com.example.ui.theme.TapeInkDark
import com.example.ui.theme.TapeInkRed
import com.example.ui.theme.TapePaperBg
import com.example.ui.theme.TapePaperShadow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryTapeScreen(
    historyList: List<CalculationEntity>,
    onRecallEntry: (CalculationEntity) -> Unit,
    onDeleteEntry: (CalculationEntity) -> Unit,
    onClearHistory: () -> Unit,
    onBackToCalc: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tape Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CasioCasingMid)
                .border(1.dp, CasioCasingBevel, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PAPER TAPE",
                    color = CasioLogoWhite,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "(${historyList.size} ITEMS)",
                    color = CasioGoldText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Share Tape
                if (historyList.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val text = buildReceiptText(historyList)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Calculation Tape"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Tape",
                            tint = CasioCyanText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Clear Tape
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.size(32.dp).testTag("btn_clear_tape")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear All Tape",
                            tint = KeycapRedAc,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        // Paper Receipt Roll Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(6.dp))
                .background(TapePaperBg, RoundedCornerShape(6.dp))
                .border(1.dp, TapePaperShadow, RoundedCornerShape(6.dp))
        ) {
            if (historyList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "--- TAPE ROLL EMPTY ---",
                        color = TapeInkDark.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Perform calculations or unit conversions to print receipt lines on the roll.",
                        color = TapeInkDark.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBackToCalc,
                        colors = ButtonDefaults.buttonColors(containerColor = CasioCasingDark)
                    ) {
                        Text("OPEN CALCULATOR", color = CasioGoldText, fontSize = 11.sp)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Perforated Top Edge
                    PerforatedReceiptEdge(modifier = Modifier.fillMaxWidth().height(10.dp))

                    // Tape Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CASIO ELECTRONIC CALCULATOR",
                            color = TapeInkDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "PRINTING TAPE LOG",
                            color = TapeInkBlue,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "================================",
                            color = TapeInkDark.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Tape Entries
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .testTag("history_tape_list"),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList, key = { it.id }) { item ->
                            HistoryTapeRow(
                                item = item,
                                onRecall = { onRecallEntry(item) },
                                onDelete = { onDeleteEntry(item) },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Calc", "${item.expression} = ${item.result}"))
                                    Toast.makeText(context, "Copied: ${item.result}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // Perforated Bottom Edge
                    PerforatedReceiptEdge(modifier = Modifier.fillMaxWidth().height(10.dp), inverted = true)
                }
            }
        }
    }

    // Confirm Clear Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text("Clear History Tape", color = CasioLogoWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to clear all calculations on the paper tape log?", color = CasioLogoWhite.copy(alpha = 0.8f))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("CLEAR ALL", color = KeycapRedAc, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("CANCEL", color = CasioGoldText)
                }
            },
            containerColor = CasioCasingDark
        )
    }
}

@Composable
private fun HistoryTapeRow(
    item: CalculationEntity,
    onRecall: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val timeStr = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0x0C000000))
            .border(0.8.dp, Color(0x15000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("history_item_${item.id}")
    ) {
        Column {
            // Tag and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[${item.tag}]",
                        color = when (item.tag) {
                            "TAX" -> TapeInkRed
                            "CONVERSION" -> TapeInkBlue
                            else -> TapeInkDark.copy(alpha = 0.6f)
                        },
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeStr,
                        color = TapeInkDark.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Row action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(22.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TapeInkDark.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    IconButton(onClick = onRecall, modifier = Modifier.size(22.dp)) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Recall to Calc",
                            tint = TapeInkBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TapeInkRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Expression
            Text(
                text = item.expression,
                color = TapeInkDark.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            // Result
            Text(
                text = "= ${item.result}",
                color = TapeInkDark,
                fontSize = 17.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRecall)
            )

            Text(
                text = "- - - - - - - - - - - - - - - -",
                color = TapeInkDark.copy(alpha = 0.2f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PerforatedReceiptEdge(
    modifier: Modifier = Modifier,
    inverted: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val teethWidth = 14f
        val numTeeth = (w / teethWidth).toInt()

        val path = Path().apply {
            if (!inverted) {
                moveTo(0f, 0f)
                for (i in 0..numTeeth) {
                    val x = i * teethWidth
                    lineTo(x + teethWidth * 0.5f, h)
                    lineTo(x + teethWidth, 0f)
                }
                lineTo(w, h)
                lineTo(0f, h)
            } else {
                moveTo(0f, h)
                for (i in 0..numTeeth) {
                    val x = i * teethWidth
                    lineTo(x + teethWidth * 0.5f, 0f)
                    lineTo(x + teethWidth, h)
                }
                lineTo(w, 0f)
                lineTo(0f, 0f)
            }
            close()
        }
        drawPath(path, color = Color(0xFF1C2024))
    }
}

private fun buildReceiptText(list: List<CalculationEntity>): String {
    val sb = StringBuilder()
    sb.appendLine("================================")
    sb.appendLine(" CASIO ELECTRONIC CALCULATOR ")
    sb.appendLine("        PAPER TAPE LOG          ")
    sb.appendLine("================================")
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    for (item in list) {
        sb.appendLine("[${item.tag}] ${sdf.format(Date(item.timestamp))}")
        sb.appendLine("  ${item.expression}")
        sb.appendLine("  = ${item.result}")
        sb.appendLine("--------------------------------")
    }
    sb.appendLine("================================")
    return sb.toString()
}
