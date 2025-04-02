package com.learningroots.nutriTrackApp.viewmodel

import androidx.lifecycle.ViewModel
import com.learningroots.nutriTrackApp.utils.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    private val _user = MutableStateFlow<UserData?>(null)
    val user: StateFlow<UserData?> = _user

    fun setUser(userData: UserData) {
        _user.value = userData
    }
}
