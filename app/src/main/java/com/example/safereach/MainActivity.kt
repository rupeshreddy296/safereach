package com.example.safereach

/**
 * PROJECT PATHS SUMMARY:
 * 
 * FRONTEND (Android Studio):
 * Path: C:\Users\rupes\AndroidStudioProjects\SAFEREACH3
 * Main Code: app\src\main\java\com\example\safereach\MainActivity.kt
 * API Config: app\src\main\java\com\example\safereach\network\RetrofitClient.kt
 * 
 * BACKEND (XAMPP Server):
 * Path: C:\xampp\htdocs\safereach
 * API Folder: C:\xampp\htdocs\safereach\api
 * Auth API: api\auth\login.php, api\auth\register.php
 * 
 * DATABASE:
 * Access: http://localhost/phpmyadmin
 * Server Address for Emulator: http://10.0.2.2/safereach/
 */

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safereach.ui.theme.DeepBlue
import com.example.safereach.ui.theme.AccentRed
import com.example.safereach.ui.theme.LightBg
import com.example.safereach.ui.theme.SAFEREACHTheme

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.safereach.viewmodel.*
import com.example.safereach.network.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAFEREACHTheme {
                MainApp()
            }
        }
    }
}

enum class UserRole {
    USER, DRIVER, ADMIN
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableIntStateOf(0) }
    var userRole by remember { mutableStateOf(UserRole.USER) }
    var selectedRequestId by remember { mutableIntStateOf(-1) }
    
    val authViewModel: AuthViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    // Handle Auth State changes
    val authState by authViewModel.authState
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val user = (authState as AuthState.Success).user
            userRole = when (user.role.uppercase()) {
                "DRIVER" -> UserRole.DRIVER
                "ADMIN" -> UserRole.ADMIN
                else -> UserRole.USER
            }
            currentScreen = when (userRole) {
                UserRole.USER -> 6
                UserRole.DRIVER -> 7
                UserRole.ADMIN -> 8
            }
        } else if (authState is AuthState.RegistrationSuccess) {
            // After signup, go to login page as requested
            authViewModel.resetState()
            currentScreen = 4
        }
    }

    when (currentScreen) {
        0 -> WelcomeScreen(onGetStarted = { currentScreen = 1 })
        1 -> EmergencyOnboardingScreen(
            onNext = { currentScreen = 2 },
            onSkip = { currentScreen = 4 }
        )
        2 -> TrackingOnboardingScreen(
            onNext = { currentScreen = 3 },
            onBack = { currentScreen = 1 },
            onSkip = { currentScreen = 4 }
        )
        3 -> QuickResponseScreen(
            onGetStarted = { currentScreen = 4 },
            onBackToFeatures = { currentScreen = 1 }
        )
        4 -> LoginScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = 3 },
            onSignUpClick = { currentScreen = 5 },
            onForgotPasswordClick = { currentScreen = 50 }
        )
        5 -> SignUpScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = 4 },
            onLoginClick = { currentScreen = 4 }
        )
        // Add Forgot Password screens
        50 -> ForgotPasswordScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = 4 },
            onCodeSent = { currentScreen = 51 }
        )
        51 -> ResetPasswordScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = 50 },
            onSuccess = { currentScreen = 4 }
        )
        6 -> UserDashboard(
            userViewModel = userViewModel,
            onLogout = { 
                authViewModel.resetState()
                currentScreen = 22 
            },
            onRequestAmbulance = { currentScreen = 10 },
            onEditProfile = { currentScreen = 18 },
            onViewAddresses = { currentScreen = 19 },
            onViewNotifications = { currentScreen = 20 },
            onViewHospitals = { currentScreen = 42 },
            onViewHealthTips = { currentScreen = 43 },
            onViewContacts = { currentScreen = 44 },
            onViewRequestDetails = { id ->
                selectedRequestId = id
                currentScreen = 45 // New screen
            }
        )
        7 -> DriverHomeScreen(
            driverViewModel = driverViewModel,
            onLogout = { 
                authViewModel.resetState()
                currentScreen = 22 
            },
            onNewRequest = { currentScreen = 25 }
        )
        8 -> AdminDashboard(
            adminViewModel = adminViewModel,
            onLogout = { 
                authViewModel.resetState()
                currentScreen = 22 
            },
            onViewRequestDetails = { id ->
                selectedRequestId = id
                currentScreen = 39 
            },
            onViewUnitDetails = { currentScreen = 41 }
        )
        10 -> RequestAmbulanceScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 6 },
            onSendRequest = { currentScreen = 11 }
        )
        100 -> ScheduleAmbulanceScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 6 },
            onScheduled = { currentScreen = 6 }
        )
        11 -> SearchingAmbulanceScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 10 },
            onCancelRequest = { currentScreen = 10 },
            onAmbulanceFound = { currentScreen = 12 }
        )
        12 -> AmbulanceFoundScreen(
            onBack = { currentScreen = 11 },
            onTrackNow = { currentScreen = 13 }
        )
        13 -> LiveTrackingScreen(
            onBack = { currentScreen = 12 },
            onAmbulanceArrived = { currentScreen = 14 }
        )
        14 -> AmbulanceArrivedScreen(
            onBack = { currentScreen = 13 },
            onStartJourney = { currentScreen = 15 }
        )
        15 -> JourneyTrackingScreen(
            onBack = { currentScreen = 14 },
            onViewDetails = { currentScreen = 16 }
        )
        16 -> HospitalDetailsScreen(
            onBack = { currentScreen = 15 }
        )
        17 -> FeedbackScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 6 },
            onSubmit = { currentScreen = 6 }
        )
        18 -> EditProfileScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 6 },
            onSave = { currentScreen = 6 }
        )
        19 -> MyAddressesScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 6 }
        )
        20 -> NotificationsScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 6 }
        )
        21 -> TermsConditionsScreen(
            onBack = { currentScreen = 5 },
            onAccept = {
                currentScreen = when (userRole) {
                    UserRole.USER -> 6
                    UserRole.DRIVER -> 7
                    UserRole.ADMIN -> 8
                }
            }
        )
        22 -> LogoutConfirmationScreen(
            onConfirm = { 
                RetrofitClient.setToken(null)
                currentScreen = 4 
            },
            onCancel = { 
                currentScreen = when (userRole) {
                    UserRole.USER -> 6
                    UserRole.DRIVER -> 7
                    UserRole.ADMIN -> 8
                }
            }
        )
        23 -> DriverLoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = { currentScreen = 7 },
            onCitizenLogin = { currentScreen = 4 }
        )
        25 -> EmergencyRequestScreen(
            driverViewModel = driverViewModel,
            onAccept = { currentScreen = 26 },
            onDecline = { currentScreen = 7 }
        )
        26 -> DriverRequestAcceptedScreen(
            driverViewModel = driverViewModel,
            onBack = { currentScreen = 7 },
            onNavigate = { currentScreen = 27 }
        )
        27 -> DriverNavigationToPickupScreen(
            driverViewModel = driverViewModel,
            onBack = { currentScreen = 26 },
            onArrived = { currentScreen = 28 }
        )
        28 -> DriverArrivedAtPickupScreen(
            driverViewModel = driverViewModel,
            onBack = { currentScreen = 27 },
            onStartTrip = { currentScreen = 31 } // Go to Patient Details/Vitals
        )
        29 -> DriverPatientBoardedScreen(
            driverViewModel = driverViewModel,
            onBack = { currentScreen = 31 },
            onConfirmStart = { currentScreen = 30 }
        )
        30 -> DriverEnRouteToHospitalScreen(
            driverViewModel = driverViewModel,
            onBack = { currentScreen = 29 },
            onHospitalReached = { currentScreen = 32 }
        )
        31 -> DriverPatientVitalsScreen(
            driverViewModel = driverViewModel,
            onBack = { currentScreen = 28 },
            onStartTransit = { currentScreen = 29 }
        )
        32 -> DriverHospitalReachedScreen(
            driverViewModel = driverViewModel,
            onComplete = { currentScreen = 33 }
        )
        33 -> DriverTripSummaryScreen(
            driverViewModel = driverViewModel,
            onReturnHome = { currentScreen = 7 }
        )
        36 -> AdminLoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = { currentScreen = 8 },
            onBack = { currentScreen = 4 }
        )
        39 -> UnifiedRequestDetailsScreen(
            driverViewModel = driverViewModel,
            requestId = selectedRequestId,
            onBack = { currentScreen = 8 }
        )
        41 -> AdminUnitDetailsScreen(
            onBack = { currentScreen = 8 }
        )
        42 -> HospitalListScreen(
            userViewModel = userViewModel,
            onBack = { currentScreen = 6 }
        )
        43 -> HealthTipsScreen(
            onBack = { currentScreen = 6 }
        )
        44 -> EmergencyContactsScreen(
            onBack = { currentScreen = 6 }
        )
        45 -> UnifiedRequestDetailsScreen(
            driverViewModel = driverViewModel,
            requestId = selectedRequestId,
            onBack = { currentScreen = 6 }
        )
    }
}

