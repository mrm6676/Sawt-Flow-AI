package com.example.sawtflow.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawtflow.data.local.SavedTranscript
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.ui.theme.AmberAccent
import com.example.sawtflow.ui.theme.CoralAccent
import com.example.sawtflow.ui.theme.CyanAccent
import com.example.sawtflow.ui.theme.EmeraldAccent
import com.example.sawtflow.ui.theme.NavyBorder
import com.example.sawtflow.ui.theme.NavyCard
import com.example.sawtflow.ui.theme.NavyLight
import com.example.sawtflow.ui.theme.NavyPrimary
import com.example.sawtflow.ui.theme.NavySurface
import com.example.sawtflow.ui.viewmodel.AppTab
import com.example.sawtflow.ui.viewmodel.SawtFlowViewModel

@Composable
fun HistoryScreen(
    viewModel: SawtFlowViewModel,
    language: AppLanguage
) {
    val isAr = language == AppLanguage.AR
    val context = LocalContext.current
    val transcripts by viewModel.savedTranscripts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDialectFilter by remember { mutableStateOf<String?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val filteredTranscripts = remember(transcripts, searchQuery, selectedDialectFilter) {
        transcripts.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.transcript.contains(searchQuery, ignoreCase = true) ||
                    (item.extractedOrderId?.contains(searchQuery, ignoreCase = true) == true) ||
                    (item.audioFileName?.contains(searchQuery, ignoreCase = true) == true) ||
                    item.dialect.contains(searchQuery, ignoreCase = true)

            val matchesDialect = selectedDialectFilter == null ||
                    item.dialect.contains(selectedDialectFilter!!, ignoreCase = true)

            matchesQuery && matchesDialect
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = if (isAr) "مسح سجل المحادثات؟" else "Clear Conversation History?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isAr)
                        "سيتم حذف جميع التفريغات المحفوظة في قاعدة بيانات Room المحلية. لا يمكن التراجع عن هذا الإجراء."
                    else
                        "This will permanently delete all saved transcripts from the local Room database. This action cannot be undone.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text(if (isAr) "مسح الكل" else "Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_clear_history_button")
                ) {
                    Text(if (isAr) "إلغاء" else "Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_header_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isAr) "سجل المحادثات الصوتية" else "Voice Transcript History",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isAr) "قاعدة بيانات Room المحلية · تفريغات Gemini 3.8 Flash" else "Local Room Database · Gemini 3.8 Flash Transcripts",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        if (transcripts.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showClearConfirmDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(CoralAccent.copy(alpha = 0.5f))
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAr) "مسح" else "Clear", fontSize = 12.sp)
                            }
                        }
                    }

                    // Search input field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isAr) "ابحث بالنص العربي، رقم الطلب، أو الملف..." else "Search transcripts, order IDs, or files...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CyanAccent
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavyCard,
                            unfocusedContainerColor = NavyCard,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    // Dialect Filter Chips
                    val dialects = listOf(
                        "Gulf" to (if (isAr) "خليجي" else "Gulf"),
                        "Egyptian" to (if (isAr) "مصري" else "Egyptian"),
                        "Levantine" to (if (isAr) "شامي" else "Levantine"),
                        "North African" to (if (isAr) "مغاربي" else "Maghrebi")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDialectFilter == null,
                                onClick = { selectedDialectFilter = null },
                                label = { Text(if (isAr) "الكل (${transcripts.size})" else "All (${transcripts.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    selectedLabelColor = NavyPrimary,
                                    containerColor = NavyCard,
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.testTag("filter_all_dialects")
                            )
                        }

                        items(dialects) { (key, label) ->
                            FilterChip(
                                selected = selectedDialectFilter == key,
                                onClick = {
                                    selectedDialectFilter = if (selectedDialectFilter == key) null else key
                                },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    selectedLabelColor = NavyPrimary,
                                    containerColor = NavyCard,
                                    labelColor = Color.White.copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.testTag("filter_dialect_$key")
                            )
                        }
                    }
                }
            }
        }

        // Empty state
        if (filteredTranscripts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_empty_state_card"),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(NavyCard)
                                .border(1.dp, NavyBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                if (isAr) "لا توجد نتائج مطابقة لبحثك" else "No transcripts match your search"
                            } else {
                                if (isAr) "لا توجد تفريغات محفوظة بعد" else "No saved transcripts yet"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                if (isAr) "جرّب البحث بكلمة أخرى أو إلغاء فلتر اللهجة." else "Try adjusting your search query or dialect filter."
                            } else {
                                if (isAr)
                                    "عند تسجيل صوت أو رفع ملف صوتي وتفريغه عبر Gemini 3.8 Flash في الاستوديو، سيتم حفظه تلقائيًا في قاعدة بيانات Room المحلية."
                                else
                                    "When you record or upload audio and transcribe it with Gemini 3.8 Flash in Studio, it will automatically be saved to local Room storage."
                            },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )

                        if (searchQuery.isEmpty()) {
                            Button(
                                onClick = { viewModel.selectTab(AppTab.STUDIO) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = NavyPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("open_studio_from_empty_history")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAr) "الانتقال إلى الاستوديو والتفريغ" else "Go to Studio & Transcribe",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // List of saved transcript cards
            items(filteredTranscripts, key = { it.id }) { item ->
                SavedTranscriptCard(
                    item = item,
                    isAr = isAr,
                    onLoadIntoStudio = { viewModel.loadTranscriptIntoStudio(item) },
                    onDelete = { viewModel.deleteSavedTranscript(item.id) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Sound Flow Transcript", item.transcript)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(
                            context,
                            if (isAr) "تم نسخ التفريغ إلى الحافظة" else "Transcript copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun SavedTranscriptCard(
    item: SavedTranscript,
    isAr: Boolean,
    onLoadIntoStudio: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_transcript_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row: Source / audio file badge + Time + Gemini badge
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
                        imageVector = if (item.audioFileName?.endsWith(".wav") == true ||
                            item.audioFileName?.endsWith(".mp3") == true ||
                            item.audioFileName?.endsWith(".m4a") == true
                        ) Icons.Default.Audiotrack else Icons.Default.Mic,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.audioFileName ?: (if (isAr) "تسجيل صوتي مباشر" else "Live Audio"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.source,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }

                    Text(
                        text = item.formattedTime,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // Transcript text box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyLight, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = item.transcript,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Metadata badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dialect chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NavyLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.dialect,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // Intent tag
                val isCancel = item.detectedIntent.contains("cancel", ignoreCase = true) || item.detectedIntent.contains("إلغاء")
                val isModify = item.detectedIntent.contains("modify", ignoreCase = true) || item.detectedIntent.contains("change", ignoreCase = true) || item.detectedIntent.contains("تعديل")

                val intentBg: Color = when {
                    isCancel -> CoralAccent.copy(alpha = 0.2f)
                    isModify -> AmberAccent.copy(alpha = 0.2f)
                    else -> EmeraldAccent.copy(alpha = 0.2f)
                }
                val intentFg: Color = when {
                    isCancel -> CoralAccent
                    isModify -> AmberAccent
                    else -> EmeraldAccent
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(intentBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.detectedIntent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = intentFg
                    )
                }

                // Extracted Order ID badge
                if (!item.extractedOrderId.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.extractedOrderId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Confidence badge
                Text(
                    text = item.confidence,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Bottom action row: Load into Studio, Copy, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onLoadIntoStudio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = NavyPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("load_into_studio_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAr) "فتح في الاستوديو" else "Load in Studio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.testTag("copy_transcript_button_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = if (isAr) "نسخ النص" else "Copy Transcript",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_transcript_button_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = if (isAr) "حذف التفريغ" else "Delete Transcript",
                            tint = CoralAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
