
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

object GeminiHelper {
    private val gson = Gson()
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun generateTaskFromInput(input: String): TaskEntity {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.generateTask(
                    input + "\n\n\n\"Extract and categorize the input into the following structured format without adding any extra details. Your response should only contain the extracted fields in JSON format:\n\n{\n" +
                            "  \"title\": \"Your refined title\",\n" +
                            "  \"description\": \"A short refined description (or null if unnecessary)\",\n" +
                            "  \"createdAt\": \"ISO 8601 format (e.g., 2024-01-11T12:09:10Z)\",\n" +
                            "  \"dueAt\": \"ISO 8601 format or null\",\n" +
                            "  \"category\": \"One of [Work, Personal, Health, Finance, Study, General]\",\n" +
                            "  \"status\": \"One of [PENDING, COMPLETED, CANCELLED, ERROR]\"\n" +
                            "}\n\nEnsure createdAt and dueAt are in ISO 8601 format."
                )

                var taskDetails = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "No details provided"
                Log.d("Gemini", "AI Raw Response: $taskDetails")

                taskDetails = cleanJson(taskDetails)
                Log.d("Gemini", "AI Cleaned JSON: $taskDetails")

                val taskMap: MutableMap<String, Any?> = gson.fromJson(taskDetails, MutableMap::class.java) as MutableMap<String, Any?>

                taskMap["createdAt"] = taskMap["createdAt"]?.let { parseIsoToMillis(it.toString()) } ?: System.currentTimeMillis()
                taskMap["dueAt"] = taskMap["dueAt"]?.let { parseIsoToMillis(it.toString()) }

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
