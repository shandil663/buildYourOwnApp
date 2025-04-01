package com.example.taskgenius.utils

import android.util.Log
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TaskAIHelper {

    suspend fun generateTaskFromInput(input: String): TaskEntity {
        return withContext(Dispatchers.IO) {
            try {

                val taskDetails = GeminiHelper.generateTaskFromInput(input)
                Log.d("Hello ",taskDetails.toString())

                when {
                    taskDetails.description?.contains("meeting", true) == true -> taskDetails.copy(
                        title = "Meeting Reminder",
                        category = "Work",
                        createdAt = System.currentTimeMillis(),
                        dueAt = System.currentTimeMillis() + 86400000,
                        status = TaskStatus.PENDING
                    )
                    taskDetails.description?.contains("buy", true) == true -> taskDetails.copy(
                        title = "Shopping List",
                        category = "Personal",
                        createdAt = System.currentTimeMillis(),
                        dueAt = null,
                        status = TaskStatus.PENDING
                    )
                    else -> taskDetails.copy(
                        title = "Custom Task",
                        category = "General",
                        createdAt = System.currentTimeMillis(),
                        dueAt = null,
                        status = TaskStatus.PENDING
                    )
                }
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
}
