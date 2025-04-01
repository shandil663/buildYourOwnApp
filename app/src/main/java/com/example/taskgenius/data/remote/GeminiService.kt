package com.example.taskgenius.data.remote

import com.example.taskgenius.data.remote.model.GeminiRequest
import com.example.taskgenius.data.remote.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiService {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateTaskDetails(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
