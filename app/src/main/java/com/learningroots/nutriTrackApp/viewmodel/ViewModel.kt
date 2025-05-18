package com.learningroots.nutriTrackApp.viewmodel

import com.learningroots.nutriTrackApp.data.repository.Repository
import com.learningroots.nutriTrackApp.data.entity.*
import com.learningroots.nutriTrackApp.data.model.FruitData
import com.learningroots.nutriTrackApp.data.model.GeminiRequest
import com.learningroots.nutriTrackApp.data.model.ContentEntry
import com.learningroots.nutriTrackApp.data.model.TextPart
import com.learningroots.nutriTrackApp.data.network.GeminiApiService
import com.learningroots.nutriTrackApp.data.network.FruityViceApiService
import com.learningroots.nutriTrackApp.BuildConfig // Import BuildConfig
import com.learningroots.nutriTrackApp.data.model.GenerationConfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log
import com.learningroots.nutriTrackApp.screens.format
import kotlinx.coroutines.flow.update
import android.content.Context // Added
import android.content.SharedPreferences // Added

enum class RegistrationError {
    INVALID_USER,
    PHONE_MISMATCH,
    ALREADY_REGISTERED,
    NONE
}

enum class LoginError {
    NOT_REGISTERED,
    INVALID_CREDENTIALS,
    NONE
}

// Add GeminiUiState here or ensure it's imported if in a separate file
// sealed interface GeminiUiState { ... }

