package com.example.taskgenius.utils

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import com.example.taskgenius.helper.NotificationWorker
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes


object TaskAIHelper {

    suspend fun generateTaskFromInput(input: String, context: Context): TaskEntity {
        return withContext(Dispatchers.IO) {
            try {

                val taskDetails = GeminiHelper.generateTaskFromInput(input)
                Log.d("Hello ", taskDetails.toString())
//                val (date1, time1) = extractDateTime(taskDetails.createdAt.toString())
//                val (date2, time2) = extractDateTime(taskDetails.dueAt.toString())
//
                val startTime = unixToLocalTime(taskDetails.createdAt)
                val endTime = unixToLocalTime(taskDetails.dueAt?.toLong() ?: 0)
                if (taskDetails.description.equals("YES")) {
                    val startTime = unixToLocalTime(taskDetails.createdAt)
                    val currentTime = LocalTime.now()

                    val isPM = currentTime.hour >= 12
                    val adjustedStartTime = if (isPM) {
                        startTime.plusHours(12) // Ensure it's in PM range
                    } else {
                        startTime.minusHours(12) // Ensure it's in AM range
                    }

                    Log.d("hello",adjustedStartTime.toString())

                    val minuteValue =
                        ChronoUnit.MINUTES.between(currentTime, adjustedStartTime).minutes
                    if (minuteValue.inWholeMinutes > 0) {
                        scheduleNotificationWithWorkManager(
                            context,
                            taskDetails.title,
                            minuteValue.inWholeMinutes
                        )
                    }
                }

                TaskEntity(
                    title = taskDetails.title,
                    category = taskDetails.category,
                    createdAt = LocalDateTime.of(LocalDate.now(), startTime)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    dueAt = LocalDateTime.of(LocalDate.now(), endTime)
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    status = TaskStatus.PENDING,
                    id = taskDetails.id,
                    description = taskDetails.description
                )

            } catch (e: Exception) {
                TaskEntity(
                    title = "Error",
                    description = "There was an error generating the task: ${e.message}",
                    category = "General",
                    createdAt = System.currentTimeMillis(),
                    dueAt = null,
                    status = TaskStatus.ERROR
                )
            }
        }
    }


    fun extractDateTime(isoString: String): Pair<String, String> {
        val zonedDateTime = ZonedDateTime.parse(isoString)

        val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM, yyyy", Locale.ENGLISH)
        val formattedDate = zonedDateTime.format(dateFormatter)

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val formattedTime = zonedDateTime.format(timeFormatter)

        return Pair(formattedDate, formattedTime)
    }

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

fun unixToLocalTime(unixTimestamp: Long): LocalTime {
    val instant = Instant.ofEpochMilli(unixTimestamp)
    val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
    return localTime
}

fun formatLocalTime(localTime: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm:ss")
    return localTime.format(formatter)
}
