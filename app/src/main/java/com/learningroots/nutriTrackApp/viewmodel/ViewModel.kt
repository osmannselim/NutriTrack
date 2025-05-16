package com.learningroots.nutriTrackApp.viewmodel

import com.learningroots.nutriTrackApp.data.repository.Repository
import com.learningroots.nutriTrackApp.data.entity.*
import com.learningroots.nutriTrackApp.data.model.FruitData
import com.learningroots.nutriTrackApp.data.model.GeminiRequest
import com.learningroots.nutriTrackApp.data.model.ContentEntry
import com.learningroots.nutriTrackApp.data.model.TextPart
import com.learningroots.nutriTrackApp.data.network.GeminiApiService
import com.learningroots.nutriTrackApp.BuildConfig // Import BuildConfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log

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

class UserViewModel(
    private val repository: Repository,
    private val geminiApiService: GeminiApiService // Added GeminiApiService
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient

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

    fun setPatient(patient: Patient) {
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
            onResult(true, RegistrationError.NONE)
        }
    }

    /**
     * Fetches fruit details from an API.
     * This is a placeholder and needs actual API integration (e.g., FruityVice).
     */
    fun getFruitDetails(fruitName: String) {
        viewModelScope.launch {
            // Placeholder: Mock API call
            if (fruitName.equals("banana", ignoreCase = true)) {
                _fruitDetails.value = FruitData(
                    name = "Banana",
                    family = "Musaceae",
                    calories = 96.0,
                    fat = 0.2,
                    sugar = 17.2,
                    carbohydrates = 22.0,
                    protein = 1.0,
                    nutritions = mapOf("Potassium" to 0.422) // Example other nutrition
                )
            } else if (fruitName.equals("apple", ignoreCase = true)) {
                _fruitDetails.value = FruitData(
                    name = "Apple",
                    family = "Rosaceae",
                    calories = 95.0,
                    fat = 0.3,
                    sugar = 19.0,
                    carbohydrates = 25.0,
                    protein = 0.5,
                    nutritions = mapOf("Vitamin C" to 0.014)
                )
            } else {
                _fruitDetails.value = null // Fruit not found or error
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
                val request = GeminiRequest(contents = listOf(ContentEntry(parts = listOf(TextPart(text = promptText)))))
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
}
