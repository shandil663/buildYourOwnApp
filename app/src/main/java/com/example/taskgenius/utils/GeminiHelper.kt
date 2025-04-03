package com.example.taskgenius.utils

import android.util.Log
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import com.example.taskgenius.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId


object GeminiHelper {
    private val gson = Gson()
    private val isoDateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }

    suspend fun generateTaskFromInput(input: String): TaskEntity {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.generateTask(
                    input + "\n\n\n\"Extract and categorize the input into the following structured format without adding any extra details. Your response should only contain the extracted fields in JSON format:\n\n{\n" +
                            "  \"title\": \"Your refined title up to three words only\",\n" +
                            "  \"description\": \"if user included the word remainder or notify me or remind me then here in the description only put YES nothing else \",\n" +
                            "  \"createdAt\": \"ISO 8601 format use UTC+05:30 and  extract the timing from the input where input contains startAt: timing also convert he time do not return +05:30 and we are in 2025\",\n" +
                            "  \"dueAt\": \"ISO 8601 format use UTC+05:30 and  extract the timing from the input  where input contains endAt or lasts: timing also convert he time do not return +05:30 and we are in 2025 and \",\n" +
                            "  \"category\": \"One of [Professional, Personal, Household, Social,Wellness, General]\",\n" +
                            "  \"status\": \"One of [PENDING, COMPLETED, CANCELLED, ERROR]\"\n" +
                            "}\n\nEnsure createdAt and dueAt are in ISO 8601 format. You must use UTC+05:30 while the putting the value for createdAt and dueAt"
                )

                var taskDetails =
                    response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "No details provided"
                Log.d("Gemini", "AI Raw Response: $taskDetails")

                taskDetails = cleanJson(taskDetails)
                Log.d("Gemini", "AI Cleaned JSON: $taskDetails")

                val taskMap: MutableMap<String, Any?> =
                    gson.fromJson(taskDetails, MutableMap::class.java) as MutableMap<String, Any?>
                val createdAtIST = convertToIST(taskMap["createdAt"].toString())
                val dueAtIST = convertToIST(taskMap["dueAt"].toString())
                Log.d("1", createdAtIST.toString())
                Log.d("2", dueAtIST.toString())
                taskMap["createdAt"] = createdAtIST?.let { parseIsoToMillis(it.toString()) }
                    ?: System.currentTimeMillis()
                taskMap["dueAt"] = dueAtIST.let { parseIsoToMillis(it.toString()) }
                Log.d("1", taskMap["createdAt"].toString())
                Log.d("2", taskMap["dueAt"].toString())
                return@withContext gson.fromJson(gson.toJson(taskMap), TaskEntity::class.java)
            } catch (e: Exception) {
                Log.e("Gemini", "Error generating task", e)
                TaskEntity(
                    title = "Error",
                    description = "Failed to generate task.",
                    category = "Error",
                    createdAt = System.currentTimeMillis(),
                    dueAt = null,
                    status = TaskStatus.ERROR
                )
            }
        }
    }

    private fun cleanJson(response: String): String {
        return response
            .trim()
            .removeSurrounding("```json", "```")
            .removeSurrounding("```")
    }

    private fun parseIsoToMillis(dateString: String): Long? {
        return try {
            isoDateFormat.parse(dateString)?.time
        } catch (e: Exception) {
            Log.e("Gemini", "Error parsing date: $dateString", e)
            null
        }
    }
}

fun convertToIST(isoString: String): String {
    val utcTime = ZonedDateTime.parse(isoString)
    val istTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
    return istTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}

