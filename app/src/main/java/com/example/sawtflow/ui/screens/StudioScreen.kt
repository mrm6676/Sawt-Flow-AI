package com.example.sawtflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.data.model.Dialect
import com.example.sawtflow.data.model.IntentType
import com.example.sawtflow.ui.components.AudioWaveformBanner
import com.example.sawtflow.ui.components.StepBadge
import com.example.sawtflow.ui.theme.AmberAccent
import com.example.sawtflow.ui.theme.CyanAccent
import com.example.sawtflow.ui.theme.GreenSuccess
import com.example.sawtflow.ui.theme.NavyCard
import com.example.sawtflow.ui.theme.NavyLight
import com.example.sawtflow.ui.theme.NavyPrimary
import com.example.sawtflow.ui.theme.NavySurface
import com.example.sawtflow.ui.viewmodel.SawtFlowViewModel
import com.example.sawtflow.ui.viewmodel.StudioUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioScreen(
    state: StudioUiState,
    viewModel: SawtFlowViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAr = state.language == AppLanguage.AR

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyPrimary)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Workspace Eyebrow & Hero Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isAr) "استوديو SAWTFLOW / مساحة الذكاء الصوتي" else "SAWTFLOW STUDIO / VOICE INTELLIGENCE WORKSPACE",
                color = CyanAccent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (isAr) "حوّل الصوت إلى الإجراء الموثوق التالي." else "Turn voice into the next right action.",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 30.sp
            )
            Text(
                text = if (isAr)
                    "ارفع محادثة، وصحّح ما التقطه التعرّف الصوتي، وحوّل نية العميل إلى رد تجاري معتمد."
                else
                    "Upload or record a conversation, correct speech recognition, and turn customer intent into a verified business response.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        // Live Audio Signal Visualizer
        AudioWaveformBanner(isRecording = state.isRecording)

        // ------------------ STEP 1: INPUT ------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("step_input_card"),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StepBadge(
                    indexText = if (isAr) "01 / إدخال الصوت" else "01 / INPUT",
                    statusText = if (isAr) "جاهز" else "READY",
                    statusColor = CyanAccent
                )

                Text(
                    text = if (isAr) "ابدأ بصوت حقيقي." else "Start with a real voice.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isAr) "اختر لهجة عامية عربية أو استخدم التسجيل المباشر:" else "Select a colloquial Arabic dialect or use direct recording:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )

                // Dialect selector chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Dialect.entries.forEach { dialect ->
                        val isSelected = state.selectedDialect == dialect
                        SuggestionChip(
                            onClick = { viewModel.selectDialect(dialect) },
                            label = {
                                Text(
                                    text = if (isAr) dialect.labelAr else dialect.labelEn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) CyanAccent else NavyCard,
                                labelColor = if (isSelected) NavyPrimary else Color.White
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (isSelected) CyanAccent else NavyLight
                            )
                        )
                    }
                }

                // Action buttons: Record & Sample
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.toggleRecording() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("record_voice_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isRecording) Color(0xFFD32F2F) else AmberAccent,
                            contentColor = NavyPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Record"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isRecording) {
                                if (isAr) "إيقاف التسجيل" else "Stop Recording"
                            } else {
                                if (isAr) "تسجيل صوتي" else "Record Voice"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.loadSample() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("load_sample_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sample")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAr) "رسالة نموذجية" else "Use Sample",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Audio status chip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyCard)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "• ${state.audioStatusMessage}",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // ------------------ STEP 2: UNDERSTAND (TRANSCRIPT) ------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("step_understand_card"),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StepBadge(
                    indexText = if (isAr) "02 / فهم الكلام" else "02 / UNDERSTAND",
                    statusText = if (isAr) "التعرف الصوتي جاهز" else "SPEECH RECOGNITION READY",
                    statusColor = CyanAccent
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isAr) "صحّح المعنى، لا المتحدث." else "Correct the meaning, not the speaker.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAr) "يحافظ SawtFlow على النية بينما تنقّح الأخطاء." else "SawtFlow preserves intent while you refine errors.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }

                // Transcript editor box
                OutlinedTextField(
                    value = state.transcriptText,
                    onValueChange = { viewModel.updateTranscript(it) },
                    readOnly = !state.isEditingTranscript,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transcript_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavyCard,
                        unfocusedContainerColor = NavyCard,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyLight,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleEditTranscript() }) {
                            Icon(
                                imageVector = if (state.isEditingTranscript) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit Transcript",
                                tint = if (state.isEditingTranscript) AmberAccent else CyanAccent
                            )
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "مستوى الثقة ${state.confidence}" else "Confidence ${state.confidence}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = { viewModel.analyzeIntent() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = NavyPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("analyze_intent_button")
                    ) {
                        Text(
                            text = if (isAr) "تحليل النية ←" else "Analyze intent →",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ------------------ STEP 3: INTENT INTELLIGENCE ------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("step_intent_card"),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StepBadge(
                    indexText = if (isAr) "03 / النية المكتشفة" else "03 / INTENT",
                    statusText = state.intentConfidence,
                    statusColor = AmberAccent
                )

                Text(
                    text = if (isAr) "ما الذي يحتاجه العميل." else "What the customer needs.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Intent highlight badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(1.dp, AmberAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmberAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "◎",
                                color = AmberAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = if (isAr) "النية المكتشفة" else "DETECTED INTENT",
                                color = AmberAccent,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAr) state.detectedIntent.labelAr else state.detectedIntent.labelEn,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Tags row (Language, Urgency, Extracted Order)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyCard)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isAr) "اللغة" else "LANGUAGE",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isAr) "عربي / إنجليزي" else "Arabic / English",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyCard)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isAr) "الأولوية" else "URGENCY",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = state.urgency,
                                color = if (state.urgency == "High") AmberAccent else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyCard)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isAr) "الطلب المستخرج" else "EXTRACTED",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = state.extractedOrderId ?: "N/A",
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ------------------ STEP 4: ACTION & RETRIEVAL ------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("step_action_card"),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StepBadge(
                    indexText = if (isAr) "04 / استرجاع الأدلة" else "04 / ACTION",
                    statusText = if (isAr) "بحث معتمد" else "APPROVED LOOKUP",
                    statusColor = AmberAccent
                )

                Text(
                    text = if (isAr) "استرجع الدليل الصحيح." else "Retrieve the right evidence.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isAr)
                        "يقترح الوكيل بحثاً في الطلب للقراءة فقط قبل صياغة أي رد."
                    else
                        "The agent proposes a read-only order lookup before drafting a response.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )

                // Order search input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.orderInputId,
                        onValueChange = { viewModel.updateOrderInput(it) },
                        label = { Text(if (isAr) "رقم الطلب" else "ORDER ID") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_id_input_field"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = NavyCard,
                            unfocusedContainerColor = NavyCard,
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Button(
                        onClick = { viewModel.retrieveOrder() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = NavyPrimary),
                        modifier = Modifier.testTag("retrieve_order_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Retrieve")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isAr) "استرجاع" else "Lookup", fontWeight = FontWeight.Bold)
                    }
                }

                // Quick test chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("SF-1048", "SF-2086", "SF-3312", "SF-4420", "SF-5177").forEach { id ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NavyCard)
                                .clickable {
                                    viewModel.updateOrderInput(id)
                                    viewModel.retrieveOrder(id)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = id,
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Evidence card when retrieved
                AnimatedVisibility(visible = state.activeOrder != null) {
                    state.activeOrder?.let { order ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyCard)
                                .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${if (isAr) "طلب" else "ORDER"}: ${order.id}",
                                        color = CyanAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (order.status) {
                                                    "Delivered" -> GreenSuccess
                                                    "Canceled" -> Color(0xFFBA1A1A)
                                                    else -> AmberAccent
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = order.status,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                HorizontalDivider(color = NavyLight)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (isAr) "الموعد المتوقع" else "ETA",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = order.eta,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isAr) "آخر فحص" else "LAST SCAN",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = order.lastScan,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ------------------ STEP 5: APPROVAL (HUMAN-IN-THE-LOOP) ------------------
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("step_approval_card"),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StepBadge(
                    indexText = if (isAr) "05 / اعتماد بشري" else "05 / APPROVAL",
                    statusText = if (isAr) "بوابة الإنسان" else "HUMAN GATE",
                    statusColor = AmberAccent
                )

                Text(
                    text = if (isAr) "استجب بسياق كامل." else "Respond with context.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Draft response box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isAr) "الرد المقترح" else "DRAFT RESPONSE",
                            color = AmberAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.draftResponse,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Approve action button
                Button(
                    onClick = { viewModel.approveAction() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("approve_response_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isApproved) GreenSuccess else AmberAccent,
                        contentColor = if (state.isApproved) Color.White else NavyPrimary
                    ),
                    enabled = state.activeOrder != null
                ) {
                    Icon(
                        imageVector = if (state.isApproved) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = "Approve"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isApproved) {
                            if (isAr) "تم الاعتماد · جاهز للإرسال" else "Approved · Ready to send"
                        } else {
                            if (isAr) "اعتماد الرد ✓" else "Approve response ✓"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Confirmation badge
                AnimatedVisibility(visible = state.isApproved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenSuccess.copy(alpha = 0.2f))
                            .border(1.dp, GreenSuccess, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (isAr)
                                "✓ تم تسجيل الاعتماد في سجل التدقيق بنجاح مع المحافظة على خصوصية البيانات."
                            else
                                "✓ Approval logged to audit trail. Response ready to dispatch to customer.",
                            color = Color(0xFF81C784),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
