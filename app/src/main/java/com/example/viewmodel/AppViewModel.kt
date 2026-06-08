package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class AppScreen {
    Splash,
    Login,
    Register,
    Dashboard,
    GeneratorHome,
    GeneratorForm,
    History,
    Favorites,
    AIChat,
    Premium,
    AdminDashboard,
    Profile
}

enum class GeneratorType {
    Converter,
    Attendance,
    Incident,
    Leave,
    Visitor,
    Handover,
    DailyLog
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "AppViewModel"
    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "guard_ai.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    val repository: AppRepository by lazy {
        AppRepository(application, db)
    }

    // ==========================================
    // UI STREAM STATE
    // ==========================================
    
    private val _currentScreen = MutableStateFlow(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _isOpenedFromAdminLauncher = MutableStateFlow(false)
    val isOpenedFromAdminLauncher: StateFlow<Boolean> = _isOpenedFromAdminLauncher.asStateFlow()

    fun markOpenedFromAdminLauncher() {
        _isOpenedFromAdminLauncher.value = true
        _isAdminMode.value = true
    }

    fun toggleAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
        if (enabled) {
            _currentScreen.value = AppScreen.AdminDashboard
        } else {
            _currentScreen.value = AppScreen.Dashboard
        }
    }

    // Screen backstack history to permit back button support
    private val _screenBackStack = mutableListOf<AppScreen>()

    // Current screen properties
    private val _selectedGenerator = MutableStateFlow(GeneratorType.Converter)
    val selectedGenerator: StateFlow<GeneratorType> = _selectedGenerator.asStateFlow()

    val currentUser: StateFlow<UserAccount?> = repository.currentUserFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentEmail: StateFlow<String> = repository.currentUserEmail

    val appConfig: StateFlow<AppSystemConfig> = repository.configFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSystemConfig()
    )

    // Lists observed from database
    val chatSessions: StateFlow<List<ChatSession>> = repository.chatSessionsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedReports: StateFlow<List<Report>> = repository.reportsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteReports: StateFlow<List<Report>> = repository.favoritesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val paymentsList: StateFlow<List<PaymentTx>> = repository.paymentsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val usersList: StateFlow<List<UserAccount>> = repository.allUsersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ==========================================
    // GENERATOR FORM STATES
    // ==========================================
    
    // 1. Converter
    var convInputText = MutableStateFlow("")
    var convSrcLang = MutableStateFlow("Hinglish") // Hinglish, Odia, Bengali, Tamil, etc.
    var convTone = MutableStateFlow("Formal Security English") // Formal, WhatsApp Format, Corporate, Guard Duty English

    // 2. Attendance
    var attStaffName = MutableStateFlow("")
    var attAgency = MutableStateFlow("")
    var attTotalStaff = MutableStateFlow("12")
    var attAbsentCount = MutableStateFlow("1")
    var attRemarks = MutableStateFlow("All guards wearing neat tidy uniforms on duty.")

    // 3. Incident
    var incType = MutableStateFlow("Unauthorized Entry Attempts") // Theft, Fire, Damage, Intrusion
    var incDateTime = MutableStateFlow("")
    var incLocation = MutableStateFlow("Gate No 2 Admin Block")
    var incDetails = MutableStateFlow("Spotted a delivery executive entering without vehicle log entries...")
    var incAction = MutableStateFlow("Challenged at security checkpoint, log entries taken, supervisor informed.")

    // 4. Leave
    var lvReason = MutableStateFlow("Daughter health check-in, need to go home")
    var lvFromDate = MutableStateFlow("")
    var lvToDate = MutableStateFlow("")
    var lvReliever = MutableStateFlow("Guard Ramesh Kumar")

    // 5. Visitor Log
    var visName = MutableStateFlow("")
    var visCompany = MutableStateFlow("Amazon Delivery")
    var visReason = MutableStateFlow("Regular courier package delivery")
    var visInTime = MutableStateFlow("10:30 AM")
    var visOutTime = MutableStateFlow("10:45 AM")
    var visCardChecked = MutableStateFlow(true)

    // 6. Handover
    var handOutgoing = MutableStateFlow("")
    var handIncoming = MutableStateFlow("")
    var handKeys = MutableStateFlow(true)
    var handLogbook = MutableStateFlow(true)
    var handNotes = MutableStateFlow("Visitor list handed over, patrol vehicle keys verified.")

    // 7. Daily Security Log
    var dlogShift = MutableStateFlow("Day Shift (08:00 - 20:00)")
    var dlogPatrolCount = MutableStateFlow("4")
    var dlogVehiclesIn = MutableStateFlow("24")
    var dlogRemarks = MutableStateFlow("No structural issues, power supply backup active.")

    // AI Generation Results
    private val _generatedOutputText = MutableStateFlow("")
    val generatedOutputText: StateFlow<String> = _generatedOutputText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // ==========================================
    // CHAT SYSTEM STATE
    // ==========================================
    
    private val _activeSessionId = MutableStateFlow("")
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    var chatInputText = MutableStateFlow("")
    var searchChatQuery = MutableStateFlow("")
    
    private val _isGeneratingChat = MutableStateFlow(false)
    val isGeneratingChat: StateFlow<Boolean> = _isGeneratingChat.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessage>> = _activeSessionId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else repository.getMessagesForSession(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ==========================================
    // ADMIN ANALYTICS STATE
    // ==========================================
    val totalRevenue: StateFlow<Int> = paymentsList.map { list ->
        list.filter { it.status == "Approved" }.sumOf { it.amount }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val adminLogs = MutableStateFlow<List<String>>(emptyList())

    // Announcement simulation list
    private val _announcements = MutableStateFlow(listOf(
        "Welcome to Guard English AI! Designed by Bikash Bindhani for Facility Management & Field Employees. 👍",
        "MAINTENANCE NOTICE: Admin settings, custom model integrations, and referral systems are active.",
        "UPGRADE SPECIAL: Apply code GUARD99 inside registration settings for bonus check-in rewards!"
    ))
    val announcements: StateFlow<List<String>> = _announcements.asStateFlow()

    // ==========================================
    // INITIATIVE CONTROLS
    // ==========================================
    
    init {
        // Show Splash initially, transition to Dashboard or Login
        viewModelScope.launch {
            kotlinx.coroutines.delay(2200)
            val savedEmail = repository.currentUserEmail.value
            if (savedEmail.isNotEmpty()) {
                if (_isOpenedFromAdminLauncher.value) {
                    val usr = db.userDao().getUserByEmail(savedEmail)
                    if (usr != null && (usr.role == "Admin" || usr.role == "Super Admin")) {
                        // Force password entry on Admin app even if already registered/logged in
                        loginEmail.value = savedEmail
                        _isAdminMode.value = true
                        navigateTo(AppScreen.Login)
                    } else {
                        // Non-admin attempting to access the Admin Console
                        repository.logout()
                        _isAdminMode.value = false
                        Toast.makeText(application, "Access Denied: Admin privileges required.", Toast.LENGTH_LONG).show()
                        navigateTo(AppScreen.Login)
                    }
                } else {
                    _isAdminMode.value = false
                    navigateTo(AppScreen.Dashboard)
                }
            } else {
                navigateTo(AppScreen.Login)
            }
        }
    }

    // Navigation Helper
    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (_screenBackStack.isNotEmpty()) {
            val prev = _screenBackStack.removeLast()
            _currentScreen.value = prev
        } else {
            // Default escape
            _currentScreen.value = AppScreen.Dashboard
        }
    }

    fun addLog(msg: String) {
        val currentLogs = adminLogs.value.toMutableList()
        currentLogs.add(0, "[${System.currentTimeMillis()}] $msg")
        adminLogs.value = currentLogs
    }

    // ==========================================
    // REGISTER & LOGIN FUNCTIONS
    // ==========================================
    var loginEmail = MutableStateFlow("")
    val adminPassword = MutableStateFlow("")
    
    var regEmail = MutableStateFlow("")
    var regName = MutableStateFlow("")
    var regRole = MutableStateFlow("User") // User, Premium User, Admin
    var regReferCode = MutableStateFlow("")

    fun executeLogin(context: Context) {
        val email = loginEmail.value.trim()
        if (email.isEmpty()) {
            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            if (_isOpenedFromAdminLauncher.value) {
                val enteredPassword = adminPassword.value.trim()
                if (enteredPassword != "757018") {
                    Toast.makeText(context, "Access Denied: Incorrect Security PIN / Password.", Toast.LENGTH_LONG).show()
                    return@launch
                }
            }

            val success = repository.login(email)
            if (success) {
                addLog("User logged-in: $email")
                Toast.makeText(context, "Welcome back, $email!", Toast.LENGTH_SHORT).show()
                val usr = db.userDao().getUserByEmail(email)
                if (_isOpenedFromAdminLauncher.value) {
                    if (usr != null && (usr.role == "Admin" || usr.role == "Super Admin")) {
                        _isAdminMode.value = true
                        navigateTo(AppScreen.AdminDashboard)
                    } else {
                        repository.logout()
                        _isAdminMode.value = false
                        Toast.makeText(context, "Access Denied: This console is only for Admins.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    _isAdminMode.value = false
                    navigateTo(AppScreen.Dashboard)
                }
            } else {
                Toast.makeText(context, "Login failed. Account may be BANNED or invalid.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun executeRegistration(context: Context) {
        val email = regEmail.value.trim()
        val name = regName.value.trim()
        val role = regRole.value
        val code = regReferCode.value.trim()

        if (email.isEmpty() || name.isEmpty()) {
            Toast.makeText(context, "Please complete all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val result = repository.register(email, name, role, code)
            if (result == "Success") {
                addLog("New account registered: $email ($role)")
                Toast.makeText(context, "Verification Successful! Please log in.", Toast.LENGTH_SHORT).show()
                loginEmail.value = email
                navigateTo(AppScreen.Login)
            } else {
                Toast.makeText(context, result, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun executeLogout(context: Context) {
        repository.logout()
        _isAdminMode.value = false
        addLog("User logged out")
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navigateTo(AppScreen.Login)
    }

    // ==========================================
    // REWARDS CLAIM
    // ==========================================
    fun claimDailyCheckIn(context: Context) {
        viewModelScope.launch {
            val result = repository.claimDailyReward()
            Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
            if (result.first) {
                addLog("Claimed daily check-in reward")
            }
        }
    }

    // ==========================================
    // AI GENERATOR DISPATCHER
    // ==========================================
    
    fun selectGenerator(type: GeneratorType) {
        _selectedGenerator.value = type
        _generatedOutputText.value = ""
        navigateTo(AppScreen.GeneratorForm)
    }

    fun runAIGeneration(context: Context) {
        viewModelScope.launch {
            // Verify limit check and deduct credit
            val decision = repository.verifyAndConsumeLimit()
            if (!decision.first) {
                Toast.makeText(context, decision.second, Toast.LENGTH_LONG).show()
                navigateTo(AppScreen.Premium)
                return@launch
            }

            _isGenerating.value = true
            _generatedOutputText.value = "AI is drafting your professional English report... Please stand by."

            // Construct compile prompt based on input form type
            val prompt = when (_selectedGenerator.value) {
                GeneratorType.Converter -> {
                    "Task: Translate input language into professional workplace English.\n" +
                    "Local Input Language: ${convSrcLang.value}\n" +
                    "Select Tone: ${convTone.value}\n" +
                    "Input Text: ${convInputText.value}"
                }
                GeneratorType.Attendance -> {
                    "Task: Generate a comprehensive, professional English security attendance report.\n" +
                    "Staff Name / Supervisor: ${attStaffName.value}\n" +
                    "Security Logistics Provider: ${attAgency.value}\n" +
                    "On-duty guards total count: ${attTotalStaff.value}\n" +
                    "Absent count: ${attAbsentCount.value}\n" +
                    "Supervisor Observations: ${attRemarks.value}"
                }
                GeneratorType.Incident -> {
                    "Task: Generate an official Incident report.\n" +
                    "Incident Occurrence Category: ${incType.value}\n" +
                    "Happened Date/Time: ${incDateTime.value}\n" +
                    "Incident Spot Location: ${incLocation.value}\n" +
                    "What happened (Raw details): ${incDetails.value}\n" +
                    "Corrective actions taken immediately: ${incAction.value}"
                }
                GeneratorType.Leave -> {
                    "Task: Write a formal corporate Leave application.\n" +
                    "Reason for requesting leave: ${lvReason.value}\n" +
                    "Leave requested from: ${lvFromDate.value} to Date: ${lvToDate.value}\n" +
                    "Designated relieving security guard backup: ${lvReliever.value}"
                }
                GeneratorType.Visitor -> {
                    "Task: Compile a professional visitor entry log report.\n" +
                    "Visitor Name: ${visName.value}\n" +
                    "Visitor Organization/Company: ${visCompany.value}\n" +
                    "Visit Objective/Purpose: ${visReason.value}\n" +
                    "In Check-time: ${visInTime.value} / Exit Out-time: ${visOutTime.value}\n" +
                    "Verify ID document checked: ${if (visCardChecked.value) "YES" else "NO"}"
                }
                GeneratorType.Handover -> {
                    "Task: Generate an official Shift Handover report.\n" +
                    "Relieving Security Guard (Outgoing): ${handOutgoing.value}\n" +
                    "Incoming Security Guard (Relief): ${handIncoming.value}\n" +
                    "Key drawer check status: ${if (handKeys.value) "VERIFIED & PASSED" else "PENDING ACTIONS"}\n" +
                    "Security occurrence register verified checklist: ${if (handLogbook.value) "VERIFIED" else "PENDING REVIEW"}\n" +
                    "Supervisor Handover instructions/Remarks: ${handNotes.value}"
                }
                GeneratorType.DailyLog -> {
                    "Task: Compile daily security log supervisor summary report.\n" +
                    "On-duty Guard Shift: ${dlogShift.value}\n" +
                    "Hourly Gate Patrol Completed Count: ${dlogPatrolCount.value} times\n" +
                    "Vehicles Gate Entries Counted: ${dlogVehiclesIn.value} entries\n" +
                    "General supervisor notes: ${dlogRemarks.value}"
                }
            }

            val systemInstruct = "Output MUST be a ready-to-use, professional formatted report. Add a polite sign-off. Write in highly correct English."

            try {
                val output = repository.generateAIResponse(prompt, systemInstruct)
                _generatedOutputText.value = output
                
                // Deducting credit notification success toast
                if (decision.second.isNotEmpty()) {
                    Toast.makeText(context, "Report compiled! ${decision.second}", Toast.LENGTH_SHORT).show()
                }

                // Automatically save generated reports inside room database
                val repHeading = when (_selectedGenerator.value) {
                    GeneratorType.Converter -> "Converted English Text"
                    GeneratorType.Attendance -> "Attendance: ${attStaffName.value}"
                    GeneratorType.Incident -> "Incident: ${incType.value}"
                    GeneratorType.Leave -> "Leave: ${lvReason.value.take(20)}..."
                    GeneratorType.Visitor -> "Visitor Check-in Log: ${visName.value}"
                    GeneratorType.Handover -> "Handover: ${handIncoming.value} Relief"
                    GeneratorType.DailyLog -> "Daily Security Log Summary"
                }

                val origTextStored = when (_selectedGenerator.value) {
                    GeneratorType.Converter -> convInputText.value
                    GeneratorType.Attendance -> attRemarks.value
                    GeneratorType.Incident -> incDetails.value
                    GeneratorType.Leave -> lvReason.value
                    GeneratorType.Visitor -> visReason.value
                    GeneratorType.Handover -> handNotes.value
                    GeneratorType.DailyLog -> dlogRemarks.value
                }

                repository.saveReport(
                    title = repHeading,
                    type = _selectedGenerator.value.name,
                    originalText = origTextStored.ifEmpty { "Structured mobile form parameters" },
                    translated = output
                )
                addLog("Generated Report: $repHeading")
            } catch (e: Exception) {
                _generatedOutputText.value = "Failed to compile. Error: ${e.message}\nPlease verify your AI Studio API keys or internet connection."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    // ==========================================
    // HISTORY & FAVORITES CRUD
    // ==========================================
    
    fun toggleFavoriteReport(report: Report) {
        viewModelScope.launch {
            repository.setReportFavorite(report.id, !report.isFavorite)
        }
    }

    fun deleteReport(report: Report, context: Context) {
        viewModelScope.launch {
            repository.deleteReport(report.id)
            Toast.makeText(context, "Report deleted from local device cache", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // UTILITIES (COPY, SHARE, WHATSAPP)
    // ==========================================
    
    fun actionCopyText(text: String, context: Context) {
        if (text.isEmpty()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("GuardEnglishDraft", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text copied to clipboard! Ready to paste into WhatsApp.", Toast.LENGTH_SHORT).show()
    }

    fun actionShareText(text: String, context: Context) {
        if (text.isEmpty()) return
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Professional Report via")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun actionOpenUrl(url: String, context: Context) {
        if (url.isEmpty()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open URL: $url", Toast.LENGTH_SHORT).show()
        }
    }

    fun actionSendWhatsApp(text: String, context: Context) {
        if (text.isEmpty()) return
        // First copy text to clipboard as safety backup
        actionCopyText(text, context)

        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                `package` = "com.whatsapp"
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Give advice to launch chooser
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(intent, "WhatsApp not found. Send via:")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        }
    }

    // Export simulated PDF
    fun actionExportPdf(reportTitle: String, text: String, context: Context) {
        if (text.isEmpty()) return
        // In mobile-optimized mode, we copy formatted text and offer file download
        actionCopyText(text, context)
        Toast.makeText(
            context,
            "📁 PDF Simulated Export Compiled! File downloaded: /$reportTitle.pdf copied to Clip.",
            Toast.LENGTH_LONG
        ).show()
        addLog("Simulated PDF compilation downloaded")
    }

    // ==========================================
    // CHAT MANAGEMENT
    // ==========================================
    
    fun createNewChatThread() {
        val uniqueId = UUID.randomUUID().toString()
        val num = chatSessions.value.size + 1
        val title = "Report Incident Discussion #$num"
        
        viewModelScope.launch {
            repository.createChatSession(uniqueId, title)
            _activeSessionId.value = uniqueId
            // Add automatic helpful greeting message from AI
            repository.insertChatMessage(
                ChatMessage(
                    sessionId = uniqueId,
                    role = "assistant",
                    text = "Hello! I am your security supervisor AI writing buddy. How are you doing? Tell me what happened, and I can shape it into a neat incident draft/leave slip.",
                    modelName = appConfig.value.defaultModel
                )
            )
            addLog("Created Chat Thread: $title")
        }
    }

    fun selectChatSession(id: String) {
        _activeSessionId.value = id
        navigateTo(AppScreen.AIChat)
    }

    fun deleteChatSession(id: String, context: Context) {
        viewModelScope.launch {
            repository.deleteChatSession(id)
            if (_activeSessionId.value == id) {
                _activeSessionId.value = ""
            }
            Toast.makeText(context, "Discussion thread deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun pinChatSession(id: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.pinChatSession(id, isPinned)
        }
    }

    fun renameChatSession(id: String, newName: String) {
        if (newName.isEmpty()) return
        viewModelScope.launch {
            repository.renameChatSession(id, newName)
        }
    }

    fun continueResponse() {
         val lastMsg = chatMessages.value.lastOrNull { it.role == "assistant" } ?: return
         chatInputText.value = "Continue explaining: " + lastMsg.text.takeLast(30)
         // Trigger send
         triggerSendAndGenerate()
    }

    fun regenerateResponse() {
        val lastUserMsg = chatMessages.value.lastOrNull { it.role == "user" } ?: return
        chatInputText.value = lastUserMsg.text
        // Trigger send
        triggerSendAndGenerate()
    }

    fun triggerSendAndGenerate() {
        val query = chatInputText.value.trim()
        val sessId = _activeSessionId.value
        if (query.isEmpty() || sessId.isEmpty()) return

        // Clear input field early
        chatInputText.value = ""

        viewModelScope.launch {
            // Write User query message to RoomDB
            repository.insertChatMessage(
                ChatMessage(
                    sessionId = sessId,
                    role = "user",
                    text = query,
                    modelName = ""
                )
            )

            _isGeneratingChat.value = true
            
            // Build conversation payload prompt context
            val messagesList = chatMessages.value.takeLast(6) // Take last 6 messages
            val contextPrompt = StringBuilder()
            contextPrompt.append("You are in a live interactive chat assisting a security guard/supervisor in the field. Guide them.\nConversation History:\n")
            messagesList.forEach { msg ->
                contextPrompt.append("${msg.role.uppercase()}: ${msg.text}\n")
            }
            contextPrompt.append("Generate a helpful, formal, professional security answer.")

            val systemInstruct = "Maintain Guard duty protocols. Help organize attendance, logs, shifts, or translate sentences instantly."

            try {
                val output = repository.generateAIResponse(contextPrompt.toString(), systemInstruct)
                repository.insertChatMessage(
                    ChatMessage(
                        sessionId = sessId,
                        role = "assistant",
                        text = output,
                        modelName = appConfig.value.defaultModel
                    )
                )
                addLog("AI Chat reply generated in thread")
            } catch (e: Exception) {
                repository.insertChatMessage(
                    ChatMessage(
                        sessionId = sessId,
                        role = "assistant",
                        text = "Apologies, I encountered a network connection error: ${e.message}. Please click 'Regenerate' to try again.",
                        modelName = appConfig.value.defaultModel
                    )
                )
            } finally {
                _isGeneratingChat.value = false
            }
        }
    }

    // ==========================================
    // PREMIUM SIMULATION BUY
    // ==========================================
    fun performSimulatedPurchase(plan: String, upiId: String, context: Context) {
        val email = currentEmail.value
        if (email.isEmpty()) {
            Toast.makeText(context, "Log in first", Toast.LENGTH_SHORT).show()
            return
        }

        if (upiId.isEmpty()) {
            Toast.makeText(context, "UPI ID is required", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = when (plan) {
            "1 Month" -> 199
            "3 Month" -> 499
            "6 Month" -> 899
            "12 Month" -> 1499
            else -> 199
        }

        viewModelScope.launch {
            repository.savePaymentTx(
                PaymentTx(
                    email = email,
                    amount = amount,
                    planName = plan,
                    upiId = upiId,
                    txReference = "TXN" + (10000000..99999999).random().toString(),
                    status = "Pending"
                )
            )
            Toast.makeText(context, "UPI Payment Requested! Awaiting approval from the Master Admin Portal.", Toast.LENGTH_LONG).show()
            addLog("UPI premium request logged: Rs $amount ($plan)")
            navigateTo(AppScreen.Profile)
        }
    }

    // ==========================================
    // ADMIN ACTIONS
    // ==========================================
    
    fun adminApprovePayment(txId: Long, approve: Boolean, context: Context) {
        viewModelScope.launch {
            repository.approvePayment(txId, approve)
            val action = if (approve) "Approved" else "Rejected"
            Toast.makeText(context, "UPI transaction payment $action", Toast.LENGTH_SHORT).show()
            addLog("Admin payment action: ID $txId $action")
        }
    }

    fun adminBanUser(email: String, ban: Boolean, context: Context) {
        viewModelScope.launch {
            repository.banUser(email, ban)
            val action = if (ban) "Banned" else "Unbanned"
            Toast.makeText(context, "User account $email $action", Toast.LENGTH_SHORT).show()
            addLog("Admin user action: $email $action")
        }
    }

    fun adminSetPremium(email: String, context: Context) {
        viewModelScope.launch {
            repository.changeUserPlan(email, "Premium User", true, 30)
            Toast.makeText(context, "Elevated profile to Premium!", Toast.LENGTH_SHORT).show()
            addLog("Admin elevated user $email to Premium 30 Days")
        }
    }

    fun adminDeleteUser(email: String, context: Context) {
        viewModelScope.launch {
            repository.deleteUser(email)
            Toast.makeText(context, "User account deleted", Toast.LENGTH_SHORT).show()
            addLog("Admin user account deleted: $email")
        }
    }

    fun adminModifySettings(
        webName: String,
        themeColor: String,
        isMaintenance: Boolean,
        adsEnabled: Boolean,
        freeLimit: Int,
        premiumLimit: Int,
        selectedDefaultModel: String,
        orKey: String,
        instaId: String,
        teleGrp: String,
        context: Context
    ) {
        viewModelScope.launch {
            val updated = AppSystemConfig(
                id = 1,
                websiteName = webName,
                themeColorHex = themeColor,
                isMaintenanceMode = isMaintenance,
                adsEnabled = adsEnabled,
                freeDailyLimit = freeLimit,
                premiumDailyLimit = premiumLimit,
                defaultModel = selectedDefaultModel,
                openRouterApiKey = orKey,
                instagramId = instaId,
                telegramGroup = teleGrp
            )
            repository.updateAppConfig(updated)
            Toast.makeText(context, "Platform parameters saved successfully", Toast.LENGTH_SHORT).show()
            addLog("Admin updated general system parameters")
        }
    }
}
