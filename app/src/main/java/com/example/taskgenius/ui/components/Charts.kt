import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import com.example.taskgenius.ui.components.CircularTimer
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DonutChart(
    elapsedTime: Float,
    inProgressTime: Float,
    remainingTime: Float,
    centerText: String,
    createdAtText: String,
    dueAtText: String
) {
    val total = elapsedTime + inProgressTime + remainingTime
    val animatedElapsedTime = remember { Animatable(0f) }
    val animatedInProgressTime = remember { Animatable(0f) }
    val animatedRemainingTime = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatedElapsedTime.animateTo(elapsedTime, animationSpec = tween(1000))
        animatedInProgressTime.animateTo(inProgressTime, animationSpec = tween(1000, delayMillis = 300))
        animatedRemainingTime.animateTo(remainingTime, animationSpec = tween(1000, delayMillis = 600))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem("Elapsed", Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(12.dp))
            LegendItem("In Progress", Color(0xFF2196F3))
            Spacer(modifier = Modifier.width(12.dp))
            LegendItem("Remaining", Color(0xFFF44336))
        }


        Text(
            text = "Created At: $createdAtText",
            fontSize = 16.sp,
            color = Color(0xFF4CAF50),
            style = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        )
        Text(
            text = "Due At: $dueAtText",
            fontSize = 16.sp,
            color = Color(0xFFF44336),
            style = TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(350.dp)) {  // Increased width
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 45f
                    val radius = size.minDimension / 2
                    val center = Offset(radius, radius)
                    val rect = androidx.compose.ui.geometry.Rect(
                        center - Offset(radius, radius),
                        center + Offset(radius, radius)
                    )

                    var startAngle = -90f
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = startAngle,
                        sweepAngle = (animatedElapsedTime.value / total) * 360f,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = Size(rect.width, rect.height),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += (animatedElapsedTime.value / total) * 360f
                    drawArc(
                        color = Color(0xFF2196F3),
                        startAngle = startAngle,
                        sweepAngle = (animatedInProgressTime.value / total) * 360f,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = Size(rect.width, rect.height),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += (animatedInProgressTime.value / total) * 360f
                    drawArc(
                        color = Color(0xFFF44336),
                        startAngle = startAngle,
                        sweepAngle = (animatedRemainingTime.value / total) * 360f,
                        useCenter = false,
                        topLeft = rect.topLeft,
                        size = Size(rect.width, rect.height),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = centerText,
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color.Black)
                )
            }
        }
    }
}
@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 14.sp)
    }
}

// Here i used the canvas to create the custom donut chart to visual the timings.
@Composable
fun Charts(task: TaskEntity) {
    var bool = false
    val istZone = ZoneId.of("Asia/Kolkata")
    val currentTime = ZonedDateTime.now(istZone).toInstant().toEpochMilli()
    val createdAt = task.createdAt ?: 0
    val dueAt = task.dueAt ?: 0
    val createdAtIST = Instant.ofEpochMilli(createdAt).atZone(istZone)
    val dueAtIST = Instant.ofEpochMilli(dueAt).atZone(istZone)
    val totalDuration = dueAtIST.toInstant().toEpochMilli() - createdAtIST.toInstant().toEpochMilli()
    var elapsedTime = (currentTime - createdAtIST.toInstant().toEpochMilli()).coerceAtLeast(0).toFloat()
    var remainingTime = (dueAtIST.toInstant().toEpochMilli() - currentTime).coerceAtLeast(0).toFloat()
    var inProgressTime = (totalDuration - elapsedTime - remainingTime).coerceAtLeast(0.toFloat())

    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    val createdAtText = createdAtIST.format(formatter)
    val dueAtText = dueAtIST.format(formatter)
    val centerText: String

    when {
        task.status == TaskStatus.COMPLETED -> {
            elapsedTime = totalDuration.toFloat()
            remainingTime = 0f
            inProgressTime = 0f
            centerText = "Completed"
        }
        currentTime < createdAtIST.toInstant().toEpochMilli() -> {
            elapsedTime = 0f
            remainingTime = totalDuration.toFloat()
            inProgressTime = 0f
            centerText = "Not Started"
            bool = true
        }
        currentTime > dueAtIST.toInstant().toEpochMilli() -> {
            elapsedTime = totalDuration.toFloat()
            remainingTime = 0f
            inProgressTime = 0f
            centerText = "Expired"
            bool = true
        }
        else -> {
            centerText = "${(elapsedTime / totalDuration * 100).toInt()}% Elapsed"
        }
    }

    Column(
        modifier = Modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DonutChart(elapsedTime, inProgressTime, remainingTime, centerText, createdAtText, dueAtText)
        if (!bool) {
            Spacer(modifier = Modifier.height(20.dp))
            CircularTimer(task.createdAt ?: 0, task.dueAt ?: 0)
        }
    }
}