package com.example.taskgenius.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun CircularTimer(taskStart: Long, taskEnd: Long) {
    val istZone = ZoneId.of("Asia/Kolkata")
    val totalDuration = taskEnd - taskStart
    var remainingTime by remember { mutableStateOf(taskEnd - ZonedDateTime.now(istZone).toInstant().toEpochMilli()) }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            val now = ZonedDateTime.now(istZone).toInstant().toEpochMilli()
            remainingTime = (taskEnd - now).coerceAtLeast(0)

            val progress = (1f - remainingTime.toFloat() / totalDuration)
            animatedProgress.animateTo(progress, animationSpec = tween(1000, easing = LinearEasing))

            delay(1000L)
        }
    }

    val timeLeft = remember(remainingTime) {
        val hours = (remainingTime / (1000 * 60 * 60)) % 24
        val minutes = (remainingTime / (1000 * 60)) % 60
        val seconds = (remainingTime / 1000) % 60
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20f
            val radius = size.minDimension / 2 - strokeWidth
            val center = Offset(size.width / 2, size.height / 2)

            drawArc(
                color = Color.LightGray,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = timeLeft,
            fontSize = 24.sp,
            style = TextStyle(color = Color.Black)
        )
    }
}
