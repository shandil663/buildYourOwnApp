package com.example.taskgenius.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.example.taskgenius.viewmodel.TaskViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

@Composable
fun CategoryTasksScreen(
    category: String,
    onTaskClick: (TaskEntity) -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.getTasksByCategory(category).collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks found in this category",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tasks) { task ->
                    TaskGridItem(
                        task, onTaskClick,
                        viewModel
                    )
                }
            }
        }
    }
}


@Composable
fun TaskGridItem(task: TaskEntity, onTaskClick: (TaskEntity) -> Unit, viewModel: TaskViewModel) {
    val istZone = ZoneId.of("Asia/Kolkata")
    val currentTime = ZonedDateTime.now(istZone).toInstant().toEpochMilli()

    val createdAtIST = Instant.ofEpochMilli(task.createdAt ?: 0).atZone(istZone)
    val dueAtIST = Instant.ofEpochMilli(task.dueAt ?: 0).atZone(istZone)

    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    val createdAtText = createdAtIST.format(formatter)
    val dueAtText = dueAtIST.format(formatter)

    var isCompleted by remember { mutableStateOf(task.status == TaskStatus.COMPLETED) }
    val bounceAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        bounceAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = {
                    if (it < 0.5f) {
                        (4 * it * it * it).coerceAtMost(1f)
                    } else {
                        (1 - (-2 * it + 2).pow(2) / 2).coerceAtMost(1f)
                    }
                }
            )
        )
    }
    val categoryGradients = mapOf(
        "Professional" to arrayOf(0.0f to Color(0xFFBBDEFB), 0.8f to Color(0xFF4F8ABE), 1.0f to Color(0xFF438ED0)),
        "Personal" to arrayOf(0.0f to Color(0xFFE1BEE7), 0.8f to Color(0xFF7B1FA2), 1.0f to Color(0xFF7B1FA2)),
        "Social" to arrayOf(0.0f to Color(0xFFFFCCBC), 0.8f to Color(0xFFD84315), 1.0f to Color(0xFFD84315)),
        "Wellness" to arrayOf(0.0f to Color(0xFFC8E6C9), 0.8f to Color(0xFF388E3C), 1.0f to Color(0xFF388E3C)),
        "Household" to arrayOf(0.0f to Color(0xFFFFF9C4), 0.8f to Color(0xFFFBC02D), 1.0f to Color(0xFFFBC02D)),
        "General" to arrayOf(0.0f to Color(0xFFCFD8DC), 0.8f to Color(0xFF455A64), 1.0f to Color(0xFF455A64))
    )

    val backgroundGradient = Brush.verticalGradient(
        colorStops = categoryGradients[task.category] ?: arrayOf(
            0.0f to Color(0xFFCFD8DC), 0.8f to Color(0xFF455A64), 1.0f to Color(0xFF455A64)
        )
    )

    val borderStroke = Brush.linearGradient(
        colors = listOf(Color(0xFFF5B84C), Color(0xFF1C170E))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .graphicsLayer(scaleX = bounceAnim.value, scaleY = bounceAnim.value)
            .then(if (isCompleted) Modifier else Modifier.clickable { onTaskClick(task) }),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(borderStroke)
                .padding(1.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(backgroundGradient)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (task.description == "YES") {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminder",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    Text(
                        text = "$createdAtText - $dueAtText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompleted) "Completed" else "Mark as Complete",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            if (!isCompleted) {
                                viewModel.updateTaskStatus(task.id, TaskStatus.COMPLETED)
                                isCompleted = true
                            }
                        },
                        enabled = !isCompleted
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Complete Task",
                            tint = if (isCompleted) Color.Green else Color.White
                        )
                    }
                }
            }
        }
    }
}
