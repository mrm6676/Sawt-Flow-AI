package com.example.sawtflow.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawtflow.ui.theme.CairoFontFamily
import java.util.Locale
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
import com.example.sawtflow.ui.viewmodel.AppTab
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
    val coroutineScope = rememberCoroutineScope()
    val isAr = state.language == AppLanguage.AR
    val canScrollDown by remember {
        derivedStateOf { scrollState.value < (scrollState.maxValue - 150) }
    }

    val context = LocalContext.current
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "voice_recording.wav"
            var fileSizeStr = "1.5 MB"
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex != -1) {
                            val bytes = cursor.getLong(sizeIndex)
                            fileSizeStr = String.format(Locale.getDefault(), "%.1f MB", bytes / (1024f * 1024f))
                        }
                    }
                }
            } catch (_: Exception) {}
            viewModel.onAudioFileUploaded(fileName, fileSizeStr, it.toString())
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                    text = if (isAr) "اختر لهجة عامية عربية:" else "Select a colloquial Arabic dialect:",
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

                // Upload Audio Recording Dropzone Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(
                            width = 1.5.dp,
                            color = if (state.uploadedAudioName != null) CyanAccent else CyanAccent.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { audioPickerLauncher.launch("audio/*") }
                        .padding(16.dp)
                        .testTag("upload_audio_dropzone"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(CyanAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = if (isAr) "رفع تسجيل صوتي عربي" else "Upload Arabic Voice Recording",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (isAr)
                                "MP3 أو WAV أو M4A أو AAC · انقر لاختيار ملف صوتي من جهازك"
                            else
                                "MP3, WAV, M4A, AAC · up to 25 MB · Tap to browse",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Audio Preview Player Card (Visible when audio uploaded or selected)
                AnimatedVisibility(visible = state.uploadedAudioName != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyCard)
                            .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .testTag("audio_preview_card")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CyanAccent)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "AUDIO",
                                            color = NavyPrimary,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = state.uploadedAudioName ?: "recording.wav",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = state.uploadedAudioSize ?: "1.4 MB",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.clearUploadedAudio() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove audio",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Playback controls & Progress
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleAudioPlayback() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (state.isPlayingAudio) AmberAccent else CyanAccent)
                                        .testTag("audio_playback_button")
                                ) {
                                    Icon(
                                        imageVector = if (state.isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (state.isPlayingAudio) "Pause" else "Play",
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    LinearProgressIndicator(
                                        progress = { state.playbackProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = CyanAccent,
                                        trackColor = NavyLight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = state.playbackPosition,
                                            color = CyanAccent,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = state.playbackDuration,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Approve Voice Recording Button (appears after uploading a voice recording)
                            Button(
                                onClick = { viewModel.approveUploadedAudio() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("approve_voice_recording_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isUploadedAudioApproved) GreenSuccess else CyanAccent,
                                    contentColor = if (state.isUploadedAudioApproved) Color.White else NavyPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = if (isAr) "اعتماد التسجيل الصوتي" else "Approve Voice Recording",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        state.isUploadedAudioApproved && isAr -> "تم اعتماد التسجيل الصوتي ✓"
                                        state.isUploadedAudioApproved -> "Voice Recording Approved ✓"
                                        isAr -> "اعتماد التسجيل الصوتي"
                                        else -> "Approve Voice Recording"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Transcribe with Gemini API Button
                            Button(
                                onClick = { viewModel.transcribeAudioWithGemini(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("gemini_transcribe_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmberAccent,
                                    contentColor = NavyPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !state.isTranscribingWithGemini
                            ) {
                                if (state.isTranscribingWithGemini) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = NavyPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isAr) "جارٍ المعالجة عبر Gemini 3.8 Flash..." else "Transcribing with Gemini 3.8 Flash...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemini AI",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isAr) "تفريغ الصوت عبر Gemini API ✨" else "Transcribe with Gemini API ✨",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Preset audio samples section
                Text(
                    text = if (isAr) "أو اختر عينة تسجيل مسبقة:" else "Or try pre-recorded voice samples:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presetClips = listOf(
                        Triple("SF-1048_gulf_inquiry.wav", "Where is my order SF-1048? They said it would arrive today.", "أبغى أعرف وين وصل طلبي SF-1048، قالوا بيوصل اليوم."),
                        Triple("SF-4420_dubai_delay.wav", "Can you check status of delayed shipment SF-4420 in Dubai?", "ممكن تفحص حالة الشحنة المتأخرة SF-4420 في دبي؟"),
                        Triple("SF-5177_cancellation.mp3", "Please cancel my order SF-5177 immediately.", "أرجو إلغاء طلبي رقم SF-5177 فوراً.")
                    )
                    presetClips.forEach { (clipName, enText, arText) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NavyCard)
                                .border(1.dp, NavyLight, RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.loadPresetAudio(clipName, "1.2 MB", enText, arText)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AudioFile,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = clipName,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
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

                // Gemini Model Indicator and trigger
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Gemini 3.8 Flash Speech AI",
                            color = AmberAccent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.transcribeAudioWithGemini(context) },
                        enabled = !state.isTranscribingWithGemini,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isAr) "تفريغ عبر Gemini ✨" else "Transcribe with Gemini ✨",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Success or Error banner
                AnimatedVisibility(visible = state.geminiSuccessMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenSuccess.copy(alpha = 0.2f))
                            .border(1.dp, GreenSuccess, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "✓ ${state.geminiSuccessMessage}",
                            color = Color(0xFF81C784),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                AnimatedVisibility(visible = state.geminiError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFBA1A1A).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFBA1A1A), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠ ${state.geminiError}",
                            color = Color(0xFFFFB4AB),
                            fontSize = 12.sp
                        )
                    }
                }

                // Transcript editor box with automatic business/order keyword highlighting
                OutlinedTextField(
                    value = state.transcriptText,
                    onValueChange = { viewModel.updateTranscript(it) },
                    readOnly = !state.isEditingTranscript,
                    visualTransformation = remember { TranscriptKeywordVisualTransformation },
                    textStyle = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontSize = 15.sp,
                        color = Color.White,
                        lineHeight = 24.sp
                    ),
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

                // Keyword highlight legend
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HighlightLegendChip(
                        label = if (isAr) "رقم الطلب" else "Order ID",
                        color = CyanAccent
                    )
                    HighlightLegendChip(
                        label = if (isAr) "طلبية / توصيل" else "Order / Delivery",
                        color = AmberAccent
                    )
                    HighlightLegendChip(
                        label = if (isAr) "إلغاء / عاجل" else "Cancel / Urgent",
                        color = Color(0xFFFF7A7A)
                    )
                }

                if (state.historySaveMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_save_message_card"),
                        colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = state.historySaveMessage,
                                color = GreenSuccess,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

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

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.saveCurrentTranscriptToHistory() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_transcript_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAr) "حفظ بالسجل" else "Save to DB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

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

        // Floating Scroll Down Quick Action
        AnimatedVisibility(
            visible = canScrollDown,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        val nextTarget = (scrollState.value + 650).coerceAtMost(scrollState.maxValue)
                        scrollState.animateScrollTo(nextTarget)
                    }
                },
                containerColor = NavyCard,
                contentColor = CyanAccent,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .border(1.5.dp, CyanAccent.copy(alpha = 0.6f), CircleShape)
                    .testTag("scroll_down_floating_button")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isAr) "تمرير لأسفل" else "Scroll down",
                    tint = CyanAccent,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

private object TranscriptKeywordVisualTransformation : VisualTransformation {
    private val orderIdRegex = Regex("\\bSF[-\\s]?\\d{4}\\b", RegexOption.IGNORE_CASE)
    private val urgentRegex = Regex(
        "(?:\\b(?:cancel|cancellation|cancelled|urgent|immediately|complaint|issue|refund)\\b|إلغاء|الغاء|ألغي|الغي|إرجاع|استرجاع|شكوى|عاجل|فوراً|مشكلة)",
        RegexOption.IGNORE_CASE
    )
    private val businessRegex = Regex(
        "(?:\\b(?:order|orders|delivery|deliver|arrive|arriving|shipment|shipped|delayed|delay|status|details|tracking|track|modify|change|invoice|payment|schedule)\\b|الطلبية|طلبية|طلبي|الطلب|طلب|التوصيل|توصيل|بيوصل|يوصل|تصل|وصل|الشحنة|شحنة|متأخرة|تأخر|حالة|تفاصيل|الموعد|موعد|العنوان|عنوان|الدفع|دفع|فاتورة|تعديل|تغيير|تبديل)",
        RegexOption.IGNORE_CASE
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val styled = buildAnnotatedString {
            append(raw)
            orderIdRegex.findAll(raw).forEach { match ->
                addStyle(
                    style = SpanStyle(
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        background = CyanAccent.copy(alpha = 0.16f)
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
            businessRegex.findAll(raw).forEach { match ->
                addStyle(
                    style = SpanStyle(
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold,
                        background = AmberAccent.copy(alpha = 0.16f)
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
            urgentRegex.findAll(raw).forEach { match ->
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFFFF8A8A),
                        fontWeight = FontWeight.Bold,
                        background = Color(0xFFFF5252).copy(alpha = 0.22f)
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

@Composable
private fun HighlightLegendChip(
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

