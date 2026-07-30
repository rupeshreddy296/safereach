package com.example.safereach.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegisterData>>

    @POST("api/auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @POST("api/auth/forgot_password.php")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Any>>

    @POST("api/auth/reset_password.php")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    // User Profile
    @GET("api/user/profile.php")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>

    @PUT("api/user/profile.php")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<ApiResponse<UserProfile>>

    // Saved Addresses
    @GET("api/user/addresses.php")
    suspend fun getAddresses(): Response<ApiResponse<List<AddressItem>>>

    @POST("api/user/addresses.php")
    suspend fun saveAddress(@Body request: AddressSaveRequest): Response<ApiResponse<AddressItem>>

    @DELETE("api/user/addresses.php")
    suspend fun deleteAddress(@Query("id") id: Int): Response<ApiResponse<Unit>>

    // Emergency Requests
    @POST("api/requests/create.php")
    suspend fun createRequest(@Body request: RequestCreateData): Response<ApiResponse<RequestCreateResponse>>

    @GET("api/requests/active.php")
    suspend fun getActiveRequests(): Response<ApiResponse<Any>> // Returns different types depending on user role

    @POST("api/requests/update_status.php")
    suspend fun updateRequestStatus(@Body request: StatusUpdateRequest): Response<ApiResponse<StatusUpdateRequest>>

    @GET("api/requests/track.php")
    suspend fun trackRequest(@Query("request_id") requestId: Int?): Response<ApiResponse<TrackResponse>>

    @POST("api/requests/feedback.php")
    suspend fun submitFeedback(@Body request: FeedbackSubmitRequest): Response<ApiResponse<Unit>>

    // Driver Operations
    @POST("api/driver/status.php")
    suspend fun updateDriverStatus(@Body request: DriverStatusRequest): Response<ApiResponse<VehicleData>>

    @POST("api/driver/location.php")
    suspend fun updateDriverLocation(@Body request: DriverLocationRequest): Response<ApiResponse<VehicleData>>

    @POST("api/driver/vitals.php")
    suspend fun updatePatientVitals(@Body request: VitalsUpdateRequest): Response<ApiResponse<VitalsUpdateRequest>>

    // User Notifications
    @GET("api/user/notifications.php")
    suspend fun getNotifications(): Response<ApiResponse<List<NotificationItem>>>

    @PUT("api/user/notifications.php")
    suspend fun markNotificationsRead(@Body request: NotificationReadRequest): Response<ApiResponse<Unit>>

    // Request History
    @GET("api/requests/history.php")
    suspend fun getRequestsHistory(): Response<ApiResponse<List<RequestHistoryItem>>>

    // Hospitals Info
    @GET("api/hospitals/list.php")
    suspend fun getHospitals(): Response<ApiResponse<List<HospitalItem>>>

    // Admin Hospital Operations
    @POST("api/admin/hospitals.php")
    suspend fun adminCreateHospital(@Body request: HospitalItem): Response<ApiResponse<HospitalItem>>

    @PUT("api/admin/hospitals.php")
    suspend fun adminUpdateHospital(@Body request: HospitalItem): Response<ApiResponse<Unit>>

    @DELETE("api/admin/hospitals.php")
    suspend fun adminDeleteHospital(@Query("id") id: Int): Response<ApiResponse<Unit>>
}
