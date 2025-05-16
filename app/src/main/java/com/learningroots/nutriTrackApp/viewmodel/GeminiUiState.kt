package com.learningroots.nutriTrackApp.viewmodel

sealed interface GeminiUiState {
    object Initial : GeminiUiState
    object Loading : GeminiUiState
    data class Success(val outputText: String) : GeminiUiState
    data class Error(val errorMessage: String) : GeminiUiState
} 