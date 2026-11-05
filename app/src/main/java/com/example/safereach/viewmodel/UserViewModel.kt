package com.example.safereach.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safereach.network.*
import kotlinx.coroutines.launch

sealed class UserUiState<out T> {
    object Idle : UserUiState<Nothing>()
    object Loading : UserUiState<Nothing>()
    data class Success<T>(val data: T) : UserUiState<T>()
    data class Error(val message: String) : UserUiState<Nothing>()
}

class UserViewModel : ViewModel() {
    
    private val _profileState = mutableStateOf<UserUiState<UserProfile>>(UserUiState.Idle)
    val profileState: State<UserUiState<UserProfile>> = _profileState

    private val _addressesState = mutableStateOf<UserUiState<List<AddressItem>>>(UserUiState.Idle)
    val addressesState: State<UserUiState<List<AddressItem>>> = _addressesState

    private val _requestState = mutableStateOf<UserUiState<RequestCreateResponse>>(UserUiState.Idle)
    val requestState: State<UserUiState<RequestCreateResponse>> = _requestState

    private val _historyState = mutableStateOf<UserUiState<List<RequestHistoryItem>>>(UserUiState.Idle)
    val historyState: State<UserUiState<List<RequestHistoryItem>>> = _historyState

    private val _notificationsState = mutableStateOf<UserUiState<List<NotificationItem>>>(UserUiState.Idle)
    val notificationsState: State<UserUiState<List<NotificationItem>>> = _notificationsState

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = UserUiState.Loading
            try {
                val response = RetrofitClient.apiService.getProfile()
                if (response.isSuccessful && (response.body()?.success == true)) {
                    response.body()?.data?.let {
                        _profileState.value = UserUiState.Success(it)
                    } ?: run { _profileState.value = UserUiState.Error("No data") }
                } else {
                    _profileState.value = UserUiState.Error(response.body()?.message ?: "Failed to fetch profile")
                }
            } catch (e: Exception) {
                _profileState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateProfile(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            _profileState.value = UserUiState.Loading
            try {
                val response = RetrofitClient.apiService.updateProfile(request)
                if (response.isSuccessful && (response.body()?.success == true)) {
                    response.body()?.data?.let {
                        _profileState.value = UserUiState.Success(it)
                    } ?: run { _profileState.value = UserUiState.Error("Update successful but no data") }
                } else {
                    _profileState.value = UserUiState.Error(response.body()?.message ?: "Failed to update profile")
                }
            } catch (e: Exception) {
                _profileState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchAddresses() {
        viewModelScope.launch {
            _addressesState.value = UserUiState.Loading
            try {
                val response = RetrofitClient.apiService.getAddresses()
                if (response.isSuccessful && (response.body()?.success == true)) {
                    _addressesState.value = UserUiState.Success(response.body()?.data ?: emptyList())
                } else {
                    _addressesState.value = UserUiState.Error(response.body()?.message ?: "Failed to fetch addresses")
                }
            } catch (e: Exception) {
                _addressesState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _trackingState = mutableStateOf<UserUiState<TrackResponse>>(UserUiState.Idle)
    val trackingState: State<UserUiState<TrackResponse>> = _trackingState

    fun createEmergencyRequest(
        patientName: String,
        patientPhone: String,
        address: String,
        lat: Double,
        lng: Double,
        type: String,
        condition: String,
        desc: String? = null,
        scheduledAt: String? = null
    ) {
        viewModelScope.launch {
            _requestState.value = UserUiState.Loading
            try {
                val request = RequestCreateData(
                    patientName = patientName,
                    patientPhone = patientPhone,
                    pickupAddress = address,
                    pickupLatitude = lat,
                    pickupLongitude = lng,
                    requestedAmbulanceType = type,
                    emergencyType = condition,
                    description = desc,
                    scheduledAt = scheduledAt
                )
                val response = RetrofitClient.apiService.createRequest(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        _requestState.value = UserUiState.Success(data)
                        // Start tracking this request
                        startTracking(data.requestId)
                    } else {
                        _requestState.value = UserUiState.Error("Request created but no ID")
                    }
                } else {
                    _requestState.value = UserUiState.Error(response.body()?.message ?: "Failed to create request")
                }
            } catch (e: Exception) {
                _requestState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private var trackingJob: kotlinx.coroutines.Job? = null

    fun startTracking(requestId: Int) {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.apiService.trackRequest(requestId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.let {
                            _trackingState.value = UserUiState.Success(it)
                        }
                    }
                } catch (e: Exception) {
                    // Silently ignore tracking errors during polling
                }
                kotlinx.coroutines.delay(5000) // Poll every 5 seconds
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _historyState.value = UserUiState.Loading
            try {
                val response = RetrofitClient.apiService.getRequestsHistory()
                if (response.isSuccessful && response.body()?.success == true) {
                    _historyState.value = UserUiState.Success(response.body()?.data ?: emptyList())
                } else {
                    _historyState.value = UserUiState.Error(response.body()?.message ?: "Failed to fetch history")
                }
            } catch (e: Exception) {
                _historyState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _notificationsState.value = UserUiState.Loading
            try {
                val response = RetrofitClient.apiService.getNotifications()
                if (response.isSuccessful && response.body()?.success == true) {
                    _notificationsState.value = UserUiState.Success(response.body()?.data ?: emptyList())
                } else {
                    _notificationsState.value = UserUiState.Error(response.body()?.message ?: "Failed to fetch notifications")
                }
            } catch (e: Exception) {
                _notificationsState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _hospitalsState = mutableStateOf<UserUiState<List<HospitalItem>>>(UserUiState.Idle)
    val hospitalsState: State<UserUiState<List<HospitalItem>>> = _hospitalsState

    fun fetchHospitals() {
        viewModelScope.launch {
            _hospitalsState.value = UserUiState.Loading
            try {
                val response = RetrofitClient.apiService.getHospitals()
                if (response.isSuccessful && response.body()?.success == true) {
                    _hospitalsState.value = UserUiState.Success(response.body()?.data ?: emptyList())
                } else {
                    _hospitalsState.value = UserUiState.Error(response.body()?.message ?: "Failed to fetch hospitals")
                }
            } catch (e: Exception) {
                _hospitalsState.value = UserUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
