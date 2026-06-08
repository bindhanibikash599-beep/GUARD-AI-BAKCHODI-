package com.example.ui

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================================
// TRANSLATION DICTIONARY
// ==========================================================
object Translator {
    fun getLabel(key: String, lang: String): String {
        return when (lang) {
            "Hindi" -> when (key) {
                "dashboard" -> "डैशबोर्ड"
                "ai_writer" -> "एआई टूल्स"
                "history" -> "इतिहास"
                "favorites" -> "तारांकित"
                "chat" -> "एआई चैट"
                "premium" -> "प्रीमियम"
                "admin" -> "एडमिन पैनल"
                "profile" -> "प्रोफ़ाइल"
                "tagline" -> "सुरक्षा गार्डों और पर्यवेक्षकों के लिए व्यावसायिक अंग्रेजी सहायक"
                "welcome" -> "स्वागत हे, अधिकारी"
                "credits" -> "शेष क्रेडिट"
                "claim_daily" -> "दैनिक दावा करें"
                "generate" -> "अंग्रेजी रिपोर्ट तैयार करें"
                "original" -> "मूल स्थानीय पाठ"
                "converted" -> "व्यावसायिक अंग्रेजी रिपोर्ट"
                "choose_lang" -> "स्रोत्र भाषा"
                "choose_tone" -> "लक्ष्य प्रपत्र शैली"
                else -> key
            }
            "Odia" -> when (key) {
                "dashboard" -> "ଡ୍ୟାସବୋର୍ଡ"
                "ai_writer" -> "ଏଆଇ ଟୁଲ୍ସ"
                "history" -> "ଇତିହାସ"
                "favorites" -> "ଷ୍ଟାର୍ଡ"
                "chat" -> "ଏଆଇ ଚାଟ୍"
                "premium" -> "ପ୍ରିମିୟମ"
                "admin" -> "ଆଡମିନ"
                "profile" -> "ପ୍ରୋଫାଇଲ୍"
                "tagline" -> "ସୁରକ୍ଷା ଗାର୍ଡ ଏବଂ ସୁପରଭାଇଜରଙ୍କ ପାଇଁ ବ୍ୟବସାୟିକ ସହାୟକ"
                "welcome" -> "ସ୍ୱାଗତ, ଅଧିକାରୀ"
                "credits" -> "ଆପଣଙ୍କ କ୍ରେଡିଟ୍"
                "claim_daily" -> "ଦୈନିକ ଦାବି କରନ୍ତୁ"
                "generate" -> "ଇଂରାଜୀ ରିପୋର୍ଟ ପ୍ରସ୍ତୁତ କରନ୍ତୁ"
                "original" -> "ମୂଳ ସ୍ଥାନୀୟ ପାଠ୍ୟ"
                "converted" -> "ବ୍ୟବସାୟିକ ଇଂରାଜୀ ରିପୋର୍ଟ"
                "choose_lang" -> "ଉତ୍ସ ଭାଷା"
                "choose_tone" -> "ଶୈଳୀ ବାଛନ୍ତୁ"
                else -> key
            }
            else -> when (key) {
                "dashboard" -> "Dashboard"
                "ai_writer" -> "AI Writers"
                "history" -> "History"
                "favorites" -> "Starred"
                "chat" -> "AI Chat"
                "premium" -> "Premium"
                "admin" -> "Admin Panel"
                "profile" -> "Profile"
                "tagline" -> "Professional English Writing Assistant For Security Guards & Supervisors"
                "welcome" -> "Welcome back, Officer"
                "credits" -> "Credits Balance"
                "claim_daily" -> "Claim Daily Login +5"
                "generate" -> "Translate & Generate Report"
                "original" -> "Original Native Draft"
                "converted" -> "Professional English Draft"
                "choose_lang" -> "Native Language dialect"
                "choose_tone" -> "Target Report Format Layout"
                else -> key
            }
        }
    }
}