@Composable
fun LogoutConfirmationScreen(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Logout", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration Card
                Card(
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF263238))) {
                        // Placeholder for Logout Illustration
                        Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.align(Alignment.Center).size(100.dp), tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Are you sure you want to logout?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "You will need to enter your credentials again to access your account and request emergency services.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = border(1.dp, Color.LightGray)
                ) {
                    Text("Cancel", color = DeepBlue, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    "Version 2.4.0 • RapidRescue Response System",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val profileState by userViewModel.profileState
    
    LaunchedEffect(Unit) {
        userViewModel.fetchProfile()
    }

    var fullName by remember { mutableStateOf("Sarah Jenkins") }
    var email by remember { mutableStateOf("sarah.jenkins@example.com") }
    var phoneNumber by remember { mutableStateOf("+1 (555) 000-1234") }
    var emergencyContact by remember { mutableStateOf("Mark Jenkins (Husband)") }
    var emergencyPhone by remember { mutableStateOf("+1 (555) 000-5678") }

    LaunchedEffect(profileState) {
        if (profileState is UserUiState.Success) {
            val profile = (profileState as UserUiState.Success).data
            fullName = profile.fullName
            email = profile.email
            phoneNumber = profile.phoneNumber
            emergencyContact = profile.emergencyContactName ?: ""
            emergencyPhone = profile.emergencyContactPhone ?: ""
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                Text("Edit Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Section
                Box(modifier = Modifier.size(120.dp)) {
                    Surface(
                        modifier = Modifier.size(100.dp).align(Alignment.Center),
                        shape = CircleShape,
                        color = Color.LightGray,
                        border = border(2.dp, Color.White)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.White)
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-8).dp, y = (-8).dp).size(36.dp).clip(CircleShape).background(DeepBlue)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    }
                }
                Text("Tap to change avatar", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

                Spacer(modifier = Modifier.height(32.dp))

                // Input Fields
                ProfileTextField(label = "Full Name", value = fullName, onValueChange = { fullName = it }, icon = Icons.Default.Person)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTextField(label = "Email Address", value = email, onValueChange = { email = it }, icon = Icons.Default.Email)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTextField(label = "Phone Number", value = phoneNumber, onValueChange = { phoneNumber = it }, icon = Icons.Default.Phone)

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(24.dp))

                // Emergency Section
                Text("Emergency Context", modifier = Modifier.fillMaxWidth(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Text("This person will be notified during emergency requests.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(20.dp))
                ProfileTextField(label = "Primary Emergency Contact", value = emergencyContact, onValueChange = { emergencyContact = it }, icon = Icons.Default.ContactPage)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTextField(label = "Emergency Phone", value = emergencyPhone, onValueChange = { emergencyPhone = it }, icon = Icons.Default.Phone)

                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = {
                        userViewModel.updateProfile(
                            ProfileUpdateRequest(
                                fullName = fullName,
                                email = email,
                                emergencyContactName = emergencyContact,
                                emergencyContactPhone = emergencyPhone
                            )
                        )
                        onSave()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun MyAddressesScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val addressesState by userViewModel.addressesState
    
    LaunchedEffect(Unit) {
        userViewModel.fetchAddresses()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                Text("My Addresses", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())
            ) {
                Text("Saved Locations", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Text("Manage your frequently used addresses for faster emergency response dispatch.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                
                Spacer(modifier = Modifier.height(24.dp))

                if (addressesState is UserUiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepBlue)
                    }
                } else if (addressesState is UserUiState.Error) {
                    Text((addressesState as UserUiState.Error).message, color = Color.Red, modifier = Modifier.padding(16.dp))
                } else if (addressesState is UserUiState.Success) {
                    val addresses = (addressesState as UserUiState.Success).data
                    if (addresses.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No saved addresses", color = Color.Gray)
                        }
                    } else {
                        addresses.forEach { addr ->
                            AddressCard(
                                title = addr.title,
                                subtitle = addr.subtitle ?: "",
                                address = addr.address,
                                icon = when(addr.title.lowercase()) {
                                    "home" -> Icons.Default.Home
                                    "work" -> Icons.Default.Work
                                    else -> Icons.Default.LocationOn
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Add New Button
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    border = border(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                    onClick = { }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DeepBlue), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add New Address", color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Info, null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Saved addresses are encrypted and only used to improve response times during emergency requests. You can revoke access at any time in security settings.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AddressCard(title: String, subtitle: String, address: String, icon: androidx.compose.ui.graphics.vector.ImageVector, hasMap: Boolean = true) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(36.dp), color = Color(0xFFE8EAF6), shape = CircleShape) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(18.dp), tint = DeepBlue) }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Text(subtitle, fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = Color.Gray) }
                IconButton(onClick = { }) { Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp), tint = Color.Gray) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(address, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
            
            if (hasMap) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.align(Alignment.Center), tint = DeepBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val notificationsState by userViewModel.notificationsState
    
    LaunchedEffect(Unit) {
        userViewModel.fetchNotifications()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                    Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, null, tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
            ) {
                if (notificationsState is UserUiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepBlue)
                    }
                } else if (notificationsState is UserUiState.Error) {
                    Text((notificationsState as UserUiState.Error).message, color = Color.Red, modifier = Modifier.padding(16.dp))
                } else if (notificationsState is UserUiState.Success) {
                    val notifications = (notificationsState as UserUiState.Success).data
                    if (notifications.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No notifications", color = Color.Gray)
                        }
                    } else {
                        notifications.forEach { note ->
                            NotificationItem(
                                title = note.title,
                                desc = note.description,
                                time = note.createdAt,
                                icon = when(note.type.lowercase()) {
                                    "emergency" -> Icons.Default.MedicalServices
                                    "info" -> Icons.Default.Info
                                    else -> Icons.Default.Notifications
                                },
                                iconBg = Color(0xFFE3F2FD),
                                iconTint = DeepBlue
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun NotificationSection(label: String) {
    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
}

@Composable
fun NotificationItem(title: String, desc: String, time: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, iconTint: Color, action: String? = null, hasRating: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Surface(modifier = Modifier.size(40.dp), color = iconBg, shape = CircleShape) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = iconTint) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Text(time, fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(desc, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                
                if (hasRating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(5) { Icon(Icons.Default.StarBorder, null, modifier = Modifier.size(16.dp), tint = Color.LightGray) }
                    }
                }
                
                if (action != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { }, contentPadding = PaddingValues(0.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(action, color = if(action == "Update Profile") AccentRed else DeepBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(if(action == "Update Profile") Icons.Default.Edit else Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = if(action == "Update Profile") AccentRed else DeepBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermsConditionsScreen(onBack: () -> Unit, onAccept: () -> Unit) {
    var accepted by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Text("Terms & Conditions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Text("Last Updated: October 24, 2023", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = border(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                        Text(
                            text = "1. Acceptance of Terms\n\nBy accessing and using the RapidRescue platform, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions. RapidRescue provides emergency response coordination and logistical support. These services are intended to facilitate rapid medical assistance and should not be used in lieu of calling local emergency numbers where RapidRescue is not officially supported.\n\n2. Privacy Policy\n\nYour privacy is critical. We collect location data and medical information only when strictly necessary to provide emergency services. Data is encrypted end-to-end. Please review our full Privacy Policy to understand how we manage your information.\n\n3. User Conduct\n\nYou agree to provide accurate information and not to misuse the emergency services. False requests are strictly prohibited and may result in account termination or legal action.",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = accepted, onCheckedChange = { accepted = it }, colors = CheckboxDefaults.colors(checkedColor = DeepBlue))
                    Text("I have read and agree to all terms", fontSize = 13.sp, color = Color.DarkGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = accepted
                ) {
                    Text("Accept and Continue", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfilePreview() {
    SAFEREACHTheme {
        EditProfileScreen(userViewModel = viewModel(), onBack = {}, onSave = {})
    }
}

@Preview(showBackground = true)
@Composable
fun MyAddressesPreview() {
    SAFEREACHTheme {
        MyAddressesScreen(userViewModel = viewModel(), onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsPreview() {
    SAFEREACHTheme {
        NotificationsScreen(userViewModel = viewModel(), onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun TermsPreview() {
    SAFEREACHTheme {
        TermsConditionsScreen(onBack = {}, onAccept = {})
    }
}

@Preview(showBackground = true)
@Composable
fun LogoutPreview() {
    SAFEREACHTheme {
        LogoutConfirmationScreen(onConfirm = {}, onCancel = {})
    }
}


@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.White).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(DeepBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Medical", modifier = Modifier.size(30.dp), tint = Color.White)
                    }
                }
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-20).dp).size(45.dp)
                        .clip(RoundedCornerShape(8.dp)).background(AccentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "*", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "RapidRescue", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DeepBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Fast Help, Save Lives", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                DotIndicator(selected = true)
                DotIndicator(selected = false)
                DotIndicator(selected = false)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = "Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Initializing emergency services...", fontSize = 14.sp, color = Color.Gray.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmergencyOnboardingScreen(onNext: () -> Unit, onSkip: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PillIndicator(selected = true)
                DotIndicator(selected = false)
                DotIndicator(selected = false)
            }
            Spacer(modifier = Modifier.height(48.dp))
            AmbulanceIllustration()
            Spacer(modifier = Modifier.height(48.dp))
            Text(text = "Emergency Help,\nAnytime Anywhere", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DeepBlue, textAlign = TextAlign.Center, lineHeight = 34.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Connect with rapid medical assistance in seconds. Our intelligent system ensures the nearest ambulance reaches you instantly.", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onSkip) { Text(text = "Skip", color = Color.Gray, fontSize = 16.sp) }
                Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = DeepBlue), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(56.dp).width(130.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TrackingOnboardingScreen(onNext: () -> Unit, onBack: () -> Unit, onSkip: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) }
                Text(text = "RapidRescue", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = DeepBlue) }
            }
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Card(modifier = Modifier.fillMaxWidth().height(320.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0)).padding(16.dp)) {
                            repeat(5) { Box(modifier = Modifier.offset(x = (it * 40).dp, y = (it * 50).dp).size(100.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.2f))) }
                        }
                        Card(modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(DeepBlue), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ETA", fontSize = 12.sp, color = Color.Gray)
                                    Text("4 Mins", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                }
                            }
                        }
                        Card(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Live tracking active", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                                }
                                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "Real-time Tracking", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Know exactly where your ambulance is and when it will reach you with hyper-accurate live tracking.", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 24.sp)
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    DotIndicator(selected = false)
                    PillIndicator(selected = true)
                    DotIndicator(selected = false)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onSkip, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp)) {
                        Text("Skip", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onNext, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepBlue), shape = RoundedCornerShape(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickResponseScreen(onGetStarted: () -> Unit, onBackToFeatures: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.size(240.dp), shape = RoundedCornerShape(32.dp), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.Center), tint = Color.White.copy(alpha = 0.5f))
                    }
                }
                Surface(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-10).dp), color = AccentRed, shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1-Tap Help", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Quick Response", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Immediate medical assistance is just one click away. Our intelligent system routes the nearest ambulance to your exact coordinates instantly.", fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 24.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                DotIndicator(selected = false)
                DotIndicator(selected = false)
                PillIndicator(selected = true)
            }
            Spacer(modifier = Modifier.weight(1f))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0F4F8)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DeepBlue), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Critical Speed", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepBlue)
                            Text("Average response time under 8 minutes", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepBlue), shape = RoundedCornerShape(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onBackToFeatures) { Text("Back to features", color = Color.Gray, fontSize = 14.sp) }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    
    val authState by authViewModel.authState

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) }
                Text(text = "RapidRescue", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = DeepBlue) }
            }
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    RoleChip(selected = selectedRole == UserRole.USER, label = "User", onClick = { selectedRole = UserRole.USER })
                    RoleChip(selected = selectedRole == UserRole.DRIVER, label = "Driver", onClick = { selectedRole = UserRole.DRIVER })
                    RoleChip(selected = selectedRole == UserRole.ADMIN, label = "Admin", onClick = { selectedRole = UserRole.ADMIN })
                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = when(selectedRole) {
                        UserRole.USER -> Icons.Default.Person
                        UserRole.DRIVER -> Icons.Default.LocalTaxi
                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                    }, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Welcome Back", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Login to continue your lifesaving journey.", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                
                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Email Address", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) }, placeholder = { Text("john@example.com", color = Color.LightGray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                        TextButton(onClick = onForgotPasswordClick, contentPadding = PaddingValues(0.dp)) { Text("Forgot Password?", fontSize = 12.sp, color = DeepBlue) }
                    }
                    OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Gray) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { authViewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Login as ${selectedRole.name}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                    Text(text = " OR LOGIN WITH ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp), border = border(1.dp, Color.LightGray.copy(alpha = 0.5f))) { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Gray) }
                    OutlinedButton(onClick = { }, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp), border = border(1.dp, Color.LightGray.copy(alpha = 0.5f))) { Icon(Icons.Default.Face, contentDescription = null, tint = Color.Gray) }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Don't have an account? ", color = Color.Gray, fontSize = 14.sp)
                    TextButton(onClick = onSignUpClick, contentPadding = PaddingValues(0.dp)) { Text("Sign Up", color = DeepBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth().height(160.dp).padding(bottom = 24.dp), shape = RoundedCornerShape(16.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                        Row(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SECURE & RELIABLE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onCodeSent: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    val authState by authViewModel.authState
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.ForgotPasswordSent) {
            onCodeSent()
            authViewModel.resetState()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) }
                Text(text = "Forgot Password", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Enter your registered phone number below to receive a verification code.", fontSize = 14.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))

                val displayedError = localError ?: if (authState is AuthState.Error) (authState as AuthState.Error).message else null
                if (displayedError != null) {
                    Text(text = displayedError, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                }

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it; localError = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                    placeholder = { Text("e.g. +919999999999", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (phoneNumber.isBlank()) {
                            localError = "Phone number is required"
                        } else {
                            authViewModel.forgotPassword(phoneNumber)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Verification Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val authState by authViewModel.authState
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.ResetPasswordSuccess) {
            onSuccess()
            authViewModel.resetState()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) }
                Text(text = "Reset Password", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Enter your phone number, the verification code sent to your device, and a secure new password.", fontSize = 14.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))

                val displayedError = localError ?: if (authState is AuthState.Error) (authState as AuthState.Error).message else null
                if (displayedError != null) {
                    Text(text = displayedError, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                }

                Text(text = "Phone Number", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it; localError = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
                    placeholder = { Text("e.g. +919999999999", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Verification Code (OTP)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it; localError = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                    placeholder = { Text("6-digit code", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "New Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; localError = null },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
                    placeholder = { Text("Minimum 6 characters", color = Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (phoneNumber.isBlank() || otpCode.isBlank() || newPassword.isBlank()) {
                            localError = "All fields are required"
                        } else if (newPassword.length < 6) {
                            localError = "Password must be at least 6 characters"
                        } else {
                            authViewModel.resetPassword(phoneNumber, otpCode, newPassword)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoginClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var localError by remember { mutableStateOf<String?>(null) }
    var vehicleNumber by remember { mutableStateOf("") }
    var ambulanceType by remember { mutableStateOf("BLS") }
    
    val authState by authViewModel.authState

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) }
                Text(text = "Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(48.dp))
            }
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    RoleChip(selected = selectedRole == UserRole.USER, label = "User", onClick = { selectedRole = UserRole.USER })
                    RoleChip(selected = selectedRole == UserRole.DRIVER, label = "Driver", onClick = { selectedRole = UserRole.DRIVER })
                    RoleChip(selected = selectedRole == UserRole.ADMIN, label = "Admin", onClick = { selectedRole = UserRole.ADMIN })
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(DeepBlue), contentAlignment = Alignment.Center) { Text(text = "*", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "RapidRescue", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Join our network for clinical-grade emergency assistance, anytime and anywhere.", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(24.dp))
                
                val displayedError = localError ?: if (authState is AuthState.Error) (authState as AuthState.Error).message else null
                if (displayedError != null) {
                    Text(
                        text = displayedError,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Full Name", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it; localError = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) }, placeholder = { Text("John Doe", color = Color.LightGray) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Email", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it; localError = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) }, placeholder = { Text("john@example.com", color = Color.LightGray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(value = password, onValueChange = { password = it; localError = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Gray) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Confirm Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; localError = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) }, trailingIcon = { IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = Color.Gray) } }, visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)))
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (selectedRole == UserRole.DRIVER) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Vehicle Plate Number", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(value = vehicleNumber, onValueChange = { vehicleNumber = it; localError = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.LocalTaxi, contentDescription = null, tint = Color.Gray) }, placeholder = { Text("e.g. TN-07-BY-1234", color = Color.LightGray) }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepBlue, unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Ambulance Care Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BLS", "ALS", "CCU").forEach { type ->
                                val active = ambulanceType == type
                                Button(
                                    onClick = { ambulanceType = type },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (active) DeepBlue else Color(0xFFF3F4F6)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(type, color = if (active) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = agreeToTerms, onCheckedChange = { agreeToTerms = it }, colors = CheckboxDefaults.colors(checkedColor = DeepBlue))
                    Text(text = "I agree to the Terms of Service and Privacy Policy.", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { 
                        if (password != confirmPassword) {
                            localError = "Passwords do not match"
                        } else if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                            localError = "All fields are required"
                        } else if (selectedRole == UserRole.DRIVER && vehicleNumber.isBlank()) {
                            localError = "Vehicle plate number is required for drivers"
                        } else {
                            authViewModel.register(
                                fullName, 
                                email, 
                                password, 
                                selectedRole, 
                                if (selectedRole == UserRole.DRIVER) vehicleNumber else null, 
                                if (selectedRole == UserRole.DRIVER) ambulanceType else null
                            ) 
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = agreeToTerms && authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Sign Up as ${selectedRole.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 32.dp)) {
                    Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                    TextButton(onClick = onLoginClick, contentPadding = PaddingValues(0.dp)) { Text("Login", color = DeepBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun RoleChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp, if (selected) DeepBlue else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        color = if (selected) DeepBlue else Color.Transparent,
        onClick = onClick
    ) {
        Text(text = label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (selected) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun UserDashboard(
    userViewModel: UserViewModel,
    onLogout: () -> Unit,
    onRequestAmbulance: () -> Unit,
    onEditProfile: () -> Unit,
    onViewAddresses: () -> Unit,
    onViewNotifications: () -> Unit,
    onViewHospitals: () -> Unit,
    onViewHealthTips: () -> Unit,
    onViewContacts: () -> Unit,
    onViewRequestDetails: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepBlue,
                        indicatorColor = DeepBlue
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Requests") },
                    label = { Text("Requests") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepBlue,
                        indicatorColor = DeepBlue
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = DeepBlue,
                        indicatorColor = DeepBlue
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> UserHomeScreen(
                    userViewModel = userViewModel, 
                    onRequestAmbulance = onRequestAmbulance, 
                    onLogout = onLogout, 
                    onViewNotifications = onViewNotifications,
                    onTabChange = { selectedTab = it },
                    onViewHospitals = onViewHospitals,
                    onViewHealthTips = onViewHealthTips,
                    onViewContacts = onViewContacts
                )
                1 -> MyRequestsScreen(userViewModel = userViewModel, onViewDetails = onViewRequestDetails)
                2 -> UserProfileScreen(userViewModel = userViewModel, onLogout = onLogout, onEditProfile = onEditProfile, onViewAddresses = onViewAddresses, onViewNotifications = onViewNotifications)
            }
        }
    }
}

@Composable
 fun UserHomeScreen(
    userViewModel: UserViewModel,
    onRequestAmbulance: () -> Unit,
    onLogout: () -> Unit,
    onViewNotifications: () -> Unit,
    onTabChange: (Int) -> Unit,
    onViewHospitals: () -> Unit,
    onViewHealthTips: () -> Unit,
    onViewContacts: () -> Unit
) {
    val profileState by userViewModel.profileState
    
    LaunchedEffect(Unit) {
        userViewModel.fetchProfile()
    }
    
    val userName = when (profileState) {
        is UserUiState.Success -> (profileState as UserUiState.Success<UserProfile>).data.fullName
        else -> "Rahul"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RapidRescue",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
                IconButton(onClick = onViewNotifications) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = DeepBlue)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(text = "Hi, $userName", fontSize = 16.sp, color = Color.Gray)
                Text(text = "How can we help you today?", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                
                Spacer(modifier = Modifier.height(24.dp))

                // Request Ambulance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    onClick = onRequestAmbulance
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "*", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "REQUEST AMBULANCE",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Immediate medical assistance in 1-click",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Features Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = "My Requests",
                        subtitle = "Track your history",
                        iconBg = Color(0xFFE8EAF6),
                        iconColor = Color(0xFF3F51B5),
                        onClick = { onTabChange(1) }
                    )
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VerifiedUser,
                        title = "Health Tips",
                        subtitle = "Stay safe & informed",
                        iconBg = Color(0xFFE8F5E9),
                        iconColor = Color(0xFF4CAF50),
                        onClick = onViewHealthTips
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocalHospital,
                        title = "Hospitals",
                        subtitle = "Nearby facilities",
                        iconBg = Color(0xFFFFEBEE),
                        iconColor = Color(0xFFE91E63),
                        onClick = onViewHospitals
                    )
                    FeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Contacts,
                        title = "Emergency Contacts",
                        subtitle = "Police, Fire, SOS",
                        iconBg = Color(0xFFF5F5F5),
                        iconColor = Color(0xFF616161),
                        onClick = onViewContacts
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = border(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Current Location", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "MG Road, Bangalore, KA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        }
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                    }
                }
                
                // Logout button for testing
                TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Logout", color = AccentRed)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MyRequestsScreen(userViewModel: UserViewModel, onViewDetails: (Int) -> Unit) {
    var selectedFilter by remember { mutableStateOf("All") }
    
    val historyState by userViewModel.historyState
    
    LaunchedEffect(Unit) {
        userViewModel.fetchHistory()
    }
    
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { }, modifier = Modifier.size(24.dp)) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) 
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "My Requests", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = DeepBlue)
                }
            }

            // Filters
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Completed", "Cancelled").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)),
                        color = if (isSelected) DeepBlue else Color.White,
                        onClick = { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (historyState is UserUiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepBlue)
                    }
                } else if (historyState is UserUiState.Error) {
                    Text((historyState as UserUiState.Error).message, color = Color.Red, modifier = Modifier.padding(16.dp))
                } else if (historyState is UserUiState.Success) {
                    val requests = (historyState as UserUiState.Success).data
                    val filteredRequests = when (selectedFilter) {
                        "Completed" -> requests.filter { it.status.lowercase() == "completed" }
                        "Cancelled" -> requests.filter { it.status.lowercase() == "cancelled" }
                        else -> requests
                    }

                    if (filteredRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No requests found", color = Color.Gray)
                        }
                    } else {
                        filteredRequests.forEach { req ->
                            RequestHistoryItem(
                                hospital = req.hospitalName ?: req.emergencyType,
                                status = req.status.uppercase(),
                                time = req.createdAt,
                                location = req.pickupAddress,
                                reqId = "#RR-${req.requestId}",
                                isCancelled = req.status.lowercase() == "cancelled",
                                onClick = { onViewDetails(req.requestId) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bottom Quick Tip (from image 2)
                Card(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Text("Quick Tip", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Keep your medical history updated for faster\nemergency processing.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun RequestHistoryItem(
    hospital: String, 
    status: String, 
    time: String, 
    location: String, 
    reqId: String, 
    isCancelled: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = border(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(hospital, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Surface(
                    color = if (isCancelled) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) AccentRed else Color(0xFF2E7D32)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(time, fontSize = 13.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(location, fontSize = 13.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Req ID: $reqId", fontSize = 12.sp, color = Color.Gray)
                TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if(isCancelled) "View Log" else "View Details", color = DeepBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = DeepBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    var review by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Refined Check Icon
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8EAF6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(40.dp), tint = DeepBlue)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Emergency Response Concluded", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            Text("Request #RR-2941 has been resolved.", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("How was your experience?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text("Your feedback helps us improve response\ntimes and care quality.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        repeat(5) { index ->
                            IconButton(onClick = { rating = index + 1 }, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = null,
                                    tint = if (index < rating) Color(0xFFFFB300) else Color.LightGray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Details", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = review,
                            onValueChange = { review = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            placeholder = { Text("Write a review (optional)", color = Color.LightGray) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Submit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Text("RATE EXPERIENCE", color = Color.LightGray.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))
        }
    }
}

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun UserProfileScreen(
    userViewModel: UserViewModel,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onViewAddresses: () -> Unit,
    onViewNotifications: () -> Unit
) {
    val profileState by userViewModel.profileState
    
    LaunchedEffect(Unit) {
        userViewModel.fetchProfile()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                Text("Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(modifier = Modifier.size(100.dp)) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray)) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(60.dp), tint = Color.White)
                    }
                    IconButton(
                        onClick = onEditProfile,
                        modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).clip(CircleShape).background(DeepBlue)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rahul Sharma", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Text("Emergency Priority Account", fontSize = 14.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(32.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileInfoItem(icon = Icons.Default.Badge, label = "Full Name", value = "Rahul Sharma")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileInfoItem(icon = Icons.Default.Phone, label = "Phone Number", value = "+91 9876543210", isVerified = true)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
                        ProfileInfoItem(icon = Icons.Default.Email, label = "Email Address", value = "rahul@example.com")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Preferences & Details", modifier = Modifier.fillMaxWidth(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Spacer(modifier = Modifier.height(12.dp))

                // Actions List
                ProfileListItem(icon = Icons.Default.Payments, title = "Payment Methods", onClick = { })
                ProfileListItem(icon = Icons.Default.LocationOn, title = "Saved Addresses", onClick = onViewAddresses)
                ProfileListItem(icon = Icons.Default.Settings, title = "Notification Settings", onClick = onViewNotifications)
                ProfileListItem(icon = Icons.AutoMirrored.Filled.HelpCenter, title = "Help & Support", onClick = { })

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFFFEBEE).copy(alpha = 0.5f)),
                    border = border(1.dp, Color(0xFFFFCDD2))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = AccentRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LOGOUT", color = AccentRed, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, isVerified: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(40.dp), color = Color(0xFFE8EAF6), shape = RoundedCornerShape(8.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(20.dp), tint = DeepBlue) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
        if (isVerified) {
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                Text("VERIFIED", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun ProfileListItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = DeepBlue)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(20.dp), tint = Color.LightGray)
        }
    }
}

@Composable
fun DriverLoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onCitizenLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState
    
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("RapidRescue", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            Text("DRIVER PORTAL", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray, letterSpacing = 1.sp)
            
            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Staff Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, null, modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SECURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                    Text("Please enter your credentials to begin duty.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Driver Email", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("driver@safereach.com", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text("Forgot Password?", fontSize = 12.sp, color = DeepBlue, fontWeight = FontWeight.Bold)
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("••••••••", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it }, colors = CheckboxDefaults.colors(checkedColor = DeepBlue))
                        Text("Remember my credentials on this device", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = { authViewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(8.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Secure Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFF1F0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(AccentRed), contentAlignment = Alignment.Center) {
                                Text("!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Report issue with terminal", color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("Not authorized staff? ", fontSize = 13.sp, color = Color.Gray)
                        Text("Citizen Login", fontSize = 13.sp, color = DeepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onCitizenLogin() })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Access to this system is restricted to authorized emergency personnel. All login attempts and session activities are logged for auditing purposes.",
                fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 16.sp, modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "© 2024 RapidRescue Logistics Inc. • Security Compliance\nISO 27001",
                fontSize = 10.sp, color = Color.LightGray, textAlign = TextAlign.Center, lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun DriverHomeScreen(
    driverViewModel: DriverViewModel,
    onLogout: () -> Unit,
    onNewRequest: () -> Unit
) {
    val statusState by driverViewModel.statusState
    var isOnline by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentLat by remember { mutableDoubleStateOf(12.9716) }
    var currentLng by remember { mutableDoubleStateOf(77.5946) }

    // Periodically update location when online with simulated progression towards active dispatch target
    LaunchedEffect(isOnline) {
        if (isOnline) {
            while (true) {
                val trackingState = driverViewModel.trackingState.value
                if (trackingState is DriverUiState.Success) {
                    val trackingData = trackingState.data
                    val status = trackingData.status.lowercase()
                    val targetLat: Double
                    val targetLng: Double
                    
                    if (status == "accepted" || status == "arrived") {
                        targetLat = trackingData.pickup.latitude
                        targetLng = trackingData.pickup.longitude
                    } else if (status == "boarded" && trackingData.destinationHospital != null) {
                        targetLat = trackingData.destinationHospital.latitude
                        targetLng = trackingData.destinationHospital.longitude
                    } else {
                        targetLat = 12.9716
                        targetLng = 77.5946
                    }

                    // Linear interpolation (15% closer every step)
                    currentLat += (targetLat - currentLat) * 0.15
                    currentLng += (targetLng - currentLng) * 0.15
                }
                
                driverViewModel.updateLocation(currentLat, currentLng)
                kotlinx.coroutines.delay(5000) // Every 5 seconds
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = DeepBlue))
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) }, label = { Text("History") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("RapidRescue", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(20.dp)) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("ON", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isOnline) DeepBlue else Color.Gray)
                                            Switch(checked = isOnline, onCheckedChange = {
                                                isOnline = it
                                                driverViewModel.updateStatus(it)
                                            }, modifier = Modifier.scale(0.6f), colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DeepBlue))
                                            Text("OFF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(!isOnline) DeepBlue else Color.Gray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue)
                                }
                            }

                            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Welcome back,", fontSize = 14.sp, color = Color.Gray)
                                        Text("Officer Rodriguez", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                    }
                                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                                        Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(30.dp), tint = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    DriverStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Star, label = "Today's Trips", value = "12", iconColor = DeepBlue)
                                    DriverStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Schedule, label = "Hours Online", value = "6.5h", iconColor = Color(0xFF2E7D32))
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DeepBlue)) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text("User Rating", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                            Text("4.92", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                // Map Section
                                Card(modifier = Modifier.fillMaxWidth().height(260.dp), shape = RoundedCornerShape(24.dp)) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB)))
                                        Box(modifier = Modifier.align(Alignment.Center).size(200.dp).border(1.dp, DeepBlue.copy(alpha = 0.3f), CircleShape))
                                        Box(modifier = Modifier.align(Alignment.Center).size(48.dp).clip(CircleShape).background(DeepBlue).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.LocalTaxi, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                        Card(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Searching for requests...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                                    Text("Stay within high-demand zones for faster dispatch.", fontSize = 11.sp, color = Color.Gray)
                                                }
                                                Button(
                                                    onClick = onNewRequest,
                                                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("VIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text("Nearby Support", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SupportChip(icon = Icons.Default.LocalHospital, label = "Central Gen: 4m away")
                                    SupportChip(icon = Icons.Default.LocalFireDepartment, label = "Station 12")
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
                1 -> DriverTripHistoryScreen()
                2 -> DriverProfileScreenExtended(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun DriverPatientVitalsScreen(
    driverViewModel: DriverViewModel,
    onBack: () -> Unit,
    onStartTransit: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                    Text("Patient Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue) }
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("*", color = AccentRed, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ARRIVED • ACTIVE\nRESCUE", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)
                        }
                        Text("ETA: 12 mins to\nMedicity", color = AccentRed, fontSize = 11.sp, textAlign = TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Suresh Kumar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Row(modifier = Modifier.padding(top = 4.dp)) {
                                    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) { Text("45 Years", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = Color.Gray) }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) { Text("Male", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = Color.Gray) }
                                }
                            }
                            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray)) { Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(40.dp), tint = Color.White) }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(color = Color(0xFFFFF1F0), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFFFFCDD2))) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Warning, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Condition: Chest Pain - Critical", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentRed)
                                    Text("Patient reports sudden sharp pain radiating to left arm. Breathing is labored. History of hypertension.", fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("LIVE VITALS MONITOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentRed))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    VitalCard(modifier = Modifier.weight(1f), label = "BPM", value = "115", status = "Elevated", icon = Icons.Default.Favorite, iconColor = AccentRed)
                    VitalCard(modifier = Modifier.weight(1f), label = "mmHg", value = "145/95", status = "Stage 2 High", icon = Icons.Default.Speed, iconColor = DeepBlue)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    VitalCard(modifier = Modifier.weight(1f), label = "SpO2", value = "94%", status = "Marginal", icon = Icons.Default.Air, iconColor = Color(0xFF2E7D32))
                    VitalCard(modifier = Modifier.weight(1f), label = "°F", value = "98.6", status = "Normal", icon = Icons.Default.DeviceThermostat, iconColor = Color(0xFFF97316))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)), shape = RoundedCornerShape(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Phone, null, tint = Color.DarkGray, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Contact", color = Color.DarkGray, fontSize = 13.sp) }
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)), shape = RoundedCornerShape(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.History, null, tint = Color.DarkGray, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Records", color = Color.DarkGray, fontSize = 13.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(DeepBlue), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocalHospital, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DESTINATION HOSPITAL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Medicity Heart & Trauma Center", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onStartTransit, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentRed), shape = RoundedCornerShape(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NearMe, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("START TRANSIT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun VitalCard(modifier: Modifier = Modifier, label: String, value: String, status: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Text(label, fontSize = 11.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Text(status, fontSize = 10.sp, color = if(status == "Normal") Color(0xFF2E7D32) else AccentRed)
        }
    }
}

@Composable
fun DriverHospitalReachedScreen(
    driverViewModel: DriverViewModel,
    onComplete: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color(0xFFE8F5E9)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(40.dp)) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("City Hospital Reached", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Text("The emergency transport has arrived at the\ndestination. Please hand over the patient safely.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.Schedule, label = "Duration", value = "12 mins")
                InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.LocationOn, label = "Distance", value = "5.1 km")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Card(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(160.dp), shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) {
                    Surface(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), color = DeepBlue, shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Emergency Bay 4", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onComplete, modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepBlue), shape = RoundedCornerShape(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Complete Trip", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.LightGray)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, tint = DeepBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Call Hospital Reception", color = DeepBlue)
                }
            }
        }
    }
}

@Composable
fun DriverTripSummaryScreen(
    driverViewModel: DriverViewModel,
    onReturnHome: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue) }
            }

            Box(modifier = Modifier.padding(24.dp).fillMaxWidth().background(DeepBlue, RoundedCornerShape(16.dp)).padding(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFFE8F5E9).copy(alpha = 0.2f)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Trip Successful", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Excellent work, Responder. The patient has\nbeen safely admitted.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                }
            }

            Card(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(200.dp), shape = RoundedCornerShape(12.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF263238))) {
                    Surface(modifier = Modifier.padding(12.dp), color = Color.White, shape = RoundedCornerShape(4.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Route Completed", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("EFFICIENCY SCORE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("98%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DeepBlue)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Time Target:", fontSize = 10.sp, color = Color.Gray)
                        Text("16m / 18m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(progress = { 0.98f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color(0xFF2E7D32), trackColor = Color(0xFFF3F4F6))

                Spacer(modifier = Modifier.height(32.dp))
                Text("Emergency Logistics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))
                LogisticItem(icon = Icons.Default.LocationOn, label = "Pickup Location", value = "MG Road, Central Business District", color = DeepBlue)
                Box(modifier = Modifier.padding(start = 11.dp).height(20.dp).width(1.dp).background(Color.LightGray))
                LogisticItem(icon = Icons.Default.LocalHospital, label = "Destination", value = "City General Hospital, Trauma Care", color = AccentRed)

                Spacer(modifier = Modifier.height(32.dp))
                Text("Timestamp Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimeBox(modifier = Modifier.weight(1f), icon = Icons.Default.PlayCircleOutline, label = "Start Time", value = "10:24 AM")
                    TimeBox(modifier = Modifier.weight(1f), icon = Icons.Default.StopCircle, label = "End Time", value = "10:40 AM")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Duration", fontSize = 13.sp, color = Color.Gray)
                        Text("16m 04s", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onReturnHome, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepBlue), shape = RoundedCornerShape(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dashboard, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun LogisticItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
    }
}

@Composable
fun TimeBox(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(modifier = modifier, color = Color(0xFFF3F4F6), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = DeepBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
        }
    }
}

@Composable
fun DriverTripHistoryScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                Text("Trip History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue) }
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoBox(modifier = Modifier.weight(1f), icon = Icons.AutoMirrored.Filled.Assignment, label = "Total Trips", value = "124")
                    InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.Route, label = "Dist. Covered", value = "1,842 km")
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("TODAY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                HistoryTripCard("Sarah Johnson", "Completed", "10:45 AM", "8.4 km")
                HistoryTripCard("Michael Chen", "Completed", "08:12 AM", "12.1 km")

                Spacer(modifier = Modifier.height(32.dp))
                Text("YESTERDAY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                HistoryTripCard("Emily Rodriguez", "Completed", "05:50 PM", "4.2 km")
                HistoryTripCard("David Miller", "Completed", "02:15 PM", "15.8 km")
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HistoryTripCard(name: String, status: String, time: String, dist: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)) { Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center), tint = Color.White) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(status, fontSize = 12.sp, color = Color.Gray)
                    Text(" • $time", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(dist, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun DriverProfileScreenExtended(onLogout: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue) }
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(100.dp)) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray)) { Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(60.dp), tint = Color.White) }
                    IconButton(onClick = {}, modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).clip(CircleShape).background(DeepBlue)) { Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(14.dp), tint = Color.White) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Officer Rodriguez", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Text(" 4.92 Rating", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.Badge, label = "EMPLOYEE ID", value = "992144")
                    InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.LocalTaxi, label = "VEHICLE NO.", value = "KA01AB1234")
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("PERFORMANCE STATS", modifier = Modifier.fillMaxWidth(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Surface(modifier = Modifier.fillMaxWidth(), color = DeepBlue, shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Lives Saved", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text("1,248", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Add, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.Schedule, label = "Avg ETA", value = "6.4 mins")
                    InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.Route, label = "Distance Covered", value = "4,120 km")
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("APP SETTINGS", modifier = Modifier.fillMaxWidth(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                ProfileListItem(icon = Icons.Default.Notifications, title = "Notifications", onClick = { })
                ProfileListItem(icon = Icons.Default.Language, title = "Language Preference", onClick = { })
                ProfileListItem(icon = Icons.Default.Security, title = "Privacy & Security", onClick = { })

                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, AccentRed)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = AccentRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Logout", color = AccentRed, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DriverVitalsPreview() {
    SAFEREACHTheme { DriverPatientVitalsScreen(driverViewModel = viewModel(), {}, {}) }
}

@Preview(showBackground = true)
@Composable
fun HospitalReachedPreview() {
    SAFEREACHTheme { DriverHospitalReachedScreen(driverViewModel = viewModel()) {} }
}

@Preview(showBackground = true)
@Composable
fun TripSummaryPreview() {
    SAFEREACHTheme { DriverTripSummaryScreen(driverViewModel = viewModel()) {} }
}

@Preview(showBackground = true)
@Composable
fun TripHistoryPreview() {
    SAFEREACHTheme { DriverTripHistoryScreen() }
}

@Preview(showBackground = true)
@Composable
fun DriverProfilePreviewExtended() {
    SAFEREACHTheme { DriverProfileScreenExtended {} }
}

@Composable
fun DriverStatCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, iconColor: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
        }
    }
}

@Composable
fun SupportChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = DeepBlue, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun EmergencyRequestScreen(
    driverViewModel: DriverViewModel,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.5f)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Alert Header
                    Box(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFE57373)).padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                Text("*", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("New Emergency Request!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Condition Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFFF1F0),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CONDITION", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("Chest Pain", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentRed)
                                }
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE57373)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF7986CB), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Pickup Address", fontSize = 11.sp, color = Color.Gray)
                                Text("MG Road, Bangalore", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.AltRoute, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("2.4 km away from current position", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        // Timer
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(progress = { 0.3f }, modifier = Modifier.size(80.dp), color = Color(0xFFE57373), strokeWidth = 4.dp, trackColor = Color(0xFFFEE2E2))
                            Text("0", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Text("Seconds remaining to accept", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9FA8DA)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Accept Request", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = onDecline) {
                            Text("Decline", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverRequestAcceptedScreen(
    driverViewModel: DriverViewModel,
    onBack: () -> Unit,
    onNavigate: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                    Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Success Circle
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8EAF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(48.dp), tint = DeepBlue)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Request Accepted", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(
                    "You are now assigned to this emergency.\nProceed to the pickup location immediately.",
                    fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Patient Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = border(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("PATIENT INFORMATION", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(12.dp), tint = AccentRed)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CRITICAL", fontSize = 10.sp, color = AccentRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Suresh Kumar", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Condition", fontSize = 11.sp, color = Color.Gray)
                                Text("Chest Pain", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentRed)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Age / Gender", fontSize = 11.sp, color = Color.Gray)
                                Text("45 / Male", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = border(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(20.dp), tint = DeepBlue)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Your Location", fontSize = 11.sp, color = Color.Gray)
                                Text("MG Road, Ambulance Point 04", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Dashed line placeholder
                        Box(modifier = Modifier.padding(start = 10.dp).height(20.dp).width(1.dp).background(Color.LightGray))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(20.dp), tint = AccentRed)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Pickup Location", fontSize = 11.sp, color = Color.Gray)
                                Text("Koramangala 4th Block, 80ft Road", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("Approx. 2.4 km away (6 mins)", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Map Pattern Placeholder
                Card(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(16.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB))) {
                        // Simulated route pattern
                        repeat(10) { i ->
                            Box(modifier = Modifier.offset(x = (i * 40).dp).fillMaxHeight().width(0.5.dp).background(Color.White))
                            Box(modifier = Modifier.offset(y = (i * 30).dp).fillMaxWidth().height(0.5.dp).background(Color.White))
                        }
                        Box(modifier = Modifier.align(Alignment.Center).size(100.dp, 2.dp).background(Color(0xFF1E88E5)).offset(y = 20.dp))
                        
                        Surface(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), color = Color.White, shape = RoundedCornerShape(20.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Navigation, null, modifier = Modifier.size(14.dp), tint = DeepBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fastest Route Found", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNavigate,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Navigation, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Navigate to Pickup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Bottom Nav Bar Placeholder (Matching image)
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Home, null, tint = Color.Gray)
                        Text("Home", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(color = DeepBlue, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White) }
                        }
                        Text("Requests", fontSize = 10.sp, color = DeepBlue, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, null, tint = Color.Gray)
                        Text("Profile", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverNavigationToPickupScreen(
    driverViewModel: DriverViewModel,
    onBack: () -> Unit,
    onArrived: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF3F4F6)) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map Background (Mimicking Image 2 gradient/map)
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFB0BEC5))) {
                // Gradient or simplified map pattern
                repeat(20) { i ->
                    Box(modifier = Modifier.offset(x = (i * 30).dp).fillMaxHeight().width(0.5.dp).background(Color.White.copy(alpha = 0.2f)))
                    Box(modifier = Modifier.offset(y = (i * 30).dp).fillMaxWidth().height(0.5.dp).background(Color.White.copy(alpha = 0.2f)))
                }
                // Dotted Route Line
                // (Visual representation of route dots)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Nav
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                        Text("Emergency Navigation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Turn Header Card
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(DeepBlue), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(32.dp).scale(-1f)) // Turn left icon
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Next Turn in 500m", fontSize = 12.sp, color = Color.Gray)
                            Text("Turn left onto MG Road", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Map Controls (Floating)
                Column(modifier = Modifier.align(Alignment.End).padding(end = 16.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FloatingActionButton(onClick = {}, containerColor = Color.White, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(20.dp)) }
                    FloatingActionButton(onClick = {}, containerColor = Color.White, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Layers, null, modifier = Modifier.size(20.dp)) }
                    FloatingActionButton(onClick = {}, containerColor = Color.White, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp)) }
                    FloatingActionButton(onClick = {}, containerColor = Color.White, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(20.dp)) }
                }

                // Bottom Arrival Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("5 min", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("2.4 km • 10:45 ETA", fontSize = 13.sp, color = Color.Gray)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconButton(onClick = {}, modifier = Modifier.border(1.dp, Color.LightGray, CircleShape)) { Icon(Icons.Default.Phone, null, tint = DeepBlue) }
                                IconButton(onClick = {}, modifier = Modifier.border(1.dp, Color.LightGray, CircleShape)) { Icon(Icons.Default.Videocam, null, tint = DeepBlue) }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onArrived,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Arrived", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverArrivedAtPickupScreen(
    driverViewModel: DriverViewModel,
    onBack: () -> Unit,
    onStartTrip: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Nav
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                    Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Arrived Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFE8F5E9), shape = CircleShape, modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp)) }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text("You have arrived", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text("At MG Road, Bangalore pickup point", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Patient Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = border(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.LightGray)) {
                                // Placeholder for Suresh Kumar image
                                Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(40.dp), tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PATIENT NAME", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("Suresh Kumar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text("Emergency: Chest Pain", fontSize = 13.sp, color = Color.Gray)
                            }
                            IconButton(onClick = {}, modifier = Modifier.border(1.dp, Color.LightGray, CircleShape)) { Icon(Icons.Default.Phone, null, tint = Color.Gray) }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Patient on Board?", fontSize = 14.sp, color = Color.DarkGray)
                            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(20.dp)) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(DeepBlue))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Awaiting Confirmation", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onStartTrip,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Trip", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = border(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Text("Cancel Request", color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hospital Info Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalHospital, null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Nearest Hospital", fontSize = 10.sp, color = Color.Gray)
                            Text("City Hospital", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocationOn, null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("ETA to Hospital", fontSize = 10.sp, color = Color.Gray)
                            Text("12 mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DriverPatientBoardedScreen(
    driverViewModel: DriverViewModel,
    onBack: () -> Unit,
    onConfirmStart: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF3F4F6)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Nav
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp, start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                Text("Start Trip", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.padding(start = 16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text("RapidRescue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.padding(end = 16.dp))
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                // Map Image Placeholder
                Card(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(16.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB))) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.align(Alignment.Center).size(32.dp), tint = DeepBlue)
                    }
                }

                // Patient Boarded Badge
                Surface(
                    modifier = Modifier.offset(y = (-30).dp).padding(horizontal = 16.dp),
                    color = Color.White, shape = RoundedCornerShape(12.dp), shadowElevation = 4.dp
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = DeepBlue, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White) }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Patient Boarded", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text("Confirmed at 10:42 AM", fontSize = 11.sp, color = Color.Gray)
                        }
                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                            Text("READY", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = Color(0xFFE8F5E9)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp)) }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Patient on Board?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(
                    "Verify that the patient and all\nnecessary medical gear are secured\nbefore departure.",
                    fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Route Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = border(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("PICKUP LOCATION", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("MG Road, Bangalore", fontSize = 14.sp, color = Color.DarkGray)
                            }
                        }
                        Box(modifier = Modifier.padding(start = 10.dp).height(24.dp).width(1.dp).background(Color.LightGray))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocalHospital, null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("DESTINATION HOSPITAL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("City Hospital", fontSize = 14.sp, color = Color.DarkGray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Navigation, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("5.1 km • 12 mins via Residency Rd", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = AccentRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Proceed with caution. Sirens and emergency lights\nmust be activated for duty clearance.",
                        fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onConfirmStart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Trip", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = border(1.dp, Color.LightGray)
                ) {
                    Text("Cancel Request", color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DriverEnRouteToHospitalScreen(
    driverViewModel: DriverViewModel,
    onBack: () -> Unit,
    onHospitalReached: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF3F4F6)) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map Background
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB))) {
                // Map pattern
                repeat(20) { i ->
                    Box(modifier = Modifier.offset(x = (i * 40).dp).fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.5f)))
                    Box(modifier = Modifier.offset(y = (i * 40).dp).fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.5f)))
                }
                Box(modifier = Modifier.align(Alignment.Center).size(2.dp, 300.dp).background(DeepBlue))
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Nav
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                        Text("En-Route", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                    IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Banner
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(DeepBlue), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.AltRoute, null, tint = Color.White, modifier = Modifier.size(32.dp)) // Turn right icon
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("IN 400 METERS", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("Turn right onto MG Road", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Status Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("8 mins", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                Text("3.2 km remaining • ETA 10:42 AM", fontSize = 13.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AccentRed))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("En-route to\nHospital", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentRed, textAlign = TextAlign.End)
                                }
                                Text("Patient: Suresh Kumar", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { 0.7f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = DeepBlue,
                            trackColor = Color(0xFFF5F7FA)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onHospitalReached,
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Hospital Reached", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(56.dp).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))) {
                                Icon(Icons.Default.LocationOn, null, tint = DeepBlue)
                            }
                        }
                    }
                }
                
                // Navigation Bar Placeholder
                Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 12.dp)) { Icon(Icons.Default.Home, null, tint = Color.Gray); Text("Home", fontSize = 10.sp, color = Color.Gray) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 12.dp)) { 
                        Surface(color = DeepBlue, shape = CircleShape, modifier = Modifier.size(36.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White) } }
                        Text("Requests", fontSize = 10.sp, color = DeepBlue, fontWeight = FontWeight.Bold) 
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 12.dp)) { Icon(Icons.Default.Person, null, tint = Color.Gray); Text("Profile", fontSize = 10.sp, color = Color.Gray) }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DriverFlowPreview1() {
    SAFEREACHTheme {
        DriverRequestAcceptedScreen(driverViewModel = viewModel(), onBack = {}, onNavigate = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DriverFlowPreview2() {
    SAFEREACHTheme {
        DriverNavigationToPickupScreen(driverViewModel = viewModel(), onBack = {}, onArrived = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DriverFlowPreview3() {
    SAFEREACHTheme {
        DriverArrivedAtPickupScreen(driverViewModel = viewModel(), onBack = {}, onStartTrip = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DriverFlowPreview4() {
    SAFEREACHTheme {
        DriverPatientBoardedScreen(driverViewModel = viewModel(), onBack = {}, onConfirmStart = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DriverFlowPreview5() {
    SAFEREACHTheme {
        DriverEnRouteToHospitalScreen(driverViewModel = viewModel(), onBack = {}, onHospitalReached = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DriverLoginPreview() {
    SAFEREACHTheme {
        DriverLoginScreen(authViewModel = viewModel(), onLoginSuccess = {}, onCitizenLogin = {})
    }
}


@Preview(showBackground = true)
@Composable
fun DriverHomePreview() {
    SAFEREACHTheme {
        DriverHomeScreen(driverViewModel = viewModel(), onLogout = {}, onNewRequest = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EmergencyRequestPreview() {
    SAFEREACHTheme {
        EmergencyRequestScreen(driverViewModel = viewModel(), onAccept = {}, onDecline = {})
    }
}

@Composable
fun AdminLoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState
    
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(DeepBlue), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Command Center", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            Text("Secure access for authorized logistics and dispatch personnel only.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))

            Spacer(modifier = Modifier.height(40.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Admin Email Address", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = email, onValueChange = { email = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), placeholder = { Text("admin@safereach.com") }, leadingIcon = { Icon(Icons.Default.Email, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Password", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = password, onValueChange = { password = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), visualTransformation = PasswordVisualTransformation(), trailingIcon = { Icon(Icons.Default.Link, null) })
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = false, onCheckedChange = {})
                        Text("Keep me logged in for this session", fontSize = 12.sp, color = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = { authViewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        enabled = authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Secure Login")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Default.HeadsetMic, null); Spacer(modifier = Modifier.width(8.dp)); Text("Support") }
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Dns, null); Spacer(modifier = Modifier.width(8.dp)); Text("System Status") }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("© 2024 RapidRescue Emergency Logistics. Protected by multi-factor authentication and hardware encryption.", fontSize = 10.sp, color = Color.LightGray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AdminDashboard(
    adminViewModel: AdminViewModel,
    onLogout: () -> Unit,
    onViewRequestDetails: (Int) -> Unit,
    onViewUnitDetails: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.AutoMirrored.Filled.Assignment, null) }, label = { Text("Requests") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Analytics, null) }, label = { Text("Analytics") })
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.Group, null) }, label = { Text("Users") })
                NavigationBarItem(selected = selectedTab == 4, onClick = { selectedTab = 4 }, icon = { Icon(Icons.Default.AdminPanelSettings, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AdminOverviewScreen(adminViewModel, onLogout, onViewUnitDetails)
                1 -> AdminRequestsTab(adminViewModel, onViewDetails = onViewRequestDetails)
                2 -> AdminAnalyticsScreen()
                3 -> AdminUserManagementScreen()
                4 -> AdminDetailedProfileScreen(onLogout)
            }
        }
    }
}

@Composable
fun AdminReportsScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Menu, null, tint = DeepBlue)
            Text("Operational Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("ANALYTICS & LOGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Text("Compliance & Performance", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
        Text("Access real-time generated operational documentation. All reports are verified and digitally signed for clinical audit purposes.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weekly Efficiency Summary", fontWeight = FontWeight.Bold, color = DeepBlue)
                        Text("Consolidated data on response times, fuel usage, and crew performance.", fontSize = 12.sp, color = Color.Gray)
                    }
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                        Text("LATEST", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Default.PictureAsPdf, null); Spacer(modifier = Modifier.width(8.dp)); Text("PDF", fontSize = 12.sp) }
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Description, null); Spacer(modifier = Modifier.width(8.dp)); Text("CSV", fontSize = 12.sp) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        AdminReportItem("Daily Response Log", "24-hour summary of all emergency dispatches and outcomes.")
        AdminReportItem("Fleet Maintenance Report", "Service records, inspection status, and maintenance schedules.")
        AdminReportItem("Hospital Handoff Data", "Analytics on transfer protocols and wait-time across network.")
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AdminReportItem(title: String, desc: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AssignmentTurnedIn, null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            }
            Text(desc, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, start = 32.dp))
            Row(modifier = Modifier.padding(top = 12.dp, start = 32.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = {}) { Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                TextButton(onClick = {}) { Text("CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun AdminUserManagementScreen() {
    var selectedTab by remember { mutableStateOf("Dispatchers") }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {}, containerColor = DeepBlue, contentColor = Color.White) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF3F4F6))) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Menu, null)
                Text("Users Management", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.NotificationsNone, null)
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Dispatchers", "Drivers", "Supervisors").forEach {
                    val isSelected = selectedTab == it
                    TextButton(onClick = { selectedTab = it }, modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(it, color = if(isSelected) DeepBlue else Color.Gray, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
                            if(isSelected) Box(modifier = Modifier.height(2.dp).width(40.dp).background(DeepBlue))
                        }
                    }
                }
            }

        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                AdminUserCard("Marcus Chen", "LEVEL 3 DISPATCHER", "Active", "12 mins ago", "Admin Level")
                AdminUserCard("Sarah Rodriguez", "SHIFT SUPERVISOR", "Off-Duty", "4 hours ago", "Full Access")
                AdminUserCard("David Okoro", "SENIOR DISPATCHER", "Active", "Just now", "Operations")
            }
        }
    }
}

