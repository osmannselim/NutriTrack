package com.learningroots.nutriTrackApp.viewmodel

import com.learningroots.nutriTrackApp.data.repository.Repository
import com.learningroots.nutriTrackApp.data.entity.*
import com.learningroots.nutriTrackApp.data.model.FruitData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

class UserViewModel(private val repository: Repository) : ViewModel() {

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

    fun register(userId: String, phoneNumber: String, password: String, onResult: (Boolean, RegistrationError) -> Unit) {
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

            val updatedPatient = existing.copy(password = password)
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
     * Generates a motivational message using an AI model.
     * This is a placeholder and needs actual AI integration (e.g., Google Gemini).
     * It should also save the generated tip.
     */
    fun generateMotivationalMessage(currentPatient: Patient, foodIntake: FoodIntake?) {
        viewModelScope.launch {
            // Placeholder: Mock AI call
            // The prompt to Gemini would be: "Generate a short encouraging message to help someone improve their fruit intake."
            // For HD++: Send patient data (currentPatient) and food intake (foodIntake) to the AI.
            
            val fruitScore = currentPatient.fruits
            val specificMessage = if (foodIntake != null) {
                "Hey ${currentPatient.userId}, remember when you enjoyed ${foodIntake.selectedFoods.split("\\").firstOrNull() ?: "your favorite healthy snack"}? Let's try adding more fruits like apples or bananas to your day! Your current fruit score is $fruitScore."
            } else {
                "Hey ${currentPatient.userId}! Just a little nudge to maybe grab a banana or an apple today. They're a super easy and tasty way to get some extra goodness in. You got this! Your fruit score is $fruitScore."
            }
            
            _motivationalMessage.value = specificMessage
            // Save the generated tip
            saveNutriCoachTip(currentPatient.userId, specificMessage)
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
    }
}
