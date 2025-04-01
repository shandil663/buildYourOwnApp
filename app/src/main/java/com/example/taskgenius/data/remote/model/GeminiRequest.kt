package com.example.taskgenius.data.remote.model

data class GeminiRequest(
    val contents: List<ContentItem>
)

data class ContentItem(
    val parts: List<ContentPart>
)

data class ContentPart(
    val text: String
)
