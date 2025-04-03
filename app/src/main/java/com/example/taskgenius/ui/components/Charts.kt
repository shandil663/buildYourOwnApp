import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val elapsedAngle = (elapsedTime / total) * 360f
    val inProgressAngle = (inProgressTime / total) * 360f
    val remainingAngle = (remainingTime / total) * 360f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), // Top & Bottom Margin
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Legend
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            LegendItem("Elapsed", Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(8.dp))
            LegendItem("In Progress", Color(0xFF2196F3))
            Spacer(modifier = Modifier.width(8.dp))
            LegendItem("Remaining", Color(0xFFF44336))
        }

        // Timestamps (12-Hour Format)
        Text(text = "Created At: $createdAtText", fontSize = 14.sp)
        Text(text = "Due At: $dueAtText", fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))

        Box(modifier = Modifier.size(250.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 40f
                val radius = size.minDimension / 2
                val center = Offset(radius, radius)
                val rect = androidx.compose.ui.geometry.Rect(
                    center - Offset(radius, radius),
                    center + Offset(radius, radius)
                )

                var startAngle = -90f

                // Draw Elapsed Time (Green)
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = startAngle,
                    sweepAngle = elapsedAngle,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = Size(rect.width, rect.height),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += elapsedAngle

                // Draw In Progress Time (Blue)
                drawArc(
                    color = Color(0xFF2196F3),
                    startAngle = startAngle,
                    sweepAngle = inProgressAngle,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = Size(rect.width, rect.height),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += inProgressAngle

                // Draw Remaining Time (Red)
                drawArc(
                    color = Color(0xFFF44336),
                    startAngle = startAngle,
                    sweepAngle = remainingAngle,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = Size(rect.width, rect.height),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Center Label
            Text(
                text = centerText,
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(fontSize = 16.sp, color = Color.Black)
            )
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

@Composable
fun Charts(task: TaskEntity) {
    var bool=false
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

    // Format timestamps to 12-hour format with AM/PM
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

        currentTime < createdAtIST.toInstant().toEpochMilli() -> {  // Future Task
            elapsedTime = 0f
            remainingTime = totalDuration.toFloat()
            inProgressTime = 0f
            centerText = "Not Started"
            bool=true
        }

        currentTime > dueAtIST.toInstant().toEpochMilli() -> { // Expired Task
            elapsedTime = totalDuration.toFloat()
            remainingTime = 0f
            inProgressTime = 0f
            centerText = "Expired"

             bool=true
        }

        else -> { // Ongoing Task
            centerText = "${(elapsedTime / totalDuration * 100).toInt()}% Elapsed"
        }
    }


    Column(modifier = Modifier.padding(20.dp)) {
        DonutChart(elapsedTime, inProgressTime, remainingTime, centerText, createdAtText, dueAtText)
if(!bool)
    CircularTimer(task.createdAt ?: 0, task.dueAt ?: 0)


    }

}
