package com.example.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.authStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.currentUser
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.signInWithGoogle(activity)
            _isLoading.value = false
            result.onFailure { error ->
                if (error is GetCredentialCancellationException) {
                    Log.d(TAG, "Sign in cancelled by user")
                    // Do not treat cancellation as a hard error
                    _errorMessage.value = null
                } else {
                    Log.e(TAG, "Sign in failed", error)
                    _errorMessage.value = error.localizedMessage ?: "Sign-in failed. Please try again."
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun signOut() {
        authRepository.signOut()
    }
}