@Composable
fun AdminUserCard(name: String, role: String, status: String, active: String, perms: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.LightGray)) { Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center)) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(role, fontSize = 11.sp, color = Color.Gray)
                }
                Surface(color = if(status == "Active") Color(0xFFE8F5E9) else Color(0xFFFFF1F0), shape = RoundedCornerShape(12.dp)) {
                    Text(status.uppercase(), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if(status == "Active") Color(0xFF2E7D32) else AccentRed)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Last Active", fontSize = 11.sp, color = Color.Gray); Text(active, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
                Column(horizontalAlignment = Alignment.End) { Text("Permissions", fontSize = 11.sp, color = Color.Gray); Text(perms, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepBlue) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Edit") }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(8.dp)); Text("Reset") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)), contentPadding = PaddingValues(0.dp)) {
                Text("View Activity Logs", color = Color.DarkGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AdminDetailedProfileScreen(onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
            Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray)) { Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(40.dp)) }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Command Center", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Dispatcher ID: 9921 • Active Session", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Schedule, null, tint = DeepBlue)
                    Text("08:42", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Text("Session Duration", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Star, null, tint = DeepBlue)
                    Text("34", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Text("Requests Handled", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Security Protocol", fontWeight = FontWeight.Bold)
                }
                Text("Your session data will be encrypted and archived. Clearing local cache is recommended for shared terminals.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Language, null); Spacer(modifier = Modifier.width(8.dp)); Text("Switch Account") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = AccentRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Ready to Exit?", fontWeight = FontWeight.Bold)
                }
                Text("Ensure all active incident logs are submitted before logging out to maintain fleet synchronization.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) { 
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out Now") 
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AdminReportsPreview() {
    SAFEREACHTheme { AdminReportsScreen() }
}

@Preview(showBackground = true)
@Composable
fun AdminUsersPreview() {
    SAFEREACHTheme { AdminUserManagementScreen() }
}

@Preview(showBackground = true)
@Composable
fun AdminDetailedProfilePreview() {
    SAFEREACHTheme { AdminDetailedProfileScreen {} }
}

@Composable
fun AdminOverviewScreen(adminViewModel: AdminViewModel, onLogout: () -> Unit, onViewUnitDetails: () -> Unit) {
    val hospitalsState by adminViewModel.hospitalsState
    val requestsState by adminViewModel.requestsState
    
    LaunchedEffect(Unit) {
        adminViewModel.fetchHospitals()
        adminViewModel.fetchAllRequests()
    }
    
    val totalRequests = if (requestsState is AdminUiState.Success) (requestsState as AdminUiState.Success).data.size.toString() else "0"
    val activeHospitals = if (hospitalsState is AdminUiState.Success) (hospitalsState as AdminUiState.Success).data.size.toString() else "0"

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Menu, null, tint = DeepBlue)
            Text("Overview", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            Icon(Icons.Default.NotificationsNone, null, tint = DeepBlue)
        }
        Spacer(modifier = Modifier.height(24.dp))
        AdminStatCard(title = "Total Requests", value = totalRequests, trend = "+12%", icon = Icons.Default.Tsunami, iconBg = Color(0xFFE8EAF6))
        Spacer(modifier = Modifier.height(16.dp))
        AdminStatCard(title = "Active Ambulances", value = "23", trend = "Active Now", icon = Icons.Default.LocalTaxi, iconBg = Color(0xFFFFEBEE))
        
        TextButton(onClick = onViewUnitDetails, modifier = Modifier.padding(start = 64.dp)) {
            Text("View Fleet Details", fontSize = 12.sp, color = DeepBlue, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        AdminStatCard(title = "Completed Today", value = "120", trend = "Total Today", icon = Icons.Default.CheckCircle, iconBg = Color(0xFFE8F5E9))
        Spacer(modifier = Modifier.height(16.dp))
        AdminStatCard(title = "Available Hospitals", value = activeHospitals, trend = "Operational", icon = Icons.Default.LocalHospital, iconBg = Color(0xFFF5F5F5))
        
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onLogout, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Logout System", color = AccentRed) }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, trend: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), color = iconBg, shape = RoundedCornerShape(12.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = DeepBlue) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, color = Color.Gray)
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            }
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                Text(trend, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminRequestsTab(adminViewModel: AdminViewModel, onViewDetails: (Int) -> Unit) {
    var selectedFilter by remember { mutableStateOf("New") }
    val requestsState by adminViewModel.requestsState

    LaunchedEffect(Unit) {
        adminViewModel.fetchAllRequests()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("New", "Active", "Completed").forEach {
                val isSelected = selectedFilter == it
                Surface(
                    modifier = Modifier.weight(1f).height(40.dp).clickable { selectedFilter = it },
                    color = if (isSelected) DeepBlue else Color.White,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(it, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
            if (requestsState is AdminUiState.Loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepBlue)
                }
            } else if (requestsState is AdminUiState.Error) {
                Text((requestsState as AdminUiState.Error).message, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else if (requestsState is AdminUiState.Success) {
                val requests = (requestsState as AdminUiState.Success).data
                val filtered = when (selectedFilter) {
                    "New" -> requests.filter { it.status.lowercase() == "pending" }
                    "Active" -> requests.filter { it.status.lowercase() == "accepted" || it.status.lowercase() == "arrived" || it.status.lowercase() == "boarded" }
                    "Completed" -> requests.filter { it.status.lowercase() == "completed" }
                    else -> requests
                }
                
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No requests found", color = Color.Gray)
                    }
                } else {
                    filtered.forEach { req ->
                        AdminRequestCard(
                            name = req.patientName,
                            condition = req.emergencyType,
                            loc = req.pickupAddress,
                            priority = req.requestedAmbulanceType,
                            onClick = { onViewDetails(req.requestId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRequestCard(name: String, condition: String, loc: String, priority: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp)) {
                    Text(priority, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = AccentRed, fontWeight = FontWeight.Bold)
                }
            }
            Text(condition, color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Text(loc, fontSize = 13.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DeepBlue)) { Text("Assign Unit") }
                OutlinedButton(onClick = onClick, modifier = Modifier.weight(0.4f)) { Text("Details") }
            }
        }
    }
}

@Composable
fun UnifiedRequestDetailsScreen(
    driverViewModel: DriverViewModel,
    requestId: Int,
    onBack: () -> Unit
) {
    val trackingState by driverViewModel.trackingState
    
    LaunchedEffect(requestId) {
        if (requestId != -1) {
            driverViewModel.fetchTrackingInfo(requestId)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                Text("Request Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            }

            if (trackingState is DriverUiState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepBlue)
                }
            } else if (trackingState is DriverUiState.Error) {
                Text((trackingState as DriverUiState.Error).message, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else if (trackingState is DriverUiState.Success) {
                val data = (trackingState as DriverUiState.Success).data
                
                Surface(color = Color(0xFFFFEBEE), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${data.status.uppercase()}: ${data.emergency.type}", color = AccentRed, fontWeight = FontWeight.Bold)
                        Text("#RR-${data.requestId}", color = Color.Gray)
                    }
                }
                
                // Map Placeholder
                Card(modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB))) {
                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.align(Alignment.Center).size(48.dp), tint = DeepBlue)
                    }
                }
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Service Information", fontWeight = FontWeight.Bold, color = DeepBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Patient", fontSize = 11.sp, color = Color.Gray)
                                    Text(data.patient.name, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Pickup Address", fontSize = 11.sp, color = Color.Gray)
                                    Text(data.pickup.address, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    
                    if (data.assignedAmbulance != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Assigned Unit", fontWeight = FontWeight.Bold, color = DeepBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), border = border(1.dp, Color.LightGray.copy(alpha = 0.3f))) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalTaxi, null, tint = DeepBlue)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(data.assignedAmbulance.driverName, fontWeight = FontWeight.Bold)
                                    Text(data.assignedAmbulance.vehicleNumber, fontSize = 12.sp, color = Color.Gray)
                                }
                                IconButton(onClick = {}) { Icon(Icons.Default.Phone, null, tint = DeepBlue) }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Return to Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VitalBox(modifier: Modifier, label: String, value: String) {
    Surface(modifier = modifier, color = Color(0xFFF5F7FA), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentRed)
        }
    }
}

@Composable
fun TimelineItem(title: String, time: String, isDone: Boolean) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (isDone) Color(0xFF2E7D32) else Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column { Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text(time, fontSize = 12.sp, color = Color.Gray) }
    }
}

@Composable
fun AdminAnalyticsScreen() {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Performance Analytics", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
        Spacer(modifier = Modifier.height(24.dp))
        AnalyticsCard("AVG RESPONSE", "4.2m", "12% faster than last month")
        AnalyticsCard("ACTIVE FLEET", "84%", "32 vehicles active")
        AnalyticsCard("DAILY EMERGENCIES", "1,248", "Peak hours: 18:00 - 22:00")
        
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Fleet Efficiency", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                    EfficiencyDonut("92%", "Fuel")
                    EfficiencyDonut("78%", "Equipment")
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(label: String, value: String, desc: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, color = Color.Gray)
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Text(desc, fontSize = 11.sp, color = Color(0xFF2E7D32))
            }
            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF2E7D32))
        }
    }
}

