package com.example.sawtflow.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawtflow.data.model.AppLanguage
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

@Composable
fun OverviewScreen(
    state: StudioUiState,
    viewModel: SawtFlowViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAr = state.language == AppLanguage.AR

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyPrimary)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isAr) "ذكاء صوتي عربي للأعمال" else "ARABIC VOICE INTELLIGENCE FOR BUSINESS",
                    color = CyanAccent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isAr) "صوتٌ يفهم. وأعمالٌ تتحرّك." else "Speech that makes business move.",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavyCard)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "«فهمٌ أدق. قراراتٌ أوثق. عملٌ أسرع.»",
                        color = AmberAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isAr)
                        "يفهم SawtFlow طريقة حديث عملائك الحقيقية—من اللهجات العربية إلى المزج بين العربية والإنجليزية، والضوضاء، والنية المختبئة بين الكلمات."
                    else
                        "SawtFlow understands how your customers really speak—across Arabic dialects, English switches, noisy calls, and the intent hiding between the words.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = { viewModel.selectTab(AppTab.STUDIO) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isAr) "فتح استوديو SawtFlow ←" else "Launch SawtFlow Studio →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Capabilities Section
        Text(
            text = if (isAr) "القدرات الأساسية" else "CORE CAPABILITIES",
            color = CyanAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        CapabilityCard(
            stepNumber = "01",
            title = if (isAr) "افهم التفاصيل واللهجات" else "Hear the nuance",
            desc = if (isAr)
                "تعرّف على اللهجات العربية (خليجي، مصري، شامي، مغاربي) والمزج مع الإنجليزية دون قيود."
            else
                "Recognize Arabic dialects and mixed Arabic-English speech without rigid scripts.",
            icon = Icons.Default.Headphones
        )

        CapabilityCard(
            stepNumber = "02",
            title = if (isAr) "صحّح دون تغيير المعنى" else "Resolve, don't rewrite",
            desc = if (isAr)
                "صحّح أخطاء التعرّف الصوتي مع الحفاظ الكامل على ما قصده المتحدث وسياقه."
            else
                "Correct recognition errors while retaining what the speaker actually meant.",
            icon = Icons.Default.Transform
        )

        CapabilityCard(
            stepNumber = "03",
            title = if (isAr) "حوّل النية إلى إجراء معتمد" else "Make intent actionable",
            desc = if (isAr)
                "استخرج الطلبات، الأرقام (مثل SF-1048)، والأولويات للربط المباشر مع أنظمة العمل."
            else
                "Extract order references, intent classifications, and dates for verified action.",
            icon = Icons.Default.Verified
        )

        // 5-Step Workflow
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isAr) "من الصوت إلى النتيجة المعتمدة" else "FROM VOICE TO VERIFIED OUTCOME",
                    color = CyanAccent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isAr) "مسار العمل المتصل" else "Continuous Workflow",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                val steps = listOf(
                    Triple("01", if (isAr) "استمع" else "Listen", if (isAr) "التقاط المحادثة العربية الحقيقية" else "Capture real customer speech"),
                    Triple("02", if (isAr) "فسّر" else "Interpret", if (isAr) "تصحيح الصوت وتحديد النية" else "Resolve speech errors and extract intent"),
                    Triple("03", if (isAr) "اربط" else "Connect", if (isAr) "استرجاع بيانات الطلب للقراءة فقط" else "Retrieve read-only evidence from database"),
                    Triple("04", if (isAr) "تحقق" else "Verify", if (isAr) "بوابة موافقة الإنسان في دائرة التحكم" else "Keep human approval boundary intact"),
                    Triple("05", if (isAr) "استجب" else "Respond", if (isAr) "إجابة دقيقة تمنح العميل الخطوة التالية" else "Return context-aware, verified response")
                )

                steps.forEach { (index, title, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NavyCard)
                                .border(1.dp, CyanAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = index,
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = desc,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Demo Request Form
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isAr) "طلب تجربة وعرض مخصص" else "REQUEST A DEMONSTRATION",
                    color = AmberAccent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isAr) "ادفع بالمحادثة التالية إلى الأمام" else "Bring the next conversation forward",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isAr)
                        "أخبرنا أين تبطئ محادثات العملاء بالعربية عملك، لنوضح لك كيف يمكن لـ SawtFlow إنشاء خطوة تالية موثوقة."
                    else
                        "Tell us where Arabic customer conversations are slowing your business down.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isAr) "اسمك" else "Your Name") },
                    modifier = Modifier.fillMaxWidth(),
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

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isAr) "البريد الإلكتروني للعمل" else "Work Email") },
                    modifier = Modifier.fillMaxWidth(),
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isAr) "ما الذي ترغب في أتمتته؟" else "What would you like to automate?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
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
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank()) {
                            viewModel.updateDemoForm(name, email, notes)
                            viewModel.submitDemoRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_demo_request_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    enabled = name.isNotBlank() && email.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Submit")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isAr) "إرسال طلب التجربة" else "Submit Inquiry", fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = state.isDemoSubmitted) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenSuccess.copy(alpha = 0.2f))
                            .border(1.dp, GreenSuccess, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess)
                            Text(
                                text = if (isAr)
                                    "شكراً لك! تم استلام طلبك وتسجيله في قائمة المتابعة."
                                else
                                    "Thanks! Your request has been validated and logged in the inquiry queue.",
                                color = Color(0xFF81C784),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun CapabilityCard(
    stepNumber: String,
    title: String,
    desc: String,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NavyCard)
                    .border(1.dp, CyanAccent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stepNumber,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = desc,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
