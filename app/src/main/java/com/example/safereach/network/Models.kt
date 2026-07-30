package com.example.safereach.network

import com.google.gson.annotations.SerializedName

// Generic response wrapper
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

// Auth Models
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class ForgotPasswordRequest(
    @SerializedName("phone_number") val phoneNumber: String
)

data class ResetPasswordRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("otp_code") val otpCode: String,
    @SerializedName("new_password") val newPassword: String
)

data class LoginData(
    @SerializedName("token") val token: String,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("vehicle") val vehicle: VehicleData?
)

data class VehicleData(
    @SerializedName("ambulance_id") val ambulanceId: Int,
    @SerializedName("vehicle_number") val vehicleNumber: String,
    @SerializedName("ambulance_type") val ambulanceType: String,
    @SerializedName("status") val status: String
)

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    @SerializedName("vehicle_number") val vehicleNumber: String? = null,
    @SerializedName("ambulance_type") val ambulanceType: String? = null
)

data class RegisterData(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("role") val role: String
)

// Profile Models
data class UserProfile(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("emergency_contact_name") val emergencyContactName: String?,
    @SerializedName("emergency_contact_phone") val emergencyContactPhone: String?,
    @SerializedName("blood_group") val bloodGroup: String?,
    @SerializedName("allergies") val allergies: String?,
    @SerializedName("chronic_conditions") val chronicConditions: String?,
    @SerializedName("created_at") val createdAt: String
)

data class ProfileUpdateRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("emergency_contact_name") val emergencyContactName: String? = null,
    @SerializedName("emergency_contact_phone") val emergencyContactPhone: String? = null,
    @SerializedName("blood_group") val bloodGroup: String? = null,
    @SerializedName("allergies") val allergies: String? = null,
    @SerializedName("chronic_conditions") val chronicConditions: String? = null
)

// Address Models
data class AddressItem(
    @SerializedName("address_id") val addressId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("created_at") val createdAt: String
)

data class AddressSaveRequest(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

// Emergency Request Models
data class RequestCreateData(
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("patient_phone") val patientPhone: String,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("pickup_latitude") val pickupLatitude: Double,
    @SerializedName("pickup_longitude") val pickupLongitude: Double,
    @SerializedName("requested_ambulance_type") val requestedAmbulanceType: String,
    @SerializedName("emergency_type") val emergencyType: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("scheduled_at") val scheduledAt: String? = null
)

data class RequestCreateResponse(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("requested_ambulance_type") val requestedAmbulanceType: String
)

data class StatusUpdateRequest(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("destination_hospital_id") val destinationHospitalId: Int? = null
)

data class TrackResponse(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("patient") val patient: PatientInfo,
    @SerializedName("pickup") val pickup: LocationInfo,
    @SerializedName("emergency") val emergency: EmergencyInfo,
    @SerializedName("timestamps") val timestamps: TimestampInfo,
    @SerializedName("assigned_ambulance") val assignedAmbulance: AssignedAmbulanceInfo?,
    @SerializedName("destination_hospital") val destinationHospital: DestinationHospitalInfo?,
    @SerializedName("vitals") val vitals: VitalsInfo?
)

data class PatientInfo(
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String
)

data class LocationInfo(
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class EmergencyInfo(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String?,
    @SerializedName("requested_ambulance_type") val requestedAmbulanceType: String
)

data class TimestampInfo(
    @SerializedName("created") val created: String,
    @SerializedName("accepted") val accepted: String?,
    @SerializedName("arrived") val arrived: String?,
    @SerializedName("boarded") val boarded: String?,
    @SerializedName("completed") val completed: String?,
    @SerializedName("cancelled") val cancelled: String?
)

data class AssignedAmbulanceInfo(
    @SerializedName("ambulance_id") val ambulanceId: Int,
    @SerializedName("vehicle_number") val vehicleNumber: String,
    @SerializedName("type") val type: String,
    @SerializedName("driver_name") val driverName: String,
    @SerializedName("driver_phone") val driverPhone: String,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

data class DestinationHospitalInfo(
    @SerializedName("hospital_id") val hospitalId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class VitalsInfo(
    @SerializedName("heart_rate") val heartRate: Int,
    @SerializedName("blood_pressure") val bloodPressure: String,
    @SerializedName("oxygen_saturation") val oxygenSaturation: Int,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("condition") val condition: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class FeedbackSubmitRequest(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comments") val comments: String?
)

// Driver Models
data class DriverStatusRequest(
    @SerializedName("status") val status: String
)

data class DriverLocationRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)

data class VitalsUpdateRequest(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("heart_rate") val heartRate: Int,
    @SerializedName("blood_pressure") val bloodPressure: String,
    @SerializedName("oxygen_saturation") val oxygenSaturation: Int,
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("patient_condition") val patientCondition: String
)

// Notification Models
data class NotificationItem(
    @SerializedName("notification_id") val notificationId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)

data class NotificationReadRequest(
    @SerializedName("notification_id") val notificationId: Int? = null
)

// Request History Models
data class RequestHistoryItem(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("patient_phone") val patientPhone: String,
    @SerializedName("pickup_address") val pickupAddress: String,
    @SerializedName("pickup_latitude") val pickupLatitude: Double,
    @SerializedName("pickup_longitude") val pickupLongitude: Double,
    @SerializedName("requested_ambulance_type") val requestedAmbulanceType: String,
    @SerializedName("emergency_type") val emergencyType: String,
    @SerializedName("description") val description: String?,
    @SerializedName("assigned_ambulance_id") val assignedAmbulanceId: Int?,
    @SerializedName("destination_hospital_id") val destinationHospitalId: Int?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("accepted_at") val acceptedAt: String?,
    @SerializedName("arrived_at") val arrivedAt: String?,
    @SerializedName("boarded_at") val boardedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("cancelled_at") val cancelledAt: String?,
    @SerializedName("requester_name") val requesterName: String?,
    @SerializedName("driver_name") val driverName: String?,
    @SerializedName("vehicle_number") val vehicleNumber: String?,
    @SerializedName("hospital_name") val hospitalName: String?,
    @SerializedName("rating") val rating: Int?,
    @SerializedName("comments") val comments: String?
)

// Hospital Models
data class HospitalItem(
    @SerializedName("hospital_id") val hospitalId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("available_icu_beds") val availableIcuBeds: Int,
    @SerializedName("available_nicu_beds") val availableNicuBeds: Int,
    @SerializedName("status") val status: String,
    @SerializedName("updated_at") val updatedAt: String
)

