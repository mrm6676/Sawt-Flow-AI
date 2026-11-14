package com.example.sawtflow.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawtflow.R
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.ui.theme.AmberAccent
import com.example.sawtflow.ui.theme.CyanAccent
import com.example.sawtflow.ui.theme.GreenSuccess
import com.example.sawtflow.ui.theme.NavyBorder
import com.example.sawtflow.ui.theme.NavyCard
import com.example.sawtflow.ui.theme.NavyLight
import com.example.sawtflow.ui.theme.NavyPrimary
import com.example.sawtflow.ui.theme.NavySurface
import com.example.sawtflow.ui.viewmodel.AppTab
import com.example.sawtflow.ui.viewmodel.SawtFlowViewModel
import com.example.sawtflow.ui.viewmodel.StudioUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverviewScreen(
    state: StudioUiState,
    viewModel: SawtFlowViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isAr = state.language == AppLanguage.AR
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var contactEmail by remember { mutableStateOf(state.contactEmail) }
    var contactSubject by remember { mutableStateOf(state.contactSubject) }
    var contactMessage by remember { mutableStateOf(state.contactMessage) }
    val isValidEmail = contactEmail.contains("@") && contactEmail.contains(".")

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
                    text = if (isAr) "ذكاء صوتي عربي وإنجليزي للأعمال" else "ARABIC & ENGLISH VOICE INTELLIGENCE FOR BUSINESS",
                    color = Color.White,
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
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isAr)
                        "يفهم Sound Flow طريقة حديث عملائك الحقيقية—بالعربية ولهجاتها أو بالإنجليزية أو بالمزج بينهما، والنية المختبئة بين الكلمات."
                    else
                        "Sound Flow understands how your customers really speak—across Arabic dialects, English, code-switching, noisy calls, and the intent hiding between the words.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = { viewModel.selectTab(AppTab.STUDIO) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A4B0A), contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isAr) "فتح استوديو Sound Flow ←" else "Launch Sound Flow Studio →",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Capabilities Section
        Text(
            text = if (isAr) "القدرات الأساسية" else "CORE CAPABILITIES",
            color = Color.White,
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
                    color = Color.White,
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
                                color = Color.White,
                                fontSize = 12.sp,
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
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Contact Us Email Form
        Card(
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("contact_us_card")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isAr) "تواصل معنا" else "CONTACT US",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (isAr) "أرسل لنا رسالة مباشرة" else "Send us an email",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isAr)
                        "هل لديك استفسار حول أتمتة الصوت العربي أو التكامل مع أنظمتك؟ تواصل مع فريق Sound Flow عبر البريد الإلكتروني."
                    else
                        "Have a question about Arabic & English voice automation or integrations? Reach out to our team directly.",
                    color = Color.White,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = {
                        contactEmail = it
                        viewModel.updateContactForm(contactEmail, contactSubject, contactMessage)
                    },
                    label = { Text(if (isAr) "بريدك الإلكتروني" else "Your Email") },
                    placeholder = { Text("name@company.com", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = CyanAccent
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_email_input"),
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
                    value = contactSubject,
                    onValueChange = {
                        contactSubject = it
                        viewModel.updateContactForm(contactEmail, contactSubject, contactMessage)
                    },
                    label = { Text(if (isAr) "الموضوع" else "Subject") },
                    placeholder = {
                        Text(
                            if (isAr) "استفسار عن التكامل أو الدعم" else "Integration or API inquiry",
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_subject_input"),
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
                    value = contactMessage,
                    onValueChange = {
                        contactMessage = it
                        viewModel.updateContactForm(contactEmail, contactSubject, contactMessage)
                    },
                    label = { Text(if (isAr) "نص الرسالة" else "Message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_message_input"),
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
                        if (isValidEmail && contactMessage.isNotBlank()) {
                            viewModel.updateContactForm(contactEmail, contactSubject, contactMessage)
                            viewModel.submitContactMessage()
                            val mailto = "mailto:contact@sawtflow.ai?subject=${
                                Uri.encode(contactSubject.ifBlank { "SawtFlow Contact" })
                            }&body=${Uri.encode("From: $contactEmail\n\n$contactMessage")}"
                            try {
                                uriHandler.openUri(mailto)
                            } catch (_: Exception) {
                                // Logged in app even if no mail app is configured on emulator
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("submit_contact_us_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0C4E78),
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    enabled = isValidEmail && contactMessage.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "إرسال عبر البريد الإلكتروني" else "Send Email",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = state.isContactSubmitted) {
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
                                    "شكراً لتواصلك! تم إرسال رسالتك وتوثيقها بنجاح."
                                else
                                    "Thanks for reaching out! Your message has been sent and logged.",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Footer Section with Social Media Links
        Card(
            colors = CardDefaults.cardColors(containerColor = NavyCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("overview_footer_section")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sawtflow_brand_logo),
                        contentDescription = "Sound Flow Logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = "Sound Flow AI",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isAr) "تابعنا وتواصل معنا عبر المنصات الاجتماعية" else "Connect with Sound Flow on social media",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )

                val socialItems = listOf(
                    Triple("GitHub", Icons.Default.Code, "https://github.com/mrm6676/SawtFlow"),
                    Triple("X", Icons.Default.AlternateEmail, "https://x.com"),
                    Triple("LinkedIn", Icons.Default.Work, "https://www.linkedin.com"),
                    Triple("Website", Icons.Default.Public, "https://sawtflow.vercel.app")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    socialItems.forEach { (label, icon, url) ->
                        OutlinedButton(
                            onClick = {
                                try {
                                    uriHandler.openUri(url)
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        if (isAr) "تعذر فتح الرابط: $url" else "Unable to open $url",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("social_link_${label.lowercase()}")
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                HorizontalDivider(color = NavyBorder)

                Text(
                    text = if (isAr)
                        "© 2026 Sound Flow AI · ذكاء الصوت العربي والإنجليزي لأتمتة الأعمال"
                    else
                        "© 2026 Sound Flow AI · Arabic & English Voice Intelligence",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = desc,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
