package com.example.taskgenius.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.commandiron.wheel_picker_compose.WheelTimePicker
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.commandiron.wheel_picker_compose.core.TimeFormat
import com.example.taskgenius.helper.NotificationWorker
import com.example.taskgenius.ui.components.TaskNotificationReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.min

@Composable
fun NewTaskScreen(
    onTaskAdded: (TaskEntity) -> Unit,
    onBack: () -> Unit
) {

    var notifyMe by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var taskTitle by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(LocalTime.now().plusMinutes(1)) }
    Log.d("timeformat1", LocalTime.now().toString())
    var endTime by remember { mutableStateOf(startTime.plusMinutes(5)) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val duration = remember(startTime, endTime) {
        ChronoUnit.MINUTES.between(startTime, endTime).minutes
    }
    val formattedDuration by remember(duration) { mutableStateOf(formatDuration(duration)) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("General") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, top = 50.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("New Task", style = MaterialTheme.typography.headlineLarge)

        OutlinedTextField(
            value = taskTitle,
            onValueChange = { taskTitle = it },
            label = { Text("Task Title") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor =Color(0xFFE18181),
                unfocusedBorderColor =Color(0xFFE18181).copy(alpha = 0.5f),
                cursorColor = Color(0xFFE18181)
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Start Time", style = MaterialTheme.typography.bodyLarge)

                key(startTime) {
                    WheelTimePicker(
                        startTime = startTime,
                        timeFormat = TimeFormat.AM_PM,
                        textColor =Color(0xFFE18181),
                        onSnappedTime = { newTime ->
                            validateStartTime(context, newTime) { validTime ->
                                startTime = validTime
                                if (validTime >= endTime) {
                                    endTime = validTime.plusMinutes(30)
                                }
                            }
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("End Time", style = MaterialTheme.typography.bodyLarge)

                key(endTime) {
                    WheelTimePicker(
                        startTime = endTime,
                        timeFormat = TimeFormat.AM_PM,
                        textColor = Color(0xFFE18181),
                        onSnappedTime = { newTime ->
                            validateEndTime(context, startTime, newTime) { validTime ->
                                endTime = validTime
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Duration", style = MaterialTheme.typography.bodyLarge)
            Text(text = formattedDuration)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(30 to "30 min", 60 to "1 hour", 120 to "2 hours").forEach { (minutes, label) ->
                Button(onClick = {
                    val newStartTime = LocalTime.now().plusMinutes(1)
                    startTime = newStartTime
                    endTime = newStartTime.plusMinutes(minutes.toLong())
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE18181)
                    )

                ) {
                    Text(label)
                }
            }
            Button(onClick = { showCustomDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE18181)
                )) {
                Text("Custom")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { showCategoryDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE18181)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Category: $selectedCategory")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    tint = Color(0xFFE18181)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Notify Me", style = MaterialTheme.typography.bodyLarge)
            }

            Switch(
                checked = notifyMe,
                onCheckedChange = { notifyMe = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color(0xFFE18181),
                    uncheckedTrackColor = Color.Gray
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = @androidx.annotation.RequiresPermission(android.Manifest.permission.SCHEDULE_EXACT_ALARM) {
                if (startTime < LocalTime.now()) {
                    Toast.makeText(
                        context,
                        "Invalid Start Time! Cannot be before the current time.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                if (endTime <= startTime) {
                    Toast.makeText(
                        context,
                        "Invalid End Time! Must be after Start Time.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                if (taskTitle.isNotBlank()) {
                    val newTask = TaskEntity(
                        title = taskTitle,
                        description = "User created task",
                        category = selectedCategory,
                        createdAt = LocalDateTime.of(LocalDate.now(), startTime)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        dueAt = LocalDateTime.of(LocalDate.now(), endTime)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        status = TaskStatus.PENDING
                    )
                    if (notifyMe) {
                        val minuteValue = ChronoUnit.MINUTES.between(LocalTime.now(), startTime).minutes
                        scheduleNotificationWithWorkManager(context, taskTitle, minuteValue.inWholeMinutes)
                    }
                    onTaskAdded(newTask)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE18181) // Custom background color
            )
        ) {
            Text("Save Task")

        }


        if (showCustomDialog) {
            CustomDurationDialog(
                initialDuration = duration.inWholeMinutes.toInt(),
                onDurationSelected = { hours, minutes ->
                    val newStartTime = LocalTime.now().plusMinutes(1)
                    startTime = newStartTime
                    endTime = newStartTime.plusMinutes((hours * 60 + minutes).toLong())
                    showCustomDialog = false
                },
                onDismiss = { showCustomDialog = false }
            )
        }
        if (showCategoryDialog) {
            CategorySelectionDialog(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    showCategoryDialog = false
                },
                onDismiss = { showCategoryDialog = false }
            )
        }
    }



}


fun validateStartTime(context: Context, selectedTime: LocalTime, onValidated: (LocalTime) -> Unit) {
    val now = LocalTime.now()
    if (selectedTime < now) {
        Toast.makeText(
            context,
            "Invalid Start Time! Cannot be before the current time.",
            Toast.LENGTH_SHORT
        ).show()
        onValidated(now.plusMinutes(1))
    } else {
        onValidated(selectedTime)
    }
}

fun validateEndTime(
    context: Context,
    startTime: LocalTime,
    selectedTime: LocalTime,
    onValidated: (LocalTime) -> Unit
) {
    if (selectedTime <= startTime) {
        Toast.makeText(context, "Invalid End Time! Must be after Start Time.", Toast.LENGTH_SHORT)
            .show()
        onValidated(startTime.plusMinutes(5))
    } else {
        onValidated(selectedTime)
    }
}


@Composable
fun CustomDurationDialog(
    initialDuration: Int,
    onDurationSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val hours = remember { mutableStateOf(initialDuration / 60) }
    val minutes = remember { mutableStateOf(initialDuration % 60) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Custom Duration", style = MaterialTheme.typography.headlineSmall)
                WheelTimePicker(
                    startTime = LocalTime.of(hours.value, minutes.value),
                    textColor = Color(0xFFE18181),
                    onSnappedTime = {
                        hours.value = it.hour
                        minutes.value = it.minute
                    }
                )
                Button(
                    onClick = {
                        val selectedDuration = (hours.value * 60 + minutes.value).toLong()
                        if (selectedDuration > 0) {
                            onDurationSelected(hours.value, minutes.value)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE18181)
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
}

fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = duration.inWholeMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "$hours hr $minutes min"
        hours > 0 -> "$hours hr"
        minutes > 0 -> "$minutes min"
        else -> "0 min"
    }
}


fun LocalTime.formatTime(): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return this.format(formatter)
}



fun scheduleNotificationWithWorkManager(context: Context, title: String, delayInMinutes: Long) {
    val data = Data.Builder()
        .putString("TASK_TITLE", title)
        .putString("TASK_TIME", "Scheduled for $delayInMinutes minutes from now")
        .build()

    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
        .setInputData(data)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}

@Composable
fun CategorySelectionDialog(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf("Professional", "Personal", "Household", "Social", "Wellness", "General")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Category", style = MaterialTheme.typography.headlineSmall)

                categories.forEach { category ->
                    Button(
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE18181)
                        )
                    ) {
                        Text(category)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE18181) // Custom background color
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}