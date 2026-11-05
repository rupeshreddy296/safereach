package com.example.safereach.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safereach.network.*
import kotlinx.coroutines.launch

sealed class DriverUiState<out T> {
    object Idle : DriverUiState<Nothing>()
    object Loading : DriverUiState<Nothing>()
    data class Success<T>(val data: T) : DriverUiState<T>()
    data class Error(val message: String) : DriverUiState<Nothing>()
}

class DriverViewModel : ViewModel() {
    
    private val _statusState = mutableStateOf<DriverUiState<VehicleData>>(DriverUiState.Idle)
    val statusState: State<DriverUiState<VehicleData>> = _statusState

    private val _trackingState = mutableStateOf<DriverUiState<TrackResponse>>(DriverUiState.Idle)
    val trackingState: State<DriverUiState<TrackResponse>> = _trackingState

    fun updateStatus(isOnline: Boolean) {
        viewModelScope.launch {
            _statusState.value = DriverUiState.Loading
            try {
                val status = if (isOnline) "available" else "offline"
                val response = RetrofitClient.apiService.updateDriverStatus(DriverStatusRequest(status))
                if (response.isSuccessful && (response.body()?.success == true)) {
                    response.body()?.data?.let {
                        _statusState.value = DriverUiState.Success(it)
                    } ?: run { _statusState.value = DriverUiState.Error("Status updated but no data") }
                } else {
                    _statusState.value = DriverUiState.Error(response.body()?.message ?: "Failed to update status")
                }
            } catch (e: Exception) {
                _statusState.value = DriverUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.updateDriverLocation(DriverLocationRequest(lat, lng))
            } catch (e: Exception) {
                // Silently fail location updates or log them
            }
        }
    }

    fun acceptRequest(requestId: Int) {
        updateRequestStatus(requestId, "accepted")
    }

    fun arriveAtPickup(requestId: Int) {
        updateRequestStatus(requestId, "arrived")
    }

    fun patientBoarded(requestId: Int) {
        updateRequestStatus(requestId, "boarded")
    }

    fun completeTrip(requestId: Int) {
        updateRequestStatus(requestId, "completed")
    }

    private fun updateRequestStatus(requestId: Int, status: String) {
        viewModelScope.launch {
            _trackingState.value = DriverUiState.Loading
            try {
                val response = RetrofitClient.apiService.updateRequestStatus(StatusUpdateRequest(requestId, status))
                if (response.isSuccessful && response.body()?.success == true) {
                    // Refetch tracking info to update UI
                    fetchTrackingInfo(requestId)
                } else {
                    _trackingState.value = DriverUiState.Error(response.body()?.message ?: "Failed to update request status")
                }
            } catch (e: Exception) {
                _trackingState.value = DriverUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchTrackingInfo(requestId: Int) {
        viewModelScope.launch {
            _trackingState.value = DriverUiState.Loading
            try {
                val response = RetrofitClient.apiService.trackRequest(requestId)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        _trackingState.value = DriverUiState.Success(it)
                    } ?: run { _trackingState.value = DriverUiState.Error("No tracking data") }
                } else {
                    _trackingState.value = DriverUiState.Error(response.body()?.message ?: "Failed to fetch tracking")
                }
            } catch (e: Exception) {
                _trackingState.value = DriverUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _activeRequestsState = mutableStateOf<DriverUiState<List<RequestHistoryItem>>>(DriverUiState.Idle)
    val activeRequestsState: State<DriverUiState<List<RequestHistoryItem>>> = _activeRequestsState

    fun startPollingActiveRequests() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.apiService.getActiveRequests()
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        // Since backend returns Any, we need to handle it or cast it
                        // Assuming it returns a list of requests for driver
                        if (data is List<*>) {
                            // Map it correctly or assume structure
                            // _activeRequestsState.value = DriverUiState.Success(data as List<RequestHistoryItem>)
                        }
                    }
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(10000)
            }
        }
    }
}
