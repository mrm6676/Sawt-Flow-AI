package com.example.sawtflow.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.sawtflow.data.model.AuditLogEntry
import com.example.sawtflow.ui.theme.AmberAccent
import com.example.sawtflow.ui.theme.CyanAccent
import com.example.sawtflow.ui.theme.GreenSuccess
import com.example.sawtflow.ui.theme.NavyCard
import com.example.sawtflow.ui.theme.NavyLight
import com.example.sawtflow.ui.theme.NavyPrimary
import com.example.sawtflow.ui.theme.NavySurface
import com.example.sawtflow.ui.viewmodel.SawtFlowViewModel

@Composable
fun AuditScreen(
    viewModel: SawtFlowViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.auditLogs.collectAsState()
    val isAr = language == AppLanguage.AR

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyPrimary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isAr) "سجل التدقيق والامتثال / التحكم البشري" else "AUDIT TRAIL & GOVERNANCE / HUMAN-IN-THE-LOOP",
                color = CyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (isAr) "سجل العمليات والتحقق المعتمد" else "Verified Action Audit Log",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (isAr)
                    "كل بحث، تصحيح كلام، أو اعتماد إجراء يُسجّل بسياقه الكامل وفق سياسة عدم الإلغاء."
                else
                    "Every lookup, speech correction, and human response approval is immutably recorded.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp
            )
        }

        // Info card explaining append-only policy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NavySurface)
                .border(1.dp, AmberAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AmberAccent)
                )
                Text(
                    text = if (isAr)
                        "سياسة تدقيق صارمة: لا حذف أو تعديل للسجلات؛ الإجراءات المصادق عليها فقط تُنفّذ."
                    else
                        "Strict audit policy: Records are append-only. Only approved human-verified actions execute.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }

        // Logs list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("audit_logs_list")
        ) {
            items(logs) { entry ->
                AuditLogCard(entry = entry, isAr = isAr)
            }
        }
    }
}

@Composable
fun AuditLogCard(
    entry: AuditLogEntry,
    isAr: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    entry.eventType.contains("approved") -> GreenSuccess
                                    entry.eventType.contains("failed") -> Color(0xFFBA1A1A)
                                    entry.eventType.contains("recorded") -> AmberAccent.copy(alpha = 0.2f)
                                    else -> CyanAccent.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = entry.eventType,
                            color = when {
                                entry.eventType.contains("approved") -> Color.White
                                entry.eventType.contains("failed") -> Color.White
                                entry.eventType.contains("recorded") -> AmberAccent
                                else -> CyanAccent
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    entry.orderId?.let { orderId ->
                        Text(
                            text = "[$orderId]",
                            color = AmberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = entry.timestampFormatted,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }

            Text(
                text = entry.details,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${if (isAr) "الفاعل" else "Actor"}: ${entry.actor}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
                Text(
                    text = "ID: ${entry.id}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp
                )
            }
        }
    }
}
