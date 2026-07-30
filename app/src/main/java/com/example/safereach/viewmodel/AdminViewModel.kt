package com.example.safereach.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safereach.network.*
import kotlinx.coroutines.launch

sealed class AdminUiState<out T> {
    object Idle : AdminUiState<Nothing>()
    object Loading : AdminUiState<Nothing>()
    data class Success<T>(val data: T) : AdminUiState<T>()
    data class Error(val message: String) : AdminUiState<Nothing>()
}

class AdminViewModel : ViewModel() {
    
    private val _hospitalsState = mutableStateOf<AdminUiState<List<HospitalItem>>>(AdminUiState.Idle)
    val hospitalsState: State<AdminUiState<List<HospitalItem>>> = _hospitalsState

    private val _requestsState = mutableStateOf<AdminUiState<List<RequestHistoryItem>>>(AdminUiState.Idle)
    val requestsState: State<AdminUiState<List<RequestHistoryItem>>> = _requestsState

    fun fetchHospitals() {
        viewModelScope.launch {
            _hospitalsState.value = AdminUiState.Loading
            try {
                val response = RetrofitClient.apiService.getHospitals()
                if (response.isSuccessful && response.body()?.success == true) {
                    _hospitalsState.value = AdminUiState.Success(response.body()?.data ?: emptyList())
                } else {
                    _hospitalsState.value = AdminUiState.Error(response.body()?.message ?: "Failed to fetch hospitals")
                }
            } catch (e: Exception) {
                _hospitalsState.value = AdminUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchAllRequests() {
        viewModelScope.launch {
            _requestsState.value = AdminUiState.Loading
            try {
                // Admin might use same history endpoint or a special one if available
                val response = RetrofitClient.apiService.getRequestsHistory()
                if (response.isSuccessful && response.body()?.success == true) {
                    _requestsState.value = AdminUiState.Success(response.body()?.data ?: emptyList())
                } else {
                    _requestsState.value = AdminUiState.Error(response.body()?.message ?: "Failed to fetch requests")
                }
            } catch (e: Exception) {
                _requestsState.value = AdminUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
