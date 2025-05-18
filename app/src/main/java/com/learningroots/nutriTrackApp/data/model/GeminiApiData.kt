package com.learningroots.nutriTrackApp.data.model

// Request Structures
data class GeminiRequest(
    val contents: List<ContentEntry>,
    val generationConfig: GenerationConfig? = null
)

data class ContentEntry(
    val parts: List<TextPart>
)

data class TextPart(
    val text: String
)

// Response Structures
data class GeminiResponse(
    val candidates: List<Candidate>?,
    val error: GeminiErrorBody? // Changed from GeminiError to avoid conflict if GeminiError is a UI state
)

data class Candidate(
    val content: ContentEntry // Reusing ContentEntry as structure is similar
)

// Changed from GeminiError to avoid conflict with potential UI error states or classes
data class GeminiErrorBody(
    val code: Int,
    val message: String,
    val status: String
) 