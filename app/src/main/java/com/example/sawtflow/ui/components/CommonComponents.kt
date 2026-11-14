package com.example.sawtflow.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.example.sawtflow.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.ui.theme.AmberAccent
import com.example.sawtflow.ui.theme.CyanAccent
import com.example.sawtflow.ui.theme.NavyCard
import com.example.sawtflow.ui.theme.NavyPrimary
import com.example.sawtflow.ui.theme.TajawalFontFamily

@Composable
fun SawtFlowHeader(
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sound Flow Brand Logo
            Image(
                painter = painterResource(id = R.drawable.ic_sawtflow_brand_logo),
                contentDescription = "Sound Flow Brand Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )

            Row {
                Text(
                    text = "Sound ",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Flow",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // Language toggle
        OutlinedButton(
            onClick = onToggleLanguage,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(CyanAccent, AmberAccent))
            ),
            modifier = Modifier.height(36.dp)
        ) {
            Text(
                text = if (currentLanguage == AppLanguage.EN) "عربي" else "EN",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun AudioWaveformBanner(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, CyanAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2

            // Background orbit arc
            drawArc(
                color = CyanAccent.copy(alpha = 0.15f),
                startAngle = -20f,
                sweepAngle = 200f,
                useCenter = false,
                topLeft = Offset(width * 0.1f, 10f),
                size = androidx.compose.ui.geometry.Size(width * 0.8f, height * 0.9f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Animated wave path
            val path = Path()
            path.moveTo(0f, centerY)
            val points = 60
            for (i in 0..points) {
                val x = (width / points) * i
                val factor = if (isRecording) 1.5f else 0.8f
                val y = centerY + kotlin.math.sin(i * 0.25f + phase) * (18f * factor) *
                        kotlin.math.sin(i.toFloat() / points * 3.14f)
                path.lineTo(x, y.toFloat())
            }

            drawPath(
                path = path,
                color = CyanAccent,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Verified node
            val nodeX = width * 0.82f
            val nodeY = centerY - 14f
            drawCircle(
                color = AmberAccent.copy(alpha = 0.3f),
                radius = 14.dp.toPx(),
                center = Offset(nodeX, nodeY)
            )
            drawCircle(
                color = AmberAccent,
                radius = 6.dp.toPx(),
                center = Offset(nodeX, nodeY)
            )
        }

        // Live badge overlay
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.Red else CyanAccent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRecording) "LIVE RECORDING" else "AUDIO INTELLIGENCE STREAM",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = TajawalFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StepBadge(
    indexText: String,
    statusText: String,
    statusColor: Color = CyanAccent
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = indexText,
            color = Color.White,
            fontFamily = TajawalFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = statusText,
                color = Color.White,
                fontFamily = TajawalFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
