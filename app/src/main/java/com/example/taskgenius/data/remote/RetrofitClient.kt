package com.example.taskgenius.data.remote

import com.example.taskgenius.data.remote.model.ContentItem
import com.example.taskgenius.data.remote.model.ContentPart
import com.example.taskgenius.data.remote.model.GeminiRequest
import com.example.taskgenius.data.remote.model.GeminiResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val API_KEY = "AIzaSyC-UarC5-DSa_3hlEhz6l1mjNJDp4SnaMA"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val geminiService: GeminiService = retrofit.create(GeminiService::class.java)

    suspend fun generateTask(input: String): GeminiResponse {
        return geminiService.generateTaskDetails(API_KEY, GeminiRequest(listOf(ContentItem(listOf(ContentPart(input))))))
    }
}
