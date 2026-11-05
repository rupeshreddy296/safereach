package com.example.safereach.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safereach.UserRole
import com.example.safereach.network.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object RegistrationSuccess : AuthState()
    object ForgotPasswordSent : AuthState()
    object ResetPasswordSuccess : AuthState()
    data class Success(val user: LoginData) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                if (response.isSuccessful && (response.body()?.success == true)) {
                    val data = response.body()?.data
                    if (data != null) {
                        RetrofitClient.setToken(data.token)
                        _authState.value = AuthState.Success(data)
                    } else {
                        _authState.value = AuthState.Error("Login successful but no data returned")
                    }
                } else {
                    _authState.value = AuthState.Error(response.body()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun register(fullName: String, email: String, password: String, role: UserRole, vehicleNumber: String? = null, ambulanceType: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = RegisterRequest(
                    fullName = fullName,
                    email = email,
                    password = password,
                    role = role.name,
                    vehicleNumber = vehicleNumber,
                    ambulanceType = ambulanceType
                )
                val response = RetrofitClient.apiService.register(request)
                if (response.isSuccessful && (response.body()?.success == true)) {
                    _authState.value = AuthState.RegistrationSuccess
                } else {
                    _authState.value = AuthState.Error(response.body()?.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun forgotPassword(phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.forgotPassword(ForgotPasswordRequest(phoneNumber))
                if (response.isSuccessful && response.body()?.success == true) {
                    _authState.value = AuthState.ForgotPasswordSent
                } else {
                    _authState.value = AuthState.Error(response.body()?.message ?: "Failed to send reset code")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }

    fun resetPassword(phoneNumber: String, otp: String, newPass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.resetPassword(ResetPasswordRequest(phoneNumber, otp, newPass))
                if (response.isSuccessful && response.body()?.success == true) {
                    _authState.value = AuthState.ResetPasswordSuccess
                } else {
                    _authState.value = AuthState.Error(response.body()?.message ?: "Failed to reset password")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