// Color Hex Parser Helper
fun parseThemeColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun GuardAiAppContent(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()

    var activeLanguage by remember { mutableStateOf("English") } // English, Hindi, Odia
    val primaryColor = parseThemeColor(config.themeColorHex, Color(0xFF2196F3))

    // Check Maintenance mode bypass for administrators
    val isBypassed = currentUser?.role == "Admin" || currentUser?.role == "Super Admin"
    if (config.isMaintenanceMode && !isBypassed && currentScreen != AppScreen.Splash && currentScreen != AppScreen.Login && currentScreen != AppScreen.Register) {
        MaintenanceModeScreen(config, primaryColor, viewModel)
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212) // Eye-safe pitch dark canvas
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                AppScreen.Splash -> SplashScreen(primaryColor)
                AppScreen.Login -> LoginScreen(viewModel, primaryColor)
                AppScreen.Register -> RegisterScreen(viewModel, primaryColor)
                else -> {
                    val isUserAdmin = currentUser?.role == "Admin" || currentUser?.role == "Super Admin"
                    val showAdminWorkspace = isUserAdmin && isAdminMode

                    if (showAdminWorkspace) {
                        // ==========================================
                        // DEDICATED SEPARATE ADMIN WORKSPACE LAYOUT
                        // ==========================================
                        Scaffold(
                            topBar = {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF1B1B26))
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = "Admin System Drawer",
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        "HQ SYSTEM ADMIN CONSOLE",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.sp
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .background(primaryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            "ROLE: " + (currentUser?.role ?: "Admin").uppercase(),
                                                            color = primaryColor,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Exit Admin Mode -> back to User Mode
                                                Button(
                                                    onClick = { viewModel.toggleAdminMode(false) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C3D)),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.ArrowBack,
                                                            contentDescription = "Exit to User",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("EXIT TO USER PANEL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = { viewModel.executeLogout(context) },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Exit",
                                                        tint = Color.Gray,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFF2B2B3E), thickness = 1.dp)
                                }
                            },
                            containerColor = Color(0xFF0F0F12),
                            contentWindowInsets = WindowInsets.safeDrawing
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AdminDashboardScreen(viewModel, primaryColor)
                            }
                        }
                    } else {
                        // ==========================================
                        // DEDICATED SEPARATE USER WORKSPACE LAYOUT
                        // ==========================================
                        Scaffold(
                            bottomBar = {
                                BottomNavBar(
                                    currentScreen = currentScreen,
                                    onTabSelected = { viewModel.navigateTo(it) },
                                    primaryColor = primaryColor,
                                    userRole = currentUser?.role ?: "User"
                                )
                            },
                            containerColor = Color(0xFF121212),
                            contentWindowInsets = WindowInsets.safeDrawing
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                Column {
                                    // Dynamic Title Bar with language selector
                                    TopHeaderBar(
                                        config = config,
                                        currentUser = currentUser,
                                        activeLanguage = activeLanguage,
                                        onLanguageToggle = {
                                            activeLanguage = when (activeLanguage) {
                                                "English" -> "Hindi"
                                                "Hindi" -> "Odia"
                                                else -> "English"
                                            }
                                        },
                                        onLogout = { viewModel.executeLogout(context) },
                                        primaryColor = primaryColor,
                                        currentScreen = currentScreen,
                                        onBackClicked = { viewModel.navigateBack() }
                                    )

                                    // Banner Ads (Respect parameters)
                                    if (config.adsEnabled && currentUser?.isPremium == false) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Brush.horizontalGradient(listOf(Color(0xFF2E2E2E), Color(0xFF1E1E1E))))
                                                .padding(vertical = 4.dp, horizontal = 12.dp)
                                                .testTag("banner_ad")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(primaryColor, RoundedCornerShape(3.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Ad", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        "Bikash logistics protective agency. Upgrade to star zero ads!",
                                                        color = Color.LightGray,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                TextButton(
                                                    onClick = { viewModel.navigateTo(AppScreen.Premium) },
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Remove", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                                        when (currentScreen) {
                                            AppScreen.Dashboard -> DashboardScreen(viewModel, primaryColor, activeLanguage)
                                            AppScreen.GeneratorHome -> GeneratorHomeScreen(viewModel, primaryColor, activeLanguage)
                                            AppScreen.GeneratorForm -> GeneratorFormScreen(viewModel, primaryColor, activeLanguage)
                                            AppScreen.History -> HistoryAndStarredScreen(viewModel, primaryColor, activeLanguage, showFavoritesOnly = false)
                                            AppScreen.Favorites -> HistoryAndStarredScreen(viewModel, primaryColor, activeLanguage, showFavoritesOnly = true)
                                            AppScreen.AIChat -> ChatScreen(viewModel, primaryColor)
                                            AppScreen.Premium -> PremiumScreen(viewModel, primaryColor)
                                            AppScreen.Profile -> ProfileScreen(viewModel, primaryColor, activeLanguage)
                                            else -> {}
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
}

// ==========================================================
// INDIVIDUAL SCREENS
// ==========================================================

@Composable
fun SplashScreen(primaryColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Visual Badge
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Brush.radialGradient(listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent)))
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security logo",
                    tint = primaryColor,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "GUARD ENGLISH AI",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Security Writing HQ Pro",
                color = primaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = primaryColor, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Created by Bikash Bindhani",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun LoginScreen(viewModel: AppViewModel, primaryColor: Color) {
    val context = LocalContext.current
    val email by viewModel.loginEmail.collectAsStateWithLifecycle()
    val isOpenedFromAdmin by viewModel.isOpenedFromAdminLauncher.collectAsStateWithLifecycle()
    val adminPassword by viewModel.adminPassword.collectAsStateWithLifecycle()
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock Logo",
                tint = primaryColor,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "GUARD ENGLISH AI",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                if (isOpenedFromAdmin) "Admin Command Console" else "Officer Check-In Station",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.loginEmail.value = it },
                label = { Text("Your Registered Email ID") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            if (isOpenedFromAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = adminPassword,
                    onValueChange = { viewModel.adminPassword.value = it },
                    label = { Text("Admin Security PIN") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        androidx.compose.material3.TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Text(
                                text = if (isPasswordVisible) "HIDE" else "SHOW",
                                color = primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        unfocusedBorderColor = Color.DarkGray,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.executeLogin(context) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button")
            ) {
                Text(if (isOpenedFromAdmin) "AUTHORIZE ADMIN LOG IN" else "LOGIN & STATION ON DUTY", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Don't have a station login profile yet?", color = Color.Gray, fontSize = 13.sp)
            TextButton(onClick = { viewModel.navigateTo(AppScreen.Register) }) {
                Text("CREATE NEW OFFICER ACCOUNT", color = primaryColor, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
            // Convenient tap choices
            Text(
                "FAST-ACCESS DEMO ACCOUNTS",
                color = primaryColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            DemoAccountChip("👨‍✈️ General Guard Account (Free)", "guard@guardenglish.com") {
                viewModel.loginEmail.value = "guard@guardenglish.com"
                viewModel.executeLogin(context)
            }
            Spacer(modifier = Modifier.height(6.dp))
            DemoAccountChip("⭐ Premium Guard Account (VIP)", "supervisor@guardenglish.com") {
                viewModel.loginEmail.value = "supervisor@guardenglish.com"
                viewModel.executeLogin(context)
            }
            Spacer(modifier = Modifier.height(6.dp))
            DemoAccountChip("🛠️ Site Admin Dashboard Portal", "admin@guardenglish.com") {
                viewModel.loginEmail.value = "admin@guardenglish.com"
                viewModel.executeLogin(context)
            }
        }
    }
}

@Composable
fun DemoAccountChip(label: String, email: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        border = BorderStroke(1.dp, Color(0xFF2E2E2E))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(email, color = Color.LightGray, fontSize = 11.sp)
            }
            Icon(Icons.Default.ArrowBack, contentDescription = "", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun RegisterScreen(viewModel: AppViewModel, primaryColor: Color) {
    val context = LocalContext.current
    val email by viewModel.regEmail.collectAsStateWithLifecycle()
    val name by viewModel.regName.collectAsStateWithLifecycle()
    val role by viewModel.regRole.collectAsStateWithLifecycle()
    val refCode by viewModel.regReferCode.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "OFFICER ENLISTMENT",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Create your account profile below",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.regEmail.value = it },
                label = { Text("Email ID Address") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.regName.value = it },
                label = { Text("Officer Full Name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = refCode,
                onValueChange = { viewModel.regReferCode.value = it },
                label = { Text("Referral Code (Optional)") },
                placeholder = { Text("Apply 'GUARD99' for bonus") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedBorderColor = Color.DarkGray,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Select Station Role Platform Permissions:",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("User", "Premium User").forEach { roleChoice ->
                    val selected = role == roleChoice
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable { viewModel.regRole.value = roleChoice },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) primaryColor.copy(alpha = 0.2f) else Color(0xFF1E1E1E)
                        ),
                        border = BorderStroke(1.dp, if (selected) primaryColor else Color.DarkGray)
                    ) {
                        Text(
                            roleChoice,
                            color = if (selected) primaryColor else Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.executeRegistration(context) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_registration")
            ) {
                Text("SUBMIT PROFILE CREDENTIALS", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.navigateTo(AppScreen.Login) }) {
                Text("BACK TO SIGN IN STATION", color = primaryColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: AppViewModel, primaryColor: Color, activeLanguage: String) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val savedList by viewModel.savedReports.collectAsStateWithLifecycle()
    val starredList by viewModel.favoriteReports.collectAsStateWithLifecycle()
    val announcementsList by viewModel.announcements.collectAsStateWithLifecycle()

    var activeAnnIndex by remember { mutableIntStateOf(0) }

    // Rolling announcement trigger
    LaunchedEffect(announcementsList) {
        while (true) {
            delay(5000)
            if (announcementsList.isNotEmpty()) {
                activeAnnIndex = (activeAnnIndex + 1) % announcementsList.size
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome and Ticker
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        Translator.getLabel("welcome", activeLanguage),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        currentUser?.name ?: "Officer",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Premium Status Pill
                Box(
                    modifier = Modifier
                        .background(
                            if (currentUser?.isPremium == true) primaryColor.copy(alpha = 0.2f) else Color.DarkGray,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (currentUser?.isPremium == true) primaryColor else Color.Gray,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (currentUser?.isPremium == true) "VIP PREMIUM ACTIVE" else "FREE OFFICER ACCT",
                        color = if (currentUser?.isPremium == true) primaryColor else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ticker Animation
            if (announcementsList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B120C)),
                    border = BorderStroke(1.dp, Color(0xFF9E5616))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alert",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            announcementsList[activeAnnIndex],
                            color = Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Counter grid
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Saved Local Reports",
                    count = savedList.size.toString(),
                    modifier = Modifier.weight(1f),
                    primaryColor = primaryColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Starred Documents",
                    count = starredList.size.toString(),
                    modifier = Modifier.weight(1f),
                    primaryColor = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Limit/Check-in Tracker row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            Translator.getLabel("credits", activeLanguage),
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            if (currentUser?.isPremium == true) "Unlimited Free Usage" else "${currentUser?.credits ?: 0} Credits remaining",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { viewModel.claimDailyCheckIn(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            Translator.getLabel("claim_daily", activeLanguage),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "DISPATCH OFFICER AI SERVICES",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Quick Launch Panels
        item {
            LaunchServiceCard(
                name = "AI English Converter",
                desc = "Translate Odia, Hindi, Hinglish, Tamil to polished Security English layout.",
                icon = Icons.Default.Edit,
                tint = primaryColor,
                onClick = { viewModel.selectGenerator(GeneratorType.Converter) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LaunchServiceCard(
                name = "Attendance Compiler",
                desc = "Compile roll-call files, lists of active and missing guards quickly.",
                icon = Icons.Default.Check,
                tint = primaryColor,
                onClick = { viewModel.selectGenerator(GeneratorType.Attendance) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LaunchServiceCard(
                name = "Incident Generator",
                desc = "Compile structural theft, property damage or security intrusion logs immediately.",
                icon = Icons.Default.Warning,
                tint = primaryColor,
                onClick = { viewModel.selectGenerator(GeneratorType.Incident) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            LaunchServiceCard(
                name = "Shift Handover Slip",
                desc = "Document relieving officer checkpoint key swaps, material checklist reviews.",
                icon = Icons.Default.Person,
                tint = primaryColor,
                onClick = { viewModel.selectGenerator(GeneratorType.Handover) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Show premium action button
            if (currentUser?.isPremium != true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.Premium) },
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, primaryColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "", tint = primaryColor, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UPGRADE TO GUARD VIP PREMIUM", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Get zero Ads, unlimited generator prompts, faster AI models & vip priority.", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StatCard(title: String, count: String, modifier: Modifier, primaryColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(count, color = primaryColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LaunchServiceCard(name: String, desc: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171717))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(tint.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun GeneratorHomeScreen(viewModel: AppViewModel, primaryColor: Color, activeLanguage: String) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text(
                "SELECT REPORT CLASS CLASSROOM",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                "Choose any helper to structure localized observations professionally",
                color = Color.Gray,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(GeneratorType.values()) { type ->
            val details = when (type) {
                GeneratorType.Converter -> Pair("AI English Translator", "Translate raw local thoughts or voice notes directly.")
                GeneratorType.Attendance -> Pair("Attendance Report", "Structure roll-call parameters, sick guards backup roster.")
                GeneratorType.Incident -> Pair("Incident Occurrence Lodge", "Lodge theft, property break-in security records.")
                GeneratorType.Leave -> Pair("Leave Application Creator", "Write professional sick/casual leaves applications templates.")
                GeneratorType.Visitor -> Pair("Visitor Log Entry Tracker", "Logs outsider vehicles credentials, checked identity reports.")
                GeneratorType.Handover -> Pair("Shift Relief Swapping Handover", " swap shift records, armory drawer details.")
                GeneratorType.DailyLog -> Pair("Daily Security Observations Logs", "Total hourly log entries summary checklists.")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { viewModel.selectGenerator(type) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(primaryColor, CircleShape))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(details.first, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(details.second, color = Color.Gray, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun GeneratorFormScreen(viewModel: AppViewModel, primaryColor: Color, activeLanguage: String) {
    val context = LocalContext.current
    val type by viewModel.selectedGenerator.collectAsStateWithLifecycle()
    val isCompiling by viewModel.isGenerating.collectAsStateWithLifecycle()
    val outputReport by viewModel.generatedOutputText.collectAsStateWithLifecycle()

    var isVoiceActive by remember { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.GeneratorHome) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = type.name.uppercase() + " ASSISTANT",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Custom structured input fields depending on chosen generator
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (type) {
                        GeneratorType.Converter -> {
                            val inputText by viewModel.convInputText.collectAsStateWithLifecycle()
                            val srcLang by viewModel.convSrcLang.collectAsStateWithLifecycle()
                            val toneStyle by viewModel.convTone.collectAsStateWithLifecycle()

                            Text("Select Dialect Sources & Accents:", color = Color.Gray, fontSize = 11.sp)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                listOf("Hinglish", "Hindi", "Odia", "Bengali", "Tamil", "Telugu").forEach { lang ->
                                    val act = srcLang == lang
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .background(if (act) primaryColor else Color.DarkGray, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.convSrcLang.value = lang }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(lang, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Target English Delivery Tone:", color = Color.Gray, fontSize = 11.sp)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                listOf("Formal Corporate", "WhatsApp Ready Formatting", "Executive Security Detail").forEach { t ->
                                    val act = toneStyle == t
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .background(if (act) primaryColor else Color.DarkGray, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.convTone.value = t }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(t, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Type your local observation sentence draft here:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { viewModel.convInputText.value = it },
                                placeholder = { Text("e.g. Aaj night shift me gate 2 par security guard Rameswar so gaya tha, action liya gaya hai") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.DarkGray,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .testTag("ai_converter_input")
                            )
                        }

                        GeneratorType.Attendance -> {
                            val staffName by viewModel.attStaffName.collectAsStateWithLifecycle()
                            val agency by viewModel.attAgency.collectAsStateWithLifecycle()
                            val totalStaff by viewModel.attTotalStaff.collectAsStateWithLifecycle()
                            val absentCount by viewModel.attAbsentCount.collectAsStateWithLifecycle()
                            val remarks by viewModel.attRemarks.collectAsStateWithLifecycle()

                            FormFieldLayout("Supervisor Duty Officer Name:") {
                                OutlinedTextField(
                                    value = staffName,
                                    onValueChange = { viewModel.attStaffName.value = it },
                                    placeholder = { Text("e.g. Officer Bikash") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Provider Logistics Security Agency Name:") {
                                OutlinedTextField(
                                    value = agency,
                                    onValueChange = { viewModel.attAgency.value = it },
                                    placeholder = { Text("e.g. Iron Clad Security Force") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                    FormFieldLayout("Active Guard Count On-duty:") {
                                        OutlinedTextField(
                                            value = totalStaff,
                                            onValueChange = { viewModel.attTotalStaff.value = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                    FormFieldLayout("Absent Guards Count:") {
                                        OutlinedTextField(
                                            value = absentCount,
                                            onValueChange = { viewModel.attAbsentCount.value = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Supervisor General Remarks:") {
                                OutlinedTextField(
                                    value = remarks,
                                    onValueChange = { viewModel.attRemarks.value = it },
                                    placeholder = { Text("No sick guards reported. Swapping verified.") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        GeneratorType.Incident -> {
                            val incident by viewModel.incType.collectAsStateWithLifecycle()
                            val dt by viewModel.incDateTime.collectAsStateWithLifecycle()
                            val spot by viewModel.incLocation.collectAsStateWithLifecycle()
                            val description by viewModel.incDetails.collectAsStateWithLifecycle()
                            val actions by viewModel.incAction.collectAsStateWithLifecycle()

                            FormFieldLayout("Incident Classification Category:") {
                                OutlinedTextField(
                                    value = incident,
                                    onValueChange = { viewModel.incType.value = it },
                                    placeholder = { Text("e.g. Theft of wire scrap") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Incident Date / Check-in Hour:") {
                                OutlinedTextField(
                                    value = dt,
                                    onValueChange = { viewModel.incDateTime.value = it },
                                    placeholder = { Text("e.g. 08th Jun - 10:45 PM") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Occurrence Spot Location:") {
                                OutlinedTextField(
                                    value = spot,
                                    onValueChange = { viewModel.incLocation.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Raw Incident Happenings (Describe fully):") {
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { viewModel.incDetails.value = it },
                                    placeholder = { Text("Write raw details...") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth().height(100.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Immediate Corrective Actions taken by guards:") {
                                OutlinedTextField(
                                    value = actions,
                                    onValueChange = { viewModel.incAction.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        GeneratorType.Leave -> {
                            val reason by viewModel.lvReason.collectAsStateWithLifecycle()
                            val fromS by viewModel.lvFromDate.collectAsStateWithLifecycle()
                            val toS by viewModel.lvToDate.collectAsStateWithLifecycle()
                            val backS by viewModel.lvReliever.collectAsStateWithLifecycle()

                            FormFieldLayout("Reason for leave request:") {
                                OutlinedTextField(
                                    value = reason,
                                    onValueChange = { viewModel.lvReason.value = it },
                                    placeholder = { Text("Home emergency/Family health checkup") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                    FormFieldLayout("From Date:") {
                                        OutlinedTextField(
                                            value = fromS,
                                            onValueChange = { viewModel.lvFromDate.value = it },
                                            placeholder = { Text("e.g. 10th June") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                    FormFieldLayout("To Date:") {
                                        OutlinedTextField(
                                            value = toS,
                                            onValueChange = { viewModel.lvToDate.value = it },
                                            placeholder = { Text("e.g. 12th June") },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Relieving Backup Guard Name:") {
                                OutlinedTextField(
                                    value = backS,
                                    onValueChange = { viewModel.lvReliever.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        GeneratorType.Visitor -> {
                            val visitorName by viewModel.visName.collectAsStateWithLifecycle()
                            val company by viewModel.visCompany.collectAsStateWithLifecycle()
                            val purpose by viewModel.visReason.collectAsStateWithLifecycle()
                            val entryHour by viewModel.visInTime.collectAsStateWithLifecycle()
                            val exitHour by viewModel.visOutTime.collectAsStateWithLifecycle()
                            val docOk by viewModel.visCardChecked.collectAsStateWithLifecycle()

                            FormFieldLayout("Visitor Full Name:") {
                                OutlinedTextField(
                                    value = visitorName,
                                    onValueChange = { viewModel.visName.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Representing Organization/Identity:") {
                                OutlinedTextField(
                                    value = company,
                                    onValueChange = { viewModel.visCompany.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Visit Objective/Goal:") {
                                OutlinedTextField(
                                    value = purpose,
                                    onValueChange = { viewModel.visReason.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                    FormFieldLayout("Gate Check-in Time:") {
                                        OutlinedTextField(
                                            value = entryHour,
                                            onValueChange = { viewModel.visInTime.value = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                    FormFieldLayout("Gate Departure Time:") {
                                        OutlinedTextField(
                                            value = exitHour,
                                            onValueChange = { viewModel.visOutTime.value = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ID Document verified at checkpoint", color = Color.White, fontSize = 12.sp)
                                Switch(
                                    checked = docOk,
                                    onCheckedChange = { viewModel.visCardChecked.value = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                                )
                            }
                        }

                        GeneratorType.Handover -> {
                            val outgoing by viewModel.handOutgoing.collectAsStateWithLifecycle()
                            val relief by viewModel.handIncoming.collectAsStateWithLifecycle()
                            val keysBack by viewModel.handKeys.collectAsStateWithLifecycle()
                            val logOk by viewModel.handLogbook.collectAsStateWithLifecycle()
                            val noteText by viewModel.handNotes.collectAsStateWithLifecycle()

                            FormFieldLayout("Departing Security Officer name:") {
                                OutlinedTextField(
                                    value = outgoing,
                                    onValueChange = { viewModel.handOutgoing.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Relief Oncoming Guard Name:") {
                                OutlinedTextField(
                                    value = relief,
                                    onValueChange = { viewModel.handIncoming.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Keys/Drawe swapped correct?", color = Color.White, fontSize = 12.sp)
                                Switch(checked = keysBack, onCheckedChange = { viewModel.handKeys.value = it }, colors = SwitchDefaults.colors(checkedThumbColor = primaryColor))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Daily Occurrence Register signed?", color = Color.White, fontSize = 12.sp)
                                Switch(checked = logOk, onCheckedChange = { viewModel.handLogbook.value = it }, colors = SwitchDefaults.colors(checkedThumbColor = primaryColor))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Specific Swapping Instructions Notes:") {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { viewModel.handNotes.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        GeneratorType.DailyLog -> {
                            val shiftLabel by viewModel.dlogShift.collectAsStateWithLifecycle()
                            val patCount by viewModel.dlogPatrolCount.collectAsStateWithLifecycle()
                            val vehCount by viewModel.dlogVehiclesIn.collectAsStateWithLifecycle()
                            val logRemarks by viewModel.dlogRemarks.collectAsStateWithLifecycle()

                            FormFieldLayout("Guard Shift Label:") {
                                OutlinedTextField(
                                    value = shiftLabel,
                                    onValueChange = { viewModel.dlogShift.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                    FormFieldLayout("Patrols Completed:") {
                                        OutlinedTextField(
                                            value = patCount,
                                            onValueChange = { viewModel.dlogPatrolCount.value = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                                    FormFieldLayout("Gate Vehicles Count:") {
                                        OutlinedTextField(
                                            value = vehCount,
                                            onValueChange = { viewModel.dlogVehiclesIn.value = it },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FormFieldLayout("Shift Occurrence Log Summary notes:") {
                                OutlinedTextField(
                                    value = logRemarks,
                                    onValueChange = { viewModel.dlogRemarks.value = it },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice and Generate Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Simulated speech input button
                        IconButton(
                            onClick = {
                                coroutine.launch {
                                    isVoiceActive = true
                                    delay(2000) // Simulated speaking check listener
                                    isVoiceActive = false
                                    // Prepopulate fields dynamically with simulated localized speech entry
                                    when (type) {
                                        GeneratorType.Converter -> viewModel.convInputText.value = "sir back gate par light kharab hai please repair karwa dijiye report lock kiya."
                                        GeneratorType.Attendance -> viewModel.attRemarks.value = "Uniform clean, physical drill briefing passed, all security personnel ready on post."
                                        GeneratorType.Incident -> viewModel.incDetails.value = "Kuch outsiders boundary wall cross karke scrap material uthane ka try kar rhe the, unhe bhaya."
                                        GeneratorType.Leave -> viewModel.lvReason.value = "Village medical check-up reference for my family member, need emergency break out."
                                        GeneratorType.Visitor -> viewModel.visReason.value = "Electric maintenance inspection representative team visit."
                                        GeneratorType.Handover -> viewModel.handNotes.value = "Handed over material checking register, flashlights tested, charger grid powered."
                                        GeneratorType.DailyLog -> viewModel.dlogRemarks.value = "Main entrance surveillance stream checked, fire alarm backup check passed."
                                    }
                                    Toast.makeText(context, "🎤 Voice received & translated natively!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .background(if (isVoiceActive) Color.Red else Color.DarkGray, CircleShape)
                                .size(48.dp)
                                .testTag("voice_trigger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow, // Standin mic icon representation
                                contentDescription = "Voice",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { viewModel.runAIGeneration(context) },
                            enabled = !isCompiling,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .weight(1f)
                                .testTag("generate_report_button")
                        ) {
                            if (isCompiling) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("CONVERT INTO ENGLISH", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isVoiceActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "🎤 LISTENING TO YOUR VOICE REPORT (TAP TO CONFIRM)...",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        // OUTPUT PREVIEW DISPLAY PANEL
        if (outputReport.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "PROFESSIONAL COMPILED REPORT PREVIEW",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2830)),
                    border = BorderStroke(2.dp, primaryColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "GUARD REPORT LOG SHEET",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(primaryColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("AI APPROVED", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Divider(color = primaryColor.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Styled Text Body
                        Text(
                            text = outputReport,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier.testTag("ai_output_box")
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Divider(color = primaryColor.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Instant Action commands Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Copy
                            IconButton(onClick = { viewModel.actionCopyText(outputReport, context) }) {
                                Icon(Icons.Default.Add, contentDescription = "Copy text", tint = Color.LightGray)
                            }

                            // Share Choose Intends
                            IconButton(onClick = { viewModel.actionShareText(outputReport, context) }) {
                                Icon(Icons.Default.Share, contentDescription = "Share text", tint = Color.LightGray)
                            }

                            // PDF simulator download
                            IconButton(onClick = { viewModel.actionExportPdf(type.name, outputReport, context) }) {
                                Icon(Icons.Default.Info, contentDescription = "Export PDF file option", tint = Color.LightGray)
                            }

                            // Primary WhatsApp formatted quick trigger
                            Button(
                                onClick = { viewModel.actionSendWhatsApp(outputReport, context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WHATSAPP READY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
fun FormFieldLayout(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
fun HistoryAndStarredScreen(
    viewModel: AppViewModel,
    primaryColor: Color,
    activeLanguage: String,
    showFavoritesOnly: Boolean
) {
    val context = LocalContext.current
    val systemReports by (if (showFavoritesOnly) viewModel.favoriteReports else viewModel.savedReports).collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                if (showFavoritesOnly) "STARRED SAVED ARCHIVES" else "LOCAL OFFLINE REPORTS LOGS",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                if (showFavoritesOnly) "Starred safety checkpoint logs" else "Verify previously drafted English applications safely cached",
                color = Color.Gray,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (systemReports.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = "", tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No reports found inside local device caches.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(systemReports) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    border = BorderStroke(1.dp, Color(0xFF2E2E2E))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(report.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(report.repType + " Class Helper", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Row {
                                IconButton(onClick = { viewModel.toggleFavoriteReport(report) }) {
                                    Icon(
                                        imageVector = if (report.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Star favorite",
                                        tint = if (report.isFavorite) Color.Red else Color.Gray
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteReport(report, context) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            report.translatedText,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { viewModel.actionCopyText(report.translatedText, context) }) {
                                Text("COPY TEXT", color = primaryColor, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.actionSendWhatsApp(report.translatedText, context) }) {
                                Text("WHATSAPP SEND", color = Color(0xFF25D366), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: AppViewModel, primaryColor: Color) {
    val context = LocalContext.current
    val sessions by viewModel.chatSessions.collectAsStateWithLifecycle()
    val activeMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val activeSessId by viewModel.activeSessionId.collectAsStateWithLifecycle()
    val chatInput by viewModel.chatInputText.collectAsStateWithLifecycle()
    val isRepLoading by viewModel.isGeneratingChat.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    val filteredSessions = sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Desktop Rail Sidebar for active discussion folders List
        Column(
            modifier = Modifier
                .width(110.dp)
                .background(Color(0xFF171717))
                .padding(4.dp)
        ) {
            Button(
                onClick = { viewModel.createNewChatThread() },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("new_chat_button")
            ) {
                Text("NEW CHAT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search", fontSize = 9.sp) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 10.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredSessions) { sess ->
                    val isSelected = activeSessId == sess.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectChatSession(sess.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.Transparent
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) primaryColor else if (sess.isPinned) Color.Yellow.copy(alpha = 0.4f) else Color.Transparent
                        )
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (sess.isPinned) {
                                    Icon(Icons.Default.Star, contentDescription = "", tint = Color.Yellow, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text(
                                    sess.title,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "",
                                    tint = if (sess.isPinned) Color.Yellow else Color.Gray,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { viewModel.pinChatSession(sess.id, !sess.isPinned) }
                                )
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { viewModel.deleteChatSession(sess.id, context) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ACTIVE MESSAGE CONVERSATIVE CONTAINER
        Column(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF121212))
                .padding(8.dp)
        ) {
            if (activeSessId.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = "", tint = Color.DarkGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active discussion selected.", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.createNewChatThread() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Open New Incident Thread", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Thread title banner and rename controller
                var isRenameOpen by remember { mutableStateOf(false) }
                var renamingText by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isRenameOpen) {
                        OutlinedTextField(
                            value = renamingText,
                            onValueChange = { renamingText = it },
                            placeholder = { Text("Enter Title") },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor),
                            modifier = Modifier.width(130.dp)
                        )
                        IconButton(onClick = {
                            viewModel.renameChatSession(activeSessId, renamingText)
                            isRenameOpen = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "", tint = primaryColor)
                        }
                    } else {
                        val activeTitle = sessions.firstOrNull { it.id == activeSessId }?.title ?: "Chat"
                        Text(
                            activeTitle,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            renamingText = activeTitle
                            isRenameOpen = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat bubble log LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = false
                ) {
                    items(activeMessages) { msg ->
                        val isUser = msg.role == "user"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isUser) primaryColor.copy(alpha = 0.2f) else Color(0xFF1C1C1E),
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        if (isUser) primaryColor else Color.DarkGray,
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .padding(10.dp)
                                    .widthIn(max = 180.dp)
                            ) {
                                Column {
                                    Text(
                                        msg.text,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                    if (msg.modelName.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Engine: " + msg.modelName,
                                            color = primaryColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            // AI response helper controllers (Regenerate, Copy etc)
                            if (!isUser) {
                                Row(
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                ) {
                                    Text(
                                        "Copy",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable { viewModel.actionCopyText(msg.text, context) }
                                            .padding(4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Share",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable { viewModel.actionShareText(msg.text, context) }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // AI responding triggers loading placeholder
                if (isRepLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guard AI is structuring reply...", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }

                // Bottom prompt buttons row (Continue response, regenerate)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { viewModel.continueResponse() }) {
                        Text("CONTINUE RESPONSE", color = primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { viewModel.regenerateResponse() }) {
                        Text("REGENERATE RESPONSE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Message Text Field prompt inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { viewModel.chatInputText.value = it },
                        placeholder = { Text("Write query or report prompt details...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.triggerSendAndGenerate() },
                        modifier = Modifier
                            .background(primaryColor, CircleShape)
                            .size(44.dp)
                            .testTag("send_chat_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumScreen(viewModel: AppViewModel, primaryColor: Color) {
    val context = LocalContext.current
    var activePlanSelected by remember { mutableStateOf("1 Month") }
    var upiEnteredValue by remember { mutableStateOf("") }

    val benefits = listOf(
        "⚡ Prompt priority response - faster results!",
        "🤖 VIP Pro OpenRouter and Gemini Models.",
        "🔥 Zero interstitial banner Ads in the app.",
        "📋 Unlimited daily report prompt generations.",
        "📁 Direct PDF simulated offline file compilation."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(primaryColor, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "", tint = Color.Black, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "GUARD AI PREMIUM",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "Unleash ultimate executive writing power",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("PREMIUM SUBSCRIPTION PLANS:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Plan Grid
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    Pair("1 Month", "₹199"),
                    Pair("3 Month", "₹499"),
                    Pair("6 Month", "₹899"),
                    Pair("12 Month", "₹1499")
                ).forEach { planDetail ->
                    val isChecked = planDetail.first == activePlanSelected
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clickable { activePlanSelected = planDetail.first },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isChecked) primaryColor.copy(alpha = 0.15f) else Color(0xFF1E1E1E)
                        ),
                        border = BorderStroke(1.dp, if (isChecked) primaryColor else Color.DarkGray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(planDetail.first, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(planDetail.second, color = primaryColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("PREMIUM MEMBER BENEFITS:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            benefits.forEach { b ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = "", tint = primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(b, color = Color.LightGray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sim UPI section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SECURE UPI/CREDIT CARD CHECKOUT:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Enter your PhonePe/GPay UPI ID below. A payment ticket request is dispatched to our Master Admin Panel instantly for approval.",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = upiEnteredValue,
                        onValueChange = { upiEnteredValue = it },
                        label = { Text("UPI ID address (e.g. officer@ybl)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_input_field")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.performSimulatedPurchase(activePlanSelected, upiEnteredValue, context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("subscribe_plan_button")
                    ) {
                        Text("SUBMIT UPI INBOUND PAYMENT TICKET", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: AppViewModel, primaryColor: Color, activeLanguage: String) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (currentUser?.name ?: "O").take(2).uppercase(),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(currentUser?.name ?: "Bikash", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(currentUser?.email ?: "", color = Color.LightGray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Role pill
                    Box(
                        modifier = Modifier
                            .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "STATION ROLE: " + (currentUser?.role ?: "User").uppercase(),
                            color = primaryColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("STATION REFERRAL SYSTEM:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Your Security Referral Link Code:", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        currentUser?.referralCode ?: "GUARD99",
                        color = primaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Share this code during enlistment. If colleagues register with your code, both receive +5 bonus generation credits in the station balances!",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.actionShareText(
                                "Hey! Join GUARD ENGLISH AI- your professional writing buddy! Use my code: ${currentUser?.referralCode ?: "GUARD99"} to unlock bonus credits!",
                                context
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SHARE COMMISSION LINK CODE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (config.instagramId.isNotEmpty() || config.telegramGroup.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("OFFICIAL SOCIAL CHANNELS & FEEDS:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        if (config.instagramId.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.actionOpenUrl(config.instagramId, context)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Instagram link",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Follow Instagram Page", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (config.telegramGroup.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.actionOpenUrl(config.telegramGroup, context)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Telegram Group link",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Join Telegram Channel / Group", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Technical developer support notes
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SUPPORT & CREDITS:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Platform Developer: Bikash Bindhani", color = Color.LightGray, fontSize = 11.sp)
                    Text("Device logs checked count: ${currentUser?.loginDeviceCount ?: 1}", color = Color.LightGray, fontSize = 11.sp)
                    Text("Current active credits: ${currentUser?.credits ?: 0}", color = Color.LightGray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.executeLogout(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Text("DISMISS STATION & LOG OUT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MaintenanceModeScreen(config: AppSystemConfig, primaryColor: Color, viewModel: AppViewModel) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Warning, contentDescription = "", tint = Color(0xFFFF5722), modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "SYSTEM UNDER SHIFT HANDOVER",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                config.websiteName + " is currently in Maintenance mode requested by the security admin site director. Please stand-by or try logging in as administrator.",
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.navigateTo(AppScreen.Login) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("LOGIN AS STATION ROOT ADMIN", color = Color.White)
            }
        }
    }
}

// ==========================================================
// NAVIGATION COMPONENTS
// ==========================================================

@Composable
fun TopHeaderBar(
    config: AppSystemConfig,
    currentUser: UserAccount?,
    activeLanguage: String,
    onLanguageToggle: () -> Unit,
    onLogout: () -> Unit,
    primaryColor: Color,
    currentScreen: AppScreen,
    onBackClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isEscapable = currentScreen != AppScreen.Dashboard && currentScreen != AppScreen.Login && currentScreen != AppScreen.Register
            if (isEscapable) {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Icon(Icons.Default.Lock, contentDescription = "Guard Logo", tint = primaryColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                config.websiteName.uppercase(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Multi-Language button toggle
            Button(
                onClick = onLanguageToggle,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier
                    .height(28.dp)
                    .testTag("language_toggle")
            ) {
                Text(
                    text = activeLanguage.take(3).uppercase(),
                    color = primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fast help trigger
            IconButton(onClick = onLogout, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Person, contentDescription = "Exit", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    primaryColor: Color,
    userRole: String
) {
    NavigationBar(
        containerColor = Color(0xFF1E1E1E),
        modifier = Modifier.testTag("app_bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.Dashboard,
            onClick = { onTabSelected(AppScreen.Dashboard) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Hub", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                indicatorColor = primaryColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_tab_hub")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.GeneratorHome || currentScreen == AppScreen.GeneratorForm,
            onClick = { onTabSelected(AppScreen.GeneratorHome) },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Writers") },
            label = { Text("Writers", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                indicatorColor = primaryColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_tab_writers")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.AIChat,
            onClick = { onTabSelected(AppScreen.AIChat) },
            icon = { Icon(Icons.Default.Send, contentDescription = "Chat thread") },
            label = { Text("Chat", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                indicatorColor = primaryColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_tab_chat")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.History || currentScreen == AppScreen.Favorites,
            onClick = { onTabSelected(AppScreen.History) },
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Logs archives") },
            label = { Text("History", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                indicatorColor = primaryColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_tab_history")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.Profile,
            onClick = { onTabSelected(AppScreen.Profile) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryColor,
                selectedTextColor = primaryColor,
                indicatorColor = primaryColor.copy(alpha = 0.15f),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray
            ),
            modifier = Modifier.testTag("nav_tab_profile")
        )
    }
}

// ==========================================================
// MASTER ADMIN DASHBOARD CONSOLE
// ==========================================================

@Composable
fun AdminDashboardScreen(viewModel: AppViewModel, primaryColor: Color) {
    val context = LocalContext.current
    val totalRevenueEarned by viewModel.totalRevenue.collectAsStateWithLifecycle()
    val reportsGeneratedList by viewModel.savedReports.collectAsStateWithLifecycle()
    val payments by viewModel.paymentsList.collectAsStateWithLifecycle()
    val users by viewModel.usersList.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val logs by viewModel.adminLogs.collectAsStateWithLifecycle()

    var activeAdminSubTab by remember { mutableStateOf("STATISTICS") } // STATISTICS, USERS, UPI, SETTINGS, ENGINE_LOGS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("MASTER SITE SECURITY CONSOLE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Controls and statistics overridden station commands", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Tab selection banner
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("STATISTICS", "USERS", "UPI APPROVALS", "SETTINGS", "ENGINE LOGS").forEach { tab ->
                    val chosen = activeAdminSubTab == tab
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .background(
                                if (chosen) primaryColor else Color(0xFF1E1E1E),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { activeAdminSubTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            tab,
                            color = if (chosen) Color.Black else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        when (activeAdminSubTab) {
            "STATISTICS" -> {
                item {
                    // Analytical Cards grid
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard("Platform Total Users", users.size.toString(), Modifier.weight(1f), primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        StatCard("Premium Subscribers", users.filter { it.isPremium }.size.toString(), Modifier.weight(1f), primaryColor)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard("Total Income approved", "₹$totalRevenueEarned", Modifier.weight(1f), primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        StatCard("Reports Generated", reportsGeneratedList.size.toString(), Modifier.weight(1f), primaryColor)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Simple progress bars representing features popularity
                    Text("POPULAR FEATURE MODULES UTILIZATION STATISTICS:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    FeaturePopularityGauge("AI English Translator Detail", 0.85f, primaryColor)
                    FeaturePopularityGauge("Incident Logs Class Assist", 0.65f, primaryColor)
                    FeaturePopularityGauge("Attendance Compiler Checkpoint", 0.50f, primaryColor)
                    FeaturePopularityGauge("Direct WhatsApp Messenger Copy", 0.90f, primaryColor)
                }
            }

            "USERS" -> {
                item {
                    Text("MANAGE ENLISTED USERS ACCOUNTS:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (users.isEmpty()) {
                    item { Text("No users found", color = Color.Gray, fontSize = 12.sp) }
                } else {
                    items(users) { usr ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(usr.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(usr.email, color = Color.LightGray, fontSize = 11.sp)
                                        Text("Access Authorization: " + usr.role, color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(if (usr.isBanned) Color.Red else Color.Green, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (usr.isBanned) "Banned" else "Active",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(onClick = { viewModel.adminBanUser(usr.email, !usr.isBanned, context) }) {
                                        Text(if (usr.isBanned) "REVOKE BAN" else "BAN ACCOUNT", color = Color.Red, fontSize = 11.sp)
                                    }

                                    TextButton(onClick = {
                                        viewModel.adminSetPremium(usr.email, context)
                                    }) {
                                        Text("SET PREMIUM", color = primaryColor, fontSize = 11.sp)
                                    }

                                    TextButton(onClick = { viewModel.adminDeleteUser(usr.email, context) }) {
                                        Text("DELETE", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "UPI APPROVALS" -> {
                item {
                    Text("PENDING PREMIUM TRANSACTIONS TICKET QUEUE:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val pendingPayments = payments.filter { it.status == "Pending" }

                if (pendingPayments.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No pending UPI payment checks at this hour.", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(pendingPayments) { pay ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Premium Member Request: " + pay.email, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Inbound Reference: " + pay.txReference, color = Color.LightGray, fontSize = 11.sp)
                                Text("UPI Address: " + pay.upiId, color = Color.LightGray, fontSize = 11.sp)
                                Text("Billing Amount Plan: ${pay.planName} (Rs ${pay.amount})", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { viewModel.adminApprovePayment(pay.id, false, context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                                    ) {
                                        Text("REJECT", color = Color.White, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.adminApprovePayment(pay.id, true, context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                                    ) {
                                        Text("APPROVE PAY", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "SETTINGS" -> {
                item {
                    Text("EDIT GENERAL PLATFORM OVERRIDES PARAMETERS:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    // Direct mutable variables inside Admin UI
                    var webNameVal by remember { mutableStateOf(config.websiteName) }
                    var themeColorVal by remember { mutableStateOf(config.themeColorHex) }
                    var adsEnabledVal by remember { mutableStateOf(config.adsEnabled) }
                    var isMaintVal by remember { mutableStateOf(config.isMaintenanceMode) }
                    var openRouterKeyVal by remember { mutableStateOf(config.openRouterApiKey) }
                    var freeLimVal by remember { mutableStateOf(config.freeDailyLimit.toString()) }
                    var premLimVal by remember { mutableStateOf(config.premiumDailyLimit.toString()) }
                    var defModelVal by remember { mutableStateOf(config.defaultModel) }
                    var instagramIdVal by remember { mutableStateOf(config.instagramId) }
                    var telegramGroupVal by remember { mutableStateOf(config.telegramGroup) }

                    OutlinedTextField(
                        value = webNameVal,
                        onValueChange = { webNameVal = it },
                        label = { Text("Website / Application Title Branding") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = themeColorVal,
                        onValueChange = { themeColorVal = it },
                        label = { Text("Hex Accent Paint Pattern (e.g., #2196F3)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Configure Default Model Endpoint Target:", color = Color.Gray, fontSize = 11.sp)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("gemini-3.5-flash", "z-ai/glm-4.5-air:free", "google/gemini-2.5-flash", "deepseek/deepseek-chat-v3").forEach { m ->
                            val selected = defModelVal == m
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .background(if (selected) primaryColor else Color.DarkGray, RoundedCornerShape(8.dp))
                                    .clickable { defModelVal = m }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(m, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = defModelVal,
                        onValueChange = { defModelVal = it },
                        label = { Text("Custom Model ID (e.g. gemini-1.5-pro)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = openRouterKeyVal,
                        onValueChange = { openRouterKeyVal = it },
                        label = { Text("OpenRouter Access Bearer Key (Optional)") },
                        placeholder = { Text("sk-or-...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Stream Social Integration Links:", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = instagramIdVal,
                        onValueChange = { instagramIdVal = it },
                        label = { Text("Instagram Profile URL (e.g. https://instagram.com/username)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = telegramGroupVal,
                        onValueChange = { telegramGroupVal = it },
                        label = { Text("Telegram Group/Channel Link (e.g. https://t.me/group)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                            OutlinedTextField(
                                value = freeLimVal,
                                onValueChange = { freeLimVal = it },
                                label = { Text("Daily Free Limit") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                            OutlinedTextField(
                                value = premLimVal,
                                onValueChange = { premLimVal = it },
                                label = { Text("Premium Limit") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Simulate AdMob Banner Placement", color = Color.White, fontSize = 12.sp)
                        Switch(checked = adsEnabledVal, onCheckedChange = { adsEnabledVal = it }, colors = SwitchDefaults.colors(checkedThumbColor = primaryColor))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Activate Force Maintenance Mode", color = Color.White, fontSize = 12.sp)
                        Switch(checked = isMaintVal, onCheckedChange = { isMaintVal = it }, colors = SwitchDefaults.colors(checkedThumbColor = primaryColor))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.adminModifySettings(
                                webName = webNameVal,
                                themeColor = themeColorVal,
                                isMaintenance = isMaintVal,
                                adsEnabled = adsEnabledVal,
                                freeLimit = freeLimVal.toIntOrNull() ?: 15,
                                premiumLimit = premLimVal.toIntOrNull() ?: 9999,
                                selectedDefaultModel = defModelVal,
                                orKey = openRouterKeyVal,
                                instaId = instagramIdVal,
                                teleGrp = telegramGroupVal,
                                context = context
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("SAVE OVERRIDES PLATFORM CREDENTIALS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            "ENGINE LOGS" -> {
                item {
                    Text("LIVE PLATFORM TECHNICAL OPERATIONS ENGINE LOGS:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (logs.isEmpty()) {
                    item { Text("No current operations logs. Go use features to create traces.", color = Color.Gray, fontSize = 11.sp) }
                } else {
                    items(logs) { logTrace ->
                        Text(
                            text = logTrace,
                            color = Color.Green,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturePopularityGauge(featureName: String, fraction: Float, primaryColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(featureName, color = Color.LightGray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = primaryColor,
            trackColor = Color.DarkGray,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