@Composable
fun EfficiencyDonut(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(60.dp).border(4.dp, DeepBlue, CircleShape), contentAlignment = Alignment.Center) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(label, modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp, color = Color.Gray)
    }
}
@Composable
fun AdminUnitDetailsScreen(onBack: () -> Unit) {
    // Basic implementation for RR-204 Details
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("Unit RR-204 Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth().height(200.dp)) { Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) { Icon(Icons.Default.LocalTaxi, null, modifier = Modifier.align(Alignment.Center).size(80.dp), tint = Color.White) } }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Mercedes Sprinter 419", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Registration: RR-S-2024-X", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnalyticsCard("O2 LEVELS", "88%", "Sufficient for 12 hours")
                AnalyticsCard("FUEL", "64%", "Estimated range: 320 km")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminLoginPreview() {
    SAFEREACHTheme { AdminLoginScreen(authViewModel = viewModel(), onLoginSuccess = {}, onBack = {}) }
}

@Preview(showBackground = true)
@Composable
fun AdminOverviewPreview() {
    SAFEREACHTheme { AdminDashboard(adminViewModel = viewModel(), onLogout = {}, onViewRequestDetails = {}, onViewUnitDetails = {}) }
}

private fun border(width: androidx.compose.ui.unit.Dp, color: Color) = BorderStroke(width, color)

@Composable
fun AmbulanceIllustration() {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.offset(x = 25.dp, y = (-15).dp).size(240.dp, 260.dp).clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.4f)))
        Card(modifier = Modifier.size(260.dp, 280.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0D1B2A)), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
