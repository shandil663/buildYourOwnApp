package com.example.taskgenius.data.remote.model

data class GeminiResponse(
    val candidates: List<Candidate>?
) {
    data class Candidate(
        val content: Content?
    )

    data class Content(
        val parts: List<Part>?
    )

    data class Part(
        val text: String?
    )
}