data class AdminAnalytics(
    val averageHeifaMale: Double? = null,
    val averageHeifaFemale: Double? = null,
    val genAiInsights: List<String>? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

class UserViewModel(
    private val repository: Repository,
    private val geminiApiService: GeminiApiService,
    private val fruityViceApiService: FruityViceApiService,
    private val applicationContext: Context // Added applicationContext
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient

    private val _isLoadingSession = MutableStateFlow(true) // Added
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession // Added

    private val prefs: SharedPreferences = // Added
        applicationContext.getSharedPreferences("UserSessionPrefs", Context.MODE_PRIVATE) // Added

    init { // Added init block
        viewModelScope.launch {
            val loggedInUserId = prefs.getString("LOGGED_IN_USER_ID", null)
            if (loggedInUserId != null) {
                loadPatient(loggedInUserId) // This already sets _patient
            }
            _isLoadingSession.value = false
        }
    }

    // State for fruit details
    private val _fruitDetails = MutableStateFlow<FruitData?>(null)
    val fruitDetails: StateFlow<FruitData?> = _fruitDetails

    // State for AI-generated motivational message
    private val _motivationalMessage = MutableStateFlow<String?>(null)
    val motivationalMessage: StateFlow<String?> = _motivationalMessage
    
    // State for all tips for the modal
    private val _allNutriCoachTips = MutableStateFlow<List<NutriCoachTip>>(emptyList())
    val allNutriCoachTips: StateFlow<List<NutriCoachTip>> = _allNutriCoachTips

    // State for showing the "All Tips" modal
    private val _showAllTipsModal = MutableStateFlow(false)
    val showAllTipsModal: StateFlow<Boolean> = _showAllTipsModal

    // State for Gemini API calls
    private val _geminiUiState = MutableStateFlow<GeminiUiState>(GeminiUiState.Initial)
    val geminiUiState: StateFlow<GeminiUiState> = _geminiUiState

    // State for Admin Analytics
    private val _adminAnalytics = MutableStateFlow(AdminAnalytics())
    val adminAnalytics: StateFlow<AdminAnalytics> = _adminAnalytics

    fun toggleAllTipsModal(show: Boolean) {
        _showAllTipsModal.value = show
    }

    /**
     * Checks if the patient's fruit score is optimal.
     * Optimal is assumed to be the max HEIFA score for fruits (5.0).
     */
    fun isFruitScoreOptimal(patient: Patient?): Boolean {
        return patient?.fruits ?: 0.0 >= 5.0 // Max HEIFA score for Fruits (serves)
    }

    fun setPatient(patient: Patient?) {
        _patient.value = patient
    }

    fun loadPatient(userId: String) {
        viewModelScope.launch {
            val patient = repository.getPatientById(userId)
            patient?.let {
                // No need to create a new Patient object if it's already the correct type
                setPatient(it)
            }
        }
    }

    fun saveFoodIntake(intake: FoodIntake) {
        viewModelScope.launch {
            repository.saveFoodIntake(intake)
        }
    }

    fun loadTips(userId: String, onResult: ((List<NutriCoachTip>) -> Unit)? = null) {
        viewModelScope.launch {
            val tips = repository.getTips(userId)
            _allNutriCoachTips.value = tips // Update state for the modal
            onResult?.invoke(tips) // Keep original callback for existing screen if needed
        }
    }

    suspend fun getFoodIntakeForUser(userId: String): FoodIntake? {
        return repository.getFoodIntake(userId)
    }

    fun login(userId: String, phoneNumber: String, password: String, onResult: (Boolean, LoginError, Patient?) -> Unit) {
        viewModelScope.launch {
            val patient = repository.getPatientById(userId)
            if (patient == null || patient.password == null) {
                onResult(false, LoginError.NOT_REGISTERED, null)
                return@launch
            }
            
            if (patient.password != password) {
                onResult(false, LoginError.INVALID_CREDENTIALS, null)
                return@launch
            }

            _patient.value = patient
            // Save logged-in user ID
            with(prefs.edit()) { // Added
                putString("LOGGED_IN_USER_ID", userId) // Added
                apply() // Added
            } // Added
            onResult(true, LoginError.NONE, patient)
        }
    }

    fun register(userId: String, userName:String, phoneNumber: String, password: String, onResult: (Boolean, RegistrationError) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getPatientById(userId)
            if (existing == null) {
                onResult(false, RegistrationError.INVALID_USER)
                return@launch
            }

            if (existing.password != null) {
                onResult(false, RegistrationError.ALREADY_REGISTERED)
                return@launch
            }

            if (existing.phoneNumber != phoneNumber) {
                onResult(false, RegistrationError.PHONE_MISMATCH)
                return@launch
            }

            val updatedPatient = existing.copy(password = password, userName = userName)
            repository.updatePatient(updatedPatient)
            _patient.value = updatedPatient
            // Also log in the user by saving their ID after registration
            with(prefs.edit()) { // Added
                putString("LOGGED_IN_USER_ID", userId) // Added
                apply() // Added
            } // Added
            onResult(true, RegistrationError.NONE)
        }
    }

    /**
     * Fetches fruit details from an API.
     * This is a placeholder and needs actual API integration (e.g., FruityVice).
     */
    fun getFruitDetails(fruitName: String) {
        viewModelScope.launch {
            _fruitDetails.value = null // Clear previous details
            try {
                val response = fruityViceApiService.getFruitByName(fruitName.trim())
                if (response.isSuccessful && response.body() != null) {
                    val fruityViceData = response.body()!!
                    _fruitDetails.value = FruitData(
                        name = fruityViceData.name ?: "Unknown Fruit",
                        family = fruityViceData.family ?: "Unknown Family",
                        // Assuming nutritions are per 100g by default from FruityVice
                        calories = fruityViceData.nutritions?.calories ?: 0.0,
                        fat = fruityViceData.nutritions?.fat ?: 0.0,
                        sugar = fruityViceData.nutritions?.sugar ?: 0.0,
                        carbohydrates = fruityViceData.nutritions?.carbohydrates ?: 0.0,
                        protein = fruityViceData.nutritions?.protein ?: 0.0,
                        nutritions = emptyMap() // FruityVice free tier doesn't provide detailed other nutritions
                                                // If you have a paid tier or other source, map them here.
                    )
                } else {
                    // Handle error or fruit not found
                    Log.e("UserViewModel", "FruityVice API Error (${response.code()}): ${response.errorBody()?.string()}")
                    _fruitDetails.value = null
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "FruityVice Network Error: ", e)
                _fruitDetails.value = null
            }
        }
    }

    /**
     * Generates a motivational message using an AI model (e.g., Google Gemini).
     * This version constructs a detailed prompt based on patient data.
     * It will also save the generated tip.
     */
    fun generateMotivationalMessage(currentPatient: Patient, foodIntake: FoodIntake?) {
        viewModelScope.launch(Dispatchers.IO) { // Use IO dispatcher for network call
            _geminiUiState.value = GeminiUiState.Loading
            // 1. Determine overall diet quality based on totalScore
            val totalScore = currentPatient.totalScore
            val overallDietComment = when {
                totalScore > 80 -> "Your overall diet quality is good! That's fantastic."
                totalScore >= 51 -> "Your overall diet is on the right track and needs some improvement. Let's see how we can make it even better."
                else -> "Your overall diet has significant room for improvement, but don't worry, every small step counts!"
            }

            // 2. Identify 1-2 areas for improvement.
            // HEIFA Max Scores (general, without sex differentiation for now for simplicity in this step)
            val heifaMaxScores = mapOf(
                "fruits" to 5.0,
                "vegetables" to 5.0,
                "grains" to 5.0, // General grains
                "wholeGrains" to 5.0, // Specific to whole grains if available
                "meat" to 10.0, // General, actual optimal varies by sex
                "dairy" to 10.0,
                "water" to 10.0,
                "sodium" to 10.0, // Lower intake is better, so score reflects that
                "sugar" to 10.0, // Added sugars
                "saturatedFat" to 10.0,
                "discretionary" to 10.0, // Lower intake is better
                "alcohol" to 5.0 // Lower intake is better
            )

            val patientScores = mapOf(
                "fruits" to currentPatient.fruits,
                "vegetables" to currentPatient.vegetables,
                "grains" to currentPatient.grains,
                "wholeGrains" to currentPatient.wholeGrains,
                "meat" to currentPatient.meat,
                "dairy" to currentPatient.dairy,
                "water" to currentPatient.water,
                "sodium" to currentPatient.sodium,
                "sugar" to currentPatient.sugar,
                "saturatedFat" to currentPatient.saturatedFat,
                "discretionary" to currentPatient.discretionary,
                "alcohol" to currentPatient.alcohol
            )

            val areasToImprove = patientScores.mapNotNull { (key, score) ->
                val maxScore = heifaMaxScores[key]
                if (maxScore != null && score < maxScore * 0.6) { // Prioritize if less than 60% of max
                    // For "lower is better" scores, a low patient score is good.
                    // This logic needs refinement for inverse scores (sodium, sugar, satFat, discretionary, alcohol)
                    // For now, let's focus on "higher is better" for simplicity in prompt generation.
                    // A more sophisticated approach would convert all scores to a "0-1 achievement scale".
                    if (key in listOf("fruits", "vegetables", "grains", "wholeGrains", "meat", "dairy", "water")) {
                         Pair(key, score / maxScore) // score as percentage of max
                    } else {
                        null // Skip inverse scores for now in this simplified selection
                    }
                } else {
                    null
                }
            }
            .sortedBy { it.second } // Sort by lowest percentage of max score
            .take(2) // Take top 2
            .map { it.first }

            val improvementFocus = if (areasToImprove.isNotEmpty()) {
                "Let's focus on improving your intake of: ${areasToImprove.joinToString(" and ")}."
            } else {
                "You're doing great in many areas!"
            }

            // 3. Construct the prompt for Gemini
            val promptText = """
You are a friendly and encouraging NutriCoach. Your goal is to provide supportive and actionable advice.

User's Current Status:
- Overall Diet Assessment: $overallDietComment
- Total HEIFA Score: ${currentPatient.totalScore} (out of 100)
- Key Scores (actual/max HEIFA):
  - Fruits: ${currentPatient.fruits}/${heifaMaxScores["fruits"]}
  - Vegetables: ${currentPatient.vegetables}/${heifaMaxScores["vegetables"]}
  - Grains: ${currentPatient.grains}/${heifaMaxScores["grains"]}
  - Whole Grains: ${currentPatient.wholeGrains}/${heifaMaxScores["wholeGrains"]}
  - Meat/Alternatives: ${currentPatient.meat}/${heifaMaxScores["meat"]} (Note: optimal varies by sex, general score shown)
  - Dairy/Alternatives: ${currentPatient.dairy}/${heifaMaxScores["dairy"]}
  - Water: ${currentPatient.water}/${heifaMaxScores["water"]}
  - Sodium: ${currentPatient.sodium}/${heifaMaxScores["sodium"]} (Note: lower intake leads to higher score)
  - Added Sugars: ${currentPatient.sugar}/${heifaMaxScores["sugar"]} (Note: lower intake leads to higher score)
  - Saturated Fat: ${currentPatient.saturatedFat}/${heifaMaxScores["saturatedFat"]} (Note: lower intake leads to higher score)
  - Discretionary Foods: ${currentPatient.discretionary}/${heifaMaxScores["discretionary"]} (Note: lower intake leads to higher score)
  - Alcohol: ${currentPatient.alcohol}/${heifaMaxScores["alcohol"]} (Note: lower intake leads to higher score)

Recent Food Intake:
- Selected foods: ${foodIntake?.selectedFoods ?: "Not specified"}

Task:
Based on the User's Current Status, generate a short (2-3 sentences), positive, and encouraging motivational message.
$improvementFocus
Suggest one or two small, actionable tips to help them with the focus areas, or general healthy eating if no specific low areas were identified.
You can subtly reference their recently selected foods if it makes sense.
Keep the tone supportive and avoid being judgmental. End with an uplifting note.
""" // Ensure this multi-line string is correctly terminated

            try {
                val generationConf = GenerationConfig(temperature = 0.7f)
                val request = GeminiRequest(
                    contents = listOf(ContentEntry(parts = listOf(TextPart(text = promptText)))),
                    generationConfig = generationConf
                )
                val response = geminiApiService.generateContent(BuildConfig.GEMINI_API_KEY, request)

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    val generatedText = responseBody.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (generatedText != null) {
                        _geminiUiState.value = GeminiUiState.Success(generatedText)
                        _motivationalMessage.value = generatedText
                        saveNutriCoachTip(currentPatient.userId, generatedText)
                    } else {
                        val errorMsg = responseBody.error?.message ?: "Failed to extract text from Gemini response."
                        _geminiUiState.value = GeminiUiState.Error(errorMsg)
                        _motivationalMessage.value = null // Clear previous message on error
                        Log.e("UserViewModel", "Gemini API Error or empty content: $errorMsg")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown API error"
                    _geminiUiState.value = GeminiUiState.Error("API Error (${response.code()}): $errorBody")
                    _motivationalMessage.value = null
                    Log.e("UserViewModel", "Gemini API Error (${response.code()}): $errorBody")
                }
            } catch (e: Exception) {
                _geminiUiState.value = GeminiUiState.Error("Network Error: ${e.message}")
                _motivationalMessage.value = null
                Log.e("UserViewModel", "Gemini Network Error: ", e)
            }
        }
    }

    /**
     * Saves a new NutriCoach tip to the database.
     */
    fun saveNutriCoachTip(userId: String, message: String) {
        viewModelScope.launch {
            val newTip = NutriCoachTip(patientId = userId, message = message, timestamp = System.currentTimeMillis())
            repository.saveTip(newTip)
            // After saving, reload tips to update the list
            loadTips(userId)
        }
    }
    
    /**
     * Clears the fruit details, typically when the user navigates away or wants to search for a new fruit.
     */
    fun clearFruitDetails() {
        _fruitDetails.value = null
    }

    /**
     * Clears the currently displayed motivational message.
     */
    fun clearMotivationalMessage() {
        _motivationalMessage.value = null
        _geminiUiState.value = GeminiUiState.Initial // Reset Gemini state
    }

    fun logout() { // Added
        viewModelScope.launch { // Added
            with(prefs.edit()) { // Added
                remove("LOGGED_IN_USER_ID") // Added
                apply() // Added
            } // Added
            _patient.value = null // Added
            // Optionally, clear other user-specific states here if needed
            // e.g., _fruitDetails.value = null, _motivationalMessage.value = null, etc.
        } // Added
    } // Added

    fun loadAverageHeifaScores() {
        viewModelScope.launch {
            // Start loading for scores part
            _adminAnalytics.update {
                it.copy(isLoading = true, error = null) // Indicate loading for scores, clear previous errors
            }
            try {
                val allPatients = repository.getAllPatients()
                if (allPatients.isEmpty()) {
                    _adminAnalytics.update {
                        it.copy(error = "No patient data found for scores.", isLoading = false)
                    }
                    return@launch
                }

                val malePatients = allPatients.filter { it.sex.equals("Male", ignoreCase = true) }
                val femalePatients = allPatients.filter { it.sex.equals("Female", ignoreCase = true) }

                val avgMaleScore = if (malePatients.isNotEmpty()) malePatients.map { it.totalScore }.average() else null
                val avgFemaleScore = if (femalePatients.isNotEmpty()) femalePatients.map { it.totalScore }.average() else null

                _adminAnalytics.update {
                    it.copy(
                        averageHeifaMale = avgMaleScore,
                        averageHeifaFemale = avgFemaleScore,
                        isLoading = false // Scores loaded
                    )
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading average HEIFA scores: ", e)
                _adminAnalytics.update {
                    it.copy(error = "Failed to load average scores: ${e.message}", isLoading = false)
                }
            }
        }
    }

    fun generateAdminInsights() { // Renamed from loadAdminAnalytics
        viewModelScope.launch(Dispatchers.IO) { // Keep IO for network
            // Indicate loading for insights
            _adminAnalytics.update { it.copy(isLoading = true, error = null, genAiInsights = null) } // Clear previous insights and errors

            try {
                // Fetch all patients again, or reuse if we stored them. For simplicity, re-fetching.
                val allPatients = repository.getAllPatients()
                if (allPatients.isEmpty()) {
                    _adminAnalytics.update { it.copy(error = "No patient data for insights.", isLoading = false) }
                    return@launch
                }

                // Scores are needed for the prompt context, recalculate or fetch from state
                val malePatients = allPatients.filter { it.sex.equals("Male", ignoreCase = true) }
                val femalePatients = allPatients.filter { it.sex.equals("Female", ignoreCase = true) }
                val avgMaleScore = if (malePatients.isNotEmpty()) malePatients.map { it.totalScore }.average() else _adminAnalytics.value.averageHeifaMale
                val avgFemaleScore = if (femalePatients.isNotEmpty()) femalePatients.map { it.totalScore }.average() else _adminAnalytics.value.averageHeifaFemale


                // Prepare data for GenAI (summaryForGenAI and genAiPrompt remains the same)
                val summaryForGenAI = """
Overall Patient Count: ${allPatients.size}
Male Patients: ${malePatients.size}, Average HEIFA Score: ${avgMaleScore?.format(1) ?: "N/A"}
Female Patients: ${femalePatients.size}, Average HEIFA Score: ${avgFemaleScore?.format(1) ?: "N/A"}

Additional data points (averages across all users):
Fruits Score: ${allPatients.map { it.fruits }.average().format(1)}
Vegetables Score: ${allPatients.map { it.vegetables }.average().format(1)}
Grains Score: ${allPatients.map { it.grains }.average().format(1)}
Whole Grains Score: ${allPatients.map { it.wholeGrains }.average().format(1)}
Meat Score: ${allPatients.map { it.meat }.average().format(1)}
Dairy Score: ${allPatients.map { it.dairy }.average().format(1)}
Water Score: ${allPatients.map { it.water }.average().format(1)}
Sodium Score (lower is better reflected in score): ${allPatients.map { it.sodium }.average().format(1)}
Added Sugars Score (lower is better reflected in score): ${allPatients.map { it.sugar }.average().format(1)}
Saturated Fat Score (lower is better reflected in score): ${allPatients.map { it.saturatedFat }.average().format(1)}
Discretionary Foods Score (lower is better reflected in score): ${allPatients.map { it.discretionary }.average().format(1)}

Consider potential correlations, like if users with high vegetable scores also tend to have high fruit scores, or if one gender group shows greater dietary variety or higher scores in specific categories.
"""

                val genAiPrompt = """
As a data analyst for a nutrition app, review the following summary of user dietary scores.
Based ONLY on the data provided below, please generate exactly 3 distinct, insightful, and concise observations or patterns.
To ensure a diverse set of findings, try to provide insights that cover different aspects, for example:
- One insight about an overall population trend (e.g., a common low-scoring food group, or a widely consumed item).
- One insight about a potential correlation between different dietary scores or food group consumptions.
- One insight comparing demographic groups (e.g., male vs. female performance on a specific dietary component or overall).

Ensure the specific insights vary each time this function is called.
Each insight must be a single sentence. Do not use markdown like bullet points. Separate each insight with a newline character.

Data Summary:
$summaryForGenAI

Insights:
"""
                var insights: List<String>? = null
                var genAiError: String? = null

                try {
                    // Increased temperature for more variability
                    val generationConf = GenerationConfig(temperature = 0.8f, topK = 40, topP = 0.95f)
                    val request = GeminiRequest(
                        contents = listOf(ContentEntry(parts = listOf(TextPart(text = genAiPrompt)))),
                        generationConfig = generationConf
                    )
                    val response = geminiApiService.generateContent(BuildConfig.GEMINI_API_KEY, request)
                    if (response.isSuccessful && response.body() != null) {
                        val generatedText = response.body()!!.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (generatedText != null) {
                            insights = generatedText.trim().split("\n").map { it.trim() }.filter { it.isNotEmpty() }.take(3)
                        } else {
                            genAiError = response.body()!!.error?.message ?: "Failed to extract insights from Gemini response."
                            Log.e("UserViewModel", "Admin GenAI Error or empty content: $genAiError")
                        }
                    } else {
                        genAiError = "Admin GenAI API Error (${response.code()}): ${response.errorBody()?.string() ?: "Unknown API error"}"
                        Log.e("UserViewModel", genAiError)
                    }
                } catch (e: Exception) {
                    genAiError = "Admin GenAI Network Error: ${e.message}"
                    Log.e("UserViewModel", "Admin GenAI Network Error: ", e)
                }

                _adminAnalytics.update {
                    it.copy(
                        // Scores should already be there, no need to update them again unless fetched fresh
                        genAiInsights = insights,
                        error = genAiError ?: it.error, // Preserve score error if genAI is fine, or show genAI error
                        isLoading = false // Insights loaded
                    )
                }

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading admin analytics (insights part): ", e)
                _adminAnalytics.update { it.copy(error = "Failed to generate insights: ${e.message}", isLoading = false) }
            }
        }
    }
}