fun DotIndicator(selected: Boolean) {
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (selected) DeepBlue else Color.LightGray.copy(alpha = 0.5f)))
}

@Composable
fun PillIndicator(selected: Boolean) {
    Box(modifier = Modifier.width(32.dp).height(8.dp).clip(CircleShape).background(if (selected) DeepBlue else Color.LightGray.copy(alpha = 0.5f)))
}


@Composable
fun RequestAmbulanceScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onSendRequest: () -> Unit
) {
    var details by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var medicalCondition by remember { mutableStateOf("") }
    var criticalityLevel by remember { mutableStateOf("Critical") }
    
    val requestState by userViewModel.requestState
    
    LaunchedEffect(requestState) {
        if (requestState is UserUiState.Success) {
            onSendRequest()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) }
                    Text(text = "Request Ambulance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Map Placeholder
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background pattern (mimicking map)
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFB2DFDB).copy(alpha = 0.5f)))
                        
                        // Red Pin
                        Box(modifier = Modifier.align(Alignment.Center)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Text("*", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.Center).offset(y = (-4).dp))
                        }
                        
                        // Auto-locating Badge
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 2.dp
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp), tint = DeepBlue)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-locating...", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Current Location Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Current Location", fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = "MG Road, Bangalore",
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Text("Change", color = DeepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f), focusedContainerColor = Color(0xFFF5F7FA), unfocusedContainerColor = Color(0xFFF5F7FA))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Details Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Add Details (Optional)", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Apartment, Floor, Landmark", color = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Patient Name Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Patient Name", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Full Name", color = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Age and Gender Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Age", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Years", color = Color.LightGray) },
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gender", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Medical Condition
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Medical Condition", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = medicalCondition,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        placeholder = { Text("Select condition", color = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Criticality Level
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Criticality Level", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Moderate", "Critical", "Severe").forEach { level ->
                            val isSelected = criticalityLevel == level
                            val bgColor = if (isSelected) Color(0xFFFFEBEE) else Color.White
                            val borderColor = if (isSelected) Color(0xFFC62828) else Color.LightGray
                            val textColor = if (isSelected) Color(0xFFC62828) else Color.Gray

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                    .background(bgColor),
                                onClick = { criticalityLevel = level },
                                color = bgColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = textColor)
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(text = level, fontSize = 12.sp, color = textColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Emergency Tip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Emergency Tip", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Clear the driveway and keep the patient calm while the rescue team is en-route.", fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ETA and Send Button
                Text(
                    text = buildAnnotatedString {
                        append("Estimated Arrival: ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DeepBlue)) {
                            append("4-6 mins")
                        }
                    },
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        userViewModel.createEmergencyRequest(
                            patientName = patientName,
                            patientPhone = "", // Add patient phone if needed, currently empty in state
                            address = "MG Road, Bangalore",
                            lat = 12.9716,
                            lng = 77.5946,
                            type = criticalityLevel,
                            condition = medicalCondition,
                            desc = details
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = requestState !is UserUiState.Loading
                ) {
                    if (requestState is UserUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("*", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Request", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RequestAmbulancePreview() {
    SAFEREACHTheme {
        RequestAmbulanceScreen(userViewModel = viewModel(), onBack = {}, onSendRequest = {})
    }
}

@Composable
fun SearchingAmbulanceScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onCancelRequest: () -> Unit,
    onAmbulanceFound: () -> Unit
) {
    val trackingState by userViewModel.trackingState
    
    LaunchedEffect(trackingState) {
        if (trackingState is UserUiState.Success) {
            val data = (trackingState as UserUiState.Success<TrackResponse>).data
            if (data.status.lowercase() == "accepted") {
                onAmbulanceFound()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Stylized Map Background Placeholder
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
                // Background map lines pattern mimicked by simple boxes or canvas (simplified here)
                repeat(10) { i ->
                    Box(modifier = Modifier.offset(x = (i * 50).dp, y = 0.dp).fillMaxHeight().width(1.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                    Box(modifier = Modifier.offset(x = 0.dp, y = (i * 100).dp).fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.3f)))
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepBlue) }
                        Text(text = "RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = DeepBlue) }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    // Pulsing Logo Section
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(DeepBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "*", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "Searching Nearest Ambulance...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Please wait while we find the nearest available ambulance for your location.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Scanning Badge
                    Surface(
                        color = Color(0xFFE8EAF6),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DeepBlue))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Scanning for 12 active units in range", fontSize = 12.sp, color = DeepBlue, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Info Cards
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Pickup Point", fontSize = 12.sp, color = Color.Gray)
                                Text("MG Road, Bang...", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f).height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Request Type", fontSize = 12.sp, color = Color.Gray)
                                Text("Emergency", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Cancel Button
                    OutlinedButton(
                        onClick = onCancelRequest,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Request", color = AccentRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your request will be cancelled immediately if you press the button above.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchingAmbulancePreview() {
    SAFEREACHTheme {
        SearchingAmbulanceScreen(userViewModel = viewModel(), onBack = {}, onCancelRequest = {}, onAmbulanceFound = {})
    }
}

@Composable
fun AmbulanceFoundScreen(onBack: () -> Unit, onTrackNow: () -> Unit) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Star, null) },
                    label = { Text("Requests") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = DeepBlue)
                )
                NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                        Text("Ambulance Found!!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFE8EAF6)), contentAlignment = Alignment.Center) {
                        Text("*", color = DeepBlue, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ambulance is on the way", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Estimated arrival in ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DeepBlue)) { append("6 mins") }
                            append(". Please stay at your current location.")
                        },
                        fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Map View Placeholder
                    Card(modifier = Modifier.fillMaxWidth().height(180.dp), shape = RoundedCornerShape(16.dp)) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
                            // Simple map illustration pattern
                            repeat(8) { i ->
                                Box(modifier = Modifier.offset(x = (i * 60).dp).fillMaxHeight().width(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                                Box(modifier = Modifier.offset(y = (i * 40).dp).fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                            }
                            // Route line (simulated)
                            Box(modifier = Modifier.align(Alignment.Center).size(100.dp, 2.dp).background(Color.Red))
                            
                            // Badge
                            Surface(
                                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                                color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(DeepBlue))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Live Tracking Enabled", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Driver Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.LightGray)) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center), tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ramesh Kumar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB300))
                                        Text(" 4.9 • 2,400+ trips", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("VEHICLE", fontSize = 10.sp, color = Color.Gray)
                                    Text("KA 01 AB 1234", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepBlue)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.Phone, label = "Phone", value = "+91 9876543210")
                                InfoBox(modifier = Modifier.weight(1f), icon = Icons.Default.MedicalServices, label = "Type", value = "ALS Advanced")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onTrackNow,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NearMe, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Track Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun InfoBox(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5F7FA),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = DeepBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = Color.Gray)
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AmbulanceFoundPreview() {
    SAFEREACHTheme {
        AmbulanceFoundScreen(onBack = {}, onTrackNow = {})
    }
}

@Composable
fun LiveTrackingScreen(onBack: () -> Unit, onAmbulanceArrived: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Dark Map Placeholder
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF263238))) {
                // Background map grid pattern
                repeat(25) { i ->
                    Box(modifier = Modifier.offset(x = (i * 30).dp).fillMaxHeight().width(0.5.dp).background(Color.White.copy(alpha = 0.1f)))
                    Box(modifier = Modifier.offset(y = (i * 30).dp).fillMaxWidth().height(0.5.dp).background(Color.White.copy(alpha = 0.1f)))
                }
                
                // Simulated Route Line
                // (Using a simple box for illustration)
                Box(modifier = Modifier.align(Alignment.Center).size(2.dp, 200.dp).background(Color(0xFF1E88E5)).offset(x = 20.dp, y = (-50).dp))

                // Ambulance Marker
                Column(
                    modifier = Modifier.align(Alignment.Center).offset(y = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(DeepBlue).border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("*", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = DeepBlue,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Ambulance Moving",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Map Controls (Floating Top Right)
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 100.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { },
                    containerColor = Color.White,
                    contentColor = Color.DarkGray,
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(20.dp)) }
                
                FloatingActionButton(
                    onClick = { },
                    containerColor = Color.White,
                    contentColor = Color.DarkGray,
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Icon(Icons.Default.Layers, null, modifier = Modifier.size(20.dp)) }
            }

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                    Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            // Bottom Info Card
            Card(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Arrival Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Arriving in", fontSize = 12.sp, color = Color.Gray)
                            Text("6 mins", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = DeepBlue)
                        }
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("On the way", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Driver Profile Row
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.LightGray)) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.align(Alignment.Center).size(32.dp), tint = Color.White)
                            Box(modifier = Modifier.align(Alignment.BottomEnd).size(18.dp).clip(CircleShape).background(Color(0xFF4CAF50)).border(2.dp, Color.White, CircleShape)) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp).align(Alignment.Center), tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ramesh Kumar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepBlue)
                            Text("KA 01 AB 1234", fontSize = 13.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB300))
                                Text(" 4.9 • Fast Responder", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Distance", fontSize = 11.sp, color = Color.Gray)
                            Text("2.4 km", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Call Driver", fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ChatBubbleOutline, null, modifier = Modifier.size(18.dp), tint = Color.DarkGray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Chat", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Footer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onAmbulanceArrived) { // Changed for demo
                            Text("Arrive (Demo)", color = Color(0xFF4CAF50), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Tracking", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AmbulanceArrivedScreen(onBack: () -> Unit, onStartJourney: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                    Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                }
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Arrived Icon
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(DeepBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Arrived", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                Text("Ambulance has arrived at your\nlocation.", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(32.dp))

                // Plate Number Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.LocalTaxi, contentDescription = null, tint = DeepBlue)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PLATE NUMBER", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("KA 01 AB 1234", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                        }
                        Surface(color = Color(0xFF2E7D32), shape = RoundedCornerShape(12.dp)) {
                            Text("On Site", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Map Card
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0))) {
                        // Map lines pattern
                        repeat(10) { i ->
                            Box(modifier = Modifier.offset(x = (i * 50).dp).fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.5f)))
                            Box(modifier = Modifier.offset(y = (i * 40).dp).fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.5f)))
                        }
                        // Marker
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(48.dp).align(Alignment.Center).offset(y = (-8).dp))
                        
                        // Badge
                        Surface(
                            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                            color = Color.White, shape = RoundedCornerShape(20.dp), shadowElevation = 2.dp
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Location", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Driver", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartJourney,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MedicalServices, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Journey", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = DeepBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Issue", color = DeepBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun JourneyTrackingScreen(onBack: () -> Unit, onViewDetails: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full Screen Map Background
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE8EAF6))) {
                // Map pattern
                repeat(20) { i ->
                    Box(modifier = Modifier.offset(x = (i * 40).dp).fillMaxHeight().width(1.dp).background(Color.White.copy(alpha = 0.5f)))
                    Box(modifier = Modifier.offset(y = (i * 40).dp).fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.5f)))
                }
                // Simulated Route
                Box(modifier = Modifier.align(Alignment.Center).size(300.dp, 2.dp).background(Color(0xFF2196F3)).offset(x = (-50).dp, y = 100.dp))
                // Ambulance icon on map
                Icon(Icons.Default.LocalHospital, null, modifier = Modifier.align(Alignment.Center).size(32.dp), tint = Color.Red)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DeepBlue) }
                    Text("RapidRescue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifications", tint = DeepBlue) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Badge
                Surface(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("In Route to Hospital", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Destination Card
                Card(
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalHospital, null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Destination", fontSize = 12.sp, color = Color.Gray)
                                Text("City Hospital", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("12 mins", fontSize = 12.sp, color = Color.Gray)
                                Text("5.1 km away", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { 0.6f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                            color = DeepBlue,
                            trackColor = Color(0xFFF5F7FA)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Floating Card
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(40.dp), color = Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Star, null, modifier = Modifier.size(20.dp), tint = Color(0xFFC62828))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Emergency Response", fontSize = 12.sp, color = Color.Gray)
                                Text("Advanced Life Support (ALS)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                            }
                            OutlinedButton(onClick = onViewDetails, shape = RoundedCornerShape(12.dp), border = border(1.dp, Color.LightGray)) {
                                Text("View\nDetails", fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { }, modifier = Modifier.weight(1f).height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F7FA)), shape = RoundedCornerShape(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.DarkGray)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Chat", color = Color.DarkGray, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(onClick = { }, modifier = Modifier.weight(1f).height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = DeepBlue), shape = RoundedCornerShape(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Navigation, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Track Live", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HospitalDetailsScreen(onBack: () -> Unit) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Star, null) }, label = { Text("Requests") })
                NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = LightBg) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // Hero Image Header
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    // Placeholder for hospital photo
                    Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) {
                        Icon(Icons.Default.Business, null, modifier = Modifier.align(Alignment.Center).size(100.dp), tint = Color.White.copy(alpha = 0.5f))
                    }
                    // Overlay elements
                    IconButton(onClick = onBack, modifier = Modifier.padding(top = 40.dp, start = 8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    IconButton(onClick = { }, modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 8.dp)) {
                        Icon(Icons.Default.Share, "Share", tint = Color.White)
                    }
                    
                    // Verified Badge
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        color = DeepBlue,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    // Title Card
                    Card(
                        modifier = Modifier.fillMaxWidth().offset(y = (-30).dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("City Hospital", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(color = Color(0xFF1B5E20), shape = RoundedCornerShape(4.dp)) {
                                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("4.8 (1.2k Reviews)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Surface(color = Color(0xFFF5F7FA), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("1.2", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DeepBlue)
                                    Text("km", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Contact Info
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocationOn, null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Address", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Text("24, MG Road, Bangalore, Karnataka 560001", fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Phone, null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Contact Number", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Text("080 1234 5678", fontSize = 13.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Services Offered", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Services Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HospitalServiceChip(modifier = Modifier.weight(1f), icon = Icons.Default.AcUnit, title = "Emergency", subtitle = "24/7")
                        HospitalServiceChip(modifier = Modifier.weight(1f), icon = Icons.Default.Favorite, title = "Cardiology", subtitle = "")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HospitalServiceChip(modifier = Modifier.weight(1f), icon = Icons.Default.MonitorHeart, title = "Radiology", subtitle = "")
                        HospitalServiceChip(modifier = Modifier.weight(1f), icon = Icons.Default.Bloodtype, title = "Blood Bank", subtitle = "")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Bed Availability
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bed Availability", fontSize = 13.sp, color = Color.Gray)
                            }
                            Text("12 Beds", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Navigation, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Navigate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun HospitalServiceChip(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = border(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = DeepBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                if (subtitle.isNotEmpty()) Text(subtitle, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    SAFEREACHTheme {
        UserProfileScreen(userViewModel = viewModel(), onLogout = {}, onEditProfile = {}, onViewAddresses = {}, onViewNotifications = {})
    }
}

@Preview(showBackground = true)
@Composable
fun HospitalInfoPreview() {
    SAFEREACHTheme {
        HospitalDetailsScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AmbulanceArrivedPreview() {
    SAFEREACHTheme {
        AmbulanceArrivedScreen(onBack = {}, onStartJourney = {})
    }
}

@Preview(showBackground = true)
@Composable
fun JourneyTrackingPreview() {
    SAFEREACHTheme {
        JourneyTrackingScreen(onBack = {}, onViewDetails = {})
    }
}

@Composable
fun HospitalListScreen(userViewModel: UserViewModel, onBack: () -> Unit) {
    val hospitalsState by userViewModel.hospitalsState
    
    LaunchedEffect(Unit) {
        userViewModel.fetchHospitals()
    }
    
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("Nearby Hospitals", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (hospitalsState is UserUiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepBlue)
                    }
                } else if (hospitalsState is UserUiState.Error) {
                    Text((hospitalsState as UserUiState.Error).message, color = Color.Red, modifier = Modifier.padding(16.dp))
                } else if (hospitalsState is UserUiState.Success) {
                    val hospitals = (hospitalsState as UserUiState.Success).data
                    if (hospitals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No hospitals found nearby", color = Color.Gray)
                        }
                    } else {
                        hospitals.forEach { hospital ->
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalHospital, null, tint = DeepBlue)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(hospital.name, fontWeight = FontWeight.Bold)
                                        Text(hospital.address, color = Color.Gray, fontSize = 12.sp)
                                        Text("Available ICU: ${hospital.availableIcuBeds}", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthTipsScreen(onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("Health Tips", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                listOf("First Aid Basics", "CPR Guide", "Burn Treatment", "Stroke Awareness").forEach {
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text(it, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyContactsScreen(onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = LightBg) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("Emergency Contacts", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                mapOf("Police" to "100", "Ambulance" to "102", "Fire" to "101", "Women Helpline" to "1091").forEach { (name, num) ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(name, fontWeight = FontWeight.Bold)
                            Text(num, color = DeepBlue, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleAmbulanceScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onScheduled: () -> Unit
) {
    var patientName by remember { mutableStateOf("") }
    var pickupAddress by remember { mutableStateOf("") }
    var scheduleDate by remember { mutableStateOf("") }
    var scheduleTime by remember { mutableStateOf("") }
    var ambulanceType by remember { mutableStateOf("BLS") }
    
    val requestState by userViewModel.requestState
    
    LaunchedEffect(requestState) {
        if (requestState is UserUiState.Success) {
            onScheduled()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = DeepBlue) }
                Text("Schedule Ambulance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ProfileTextField(label = "Patient Name", value = patientName, onValueChange = { patientName = it }, icon = Icons.Default.Person)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTextField(label = "Pickup Address", value = pickupAddress, onValueChange = { pickupAddress = it }, icon = Icons.Default.LocationOn)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ProfileTextField(label = "Date", value = scheduleDate, onValueChange = { scheduleDate = it }, icon = Icons.Default.CalendarToday)
                }
                Box(modifier = Modifier.weight(1f)) {
                    ProfileTextField(label = "Time", value = scheduleTime, onValueChange = { scheduleTime = it }, icon = Icons.Default.AccessTime)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Ambulance Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BLS", "ALS", "CCU").forEach { type ->
                    val isSelected = ambulanceType == type
                    Surface(
                        modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(12.dp)).clickable { ambulanceType = type },
                        color = if (isSelected) DeepBlue else Color(0xFFF5F7FA),
                        border = border(1.dp, if (isSelected) DeepBlue else Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(type, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    userViewModel.createEmergencyRequest(
                        patientName = patientName,
                        patientPhone = "", 
                        address = pickupAddress,
                        lat = 12.9716, 
                        lng = 77.5946,
                        type = ambulanceType,
                        condition = "SCHEDULED TRANSPORT",
                        scheduledAt = "$scheduleDate $scheduleTime"
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepBlue),
                enabled = requestState !is UserUiState.Loading && patientName.isNotEmpty() && scheduleDate.isNotEmpty()
            ) {
                if (requestState is UserUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm Schedule", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
