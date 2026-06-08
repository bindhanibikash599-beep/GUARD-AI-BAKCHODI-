package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.api.GeminiNetworkService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AppRepository(private val context: Context, private val database: AppDatabase) {
    private val TAG = "AppRepository"
    private val prefs: SharedPreferences = context.getSharedPreferences("guard_ai_prefs", Context.MODE_PRIVATE)
    
    private val _currentUserEmail = MutableStateFlow<String>(prefs.getString("logged_in_email", "") ?: "")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    // Reactive flow for current logged in user account details
    val currentUserFlow: Flow<UserAccount?> = _currentUserEmail.flatMapLatest { email ->
        if (email.isEmpty()) {
            flowOf<UserAccount?>(null)
        } else {
            // Read from Room reactively
            flow {
                emit(database.userDao().getUserByEmail(email))
            }
        }
    }.flowOn(Dispatchers.IO)

    // System configurations flow
    val configFlow: Flow<AppSystemConfig> = database.configDao().getConfigFlow().map { 
        it ?: AppSystemConfig() 
    }.flowOn(Dispatchers.IO)

    // Chat sessions and reports
    val chatSessionsFlow: Flow<List<ChatSession>> = database.chatDao().getSessionsFlow()
    val reportsFlow: Flow<List<Report>> = database.reportDao().getReportsFlow()
    val favoritesFlow: Flow<List<Report>> = database.reportDao().getFavoritesFlow()
    val paymentsFlow: Flow<List<PaymentTx>> = database.paymentDao().getPaymentsFlow()
    val allUsersFlow: Flow<List<UserAccount>> = database.userDao().getAllUsersFlow()

    init {
        // Pre-populate data in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                prepopulateDatabase()
            } catch (e: Exception) {
                Log.e(TAG, "Error pre-populating database", e)
            }
        }
    }

    private suspend fun prepopulateDatabase() = withContext(Dispatchers.IO) {
        val configDao = database.configDao()
        val userDao = database.userDao()

        // 1. App Config initialization
        if (configDao.getConfig() == null) {
            configDao.insertOrUpdateConfig(
                AppSystemConfig(
                    id = 1,
                    websiteName = "Guard English AI",
                    themeColorHex = "#2196F3", // High-visibility security service blue
                    isMaintenanceMode = false,
                    adsEnabled = true, // Enable ads for free tier demonstration
                    freeDailyLimit = 15,
                    premiumDailyLimit = 9999,
                    defaultModel = "gemini-3.5-flash",
                    openRouterApiKey = ""
                )
            )
        }

        // 2. Prepopulate system accounts if they don't exist
        // Guard (Standard free user)
        if (userDao.getUserByEmail("guard@guardenglish.com") == null) {
            userDao.insertOrUpdateUser(
                UserAccount(
                    email = "guard@guardenglish.com",
                    name = "Bikash Bindhani (Field Guard)",
                    role = "User",
                    isPremium = false,
                    credits = 10,
                    referralCode = "GUARD99",
                    referencedBy = ""
                )
            )
        }

        // Supervisor (Premium user)
        if (userDao.getUserByEmail("supervisor@guardenglish.com") == null) {
            userDao.insertOrUpdateUser(
                UserAccount(
                    email = "supervisor@guardenglish.com",
                    name = "Bikash Supervisor",
                    role = "Premium User",
                    isPremium = true,
                    premiumExpiryDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days
                    credits = 9999,
                    referralCode = "SUPER88",
                    referencedBy = ""
                )
            )
        }

        // System Admin
        if (userDao.getUserByEmail("admin@guardenglish.com") == null) {
            userDao.insertOrUpdateUser(
                UserAccount(
                    email = "admin@guardenglish.com",
                    name = "Bikash Admin",
                    role = "Admin",
                    isPremium = true,
                    premiumExpiryDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
                    credits = 99999,
                    referralCode = "ADMIN77"
                )
            )
        }

        // Super Admin
        if (userDao.getUserByEmail("super@guardenglish.com") == null) {
            userDao.insertOrUpdateUser(
                UserAccount(
                    email = "super@guardenglish.com",
                    name = "Bikash Bindhani (Developer)",
                    role = "Super Admin",
                    isPremium = true,
                    premiumExpiryDate = System.currentTimeMillis() + (3650L * 24 * 60 * 60 * 1000),
                    credits = 999999,
                    referralCode = "DEVELOPER1"
                )
            )
        }
    }

    // ==========================================
    // AUTHENTICATION LOGIC
    // ==========================================

    suspend fun login(email: String, name: String = ""): Boolean = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) return@withContext false

        val userDao = database.userDao()
        var user = userDao.getUserByEmail(trimmedEmail)

        if (user == null) {
            // Auto register a new free user accounts requested on demand
            val displayName = name.ifEmpty { trimmedEmail.substringBefore("@") }
            user = UserAccount(
                email = trimmedEmail,
                name = displayName,
                role = "User",
                isPremium = false,
                credits = 10,
                referralCode = "REF" + (100000..999999).random().toString(),
                referencedBy = ""
            )
            userDao.insertOrUpdateUser(user)
        }

        if (user.isBanned) {
            return@withContext false // Cannot login banned users
        }

        // Save login details and update device counts
        val updatedUser = user.copy(
            lastLoginTime = System.currentTimeMillis(),
            loginDeviceCount = user.loginDeviceCount + 1
        )
        userDao.insertOrUpdateUser(updatedUser)

        prefs.edit().putString("logged_in_email", trimmedEmail).apply()
        _currentUserEmail.value = trimmedEmail
        return@withContext true
    }

    fun logout() {
        prefs.edit().putString("logged_in_email", "").apply()
        _currentUserEmail.value = ""
    }

    suspend fun register(email: String, name: String, role: String, referCodeEntered: String): String = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isEmpty()) return@withContext "Email cannot be empty"

        val userDao = database.userDao()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return@withContext "User already exists with this email"
        }

        var refGiftDetails = ""
        // Check referral
        if (referCodeEntered.isNotEmpty()) {
            val referrers = userDao.getAllUsers().filter { it.referralCode == referCodeEntered }
            if (referrers.isNotEmpty()) {
                val referrer = referrers.first()
                // Give referrer 5 credits as bonus, and refer-ee 5 additional credits
                userDao.insertOrUpdateUser(
                    referrer.copy(credits = referrer.credits + 5)
                )
                refGiftDetails = "(Referral Applied! Referral bonus of +5 credits added.)"
            }
        }

        val newUser = UserAccount(
            email = trimmedEmail,
            name = name.ifEmpty { trimmedEmail.substringBefore("@") },
            role = role, // User, Premium User, Admin, Super Admin
            isPremium = role == "Premium User" || role == "Admin" || role == "Super Admin",
            premiumExpiryDate = if (role != "User") System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) else 0L,
            credits = if (role == "User") 10 else 9999,
            referralCode = "REF" + (100000..999999).random().toString(),
            referencedBy = referCodeEntered
        )

        userDao.insertOrUpdateUser(newUser)
        return@withContext "Success"
    }

    // ==========================================
    // REWARDS SYSTEM
    // ==========================================

    suspend fun claimDailyReward(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val email = _currentUserEmail.value
        if (email.isEmpty()) return@withContext Pair(false, "No active login session")

        val userDao = database.userDao()
        val user = userDao.getUserByEmail(email) ?: return@withContext Pair(false, "User not found")

        val rawNow = System.currentTimeMillis()
        val isSameDay = isToday(user.dailyCheckInTimestamp, rawNow)
        if (isSameDay) {
            return@withContext Pair(false, "Already claimed your check-in rewards for today. Come back tomorrow!")
        }

        // Grant 5 extra rewards credits
        val updatedUser = user.copy(
            dailyCheckInTimestamp = rawNow,
            credits = user.credits + 5
        )
        userDao.insertOrUpdateUser(updatedUser)
        return@withContext Pair(true, "Claimed Daily Login Reward: +5 credits bonus added!")
    }

    private fun isToday(lastTime: Long, now: Long): Boolean {
        if (lastTime == 0L) return false
        val lastDay = lastTime / (1000 * 60 * 60 * 24)
        val currentDay = now / (1000 * 60 * 60 * 24)
        return lastDay == currentDay
    }

    // ==========================================
    // LIMIT CHECK & DECREMENT SYSTEM
    // ==========================================

    suspend fun verifyAndConsumeLimit(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val email = _currentUserEmail.value
        if (email.isEmpty()) return@withContext Pair(false, "Please sign-in to generate reports.")

        val userDao = database.userDao()
        val config = database.configDao().getConfig() ?: AppSystemConfig()
        val user = userDao.getUserByEmail(email) ?: return@withContext Pair(false, "Profile doesn't exist.")

        if (user.isBanned) {
            return@withContext Pair(false, "Your account has been banned. Please contact developer support: Bikash Bindhani.")
        }

        // Unlimited Mode or Premium account
        if (user.isPremium || user.role == "Admin" || user.role == "Super Admin") {
            return@withContext Pair(true, "") // Premium or Admin has unlimited checks
        }

        // Free account limits
        if (user.credits <= 0) {
            return@withContext Pair(false, "Daily credits limit exhausted. Get credits via Referral, Claim Daily Rewards or upgrade to GUARD PREMIUM!")
        }

        // Consume credit
        val newUser = user.copy(credits = user.credits - 1)
        userDao.insertOrUpdateUser(newUser)
        return@withContext Pair(true, "Credits balance: ${newUser.credits} credits left.")
    }

    // ==========================================
    // AI LOGIC INTERMEDIARY
    // ==========================================

    suspend fun generateAIResponse(
        prompt: String,
        systemModePrompt: String = ""
    ): String = withContext(Dispatchers.IO) {
        val config = database.configDao().getConfig() ?: AppSystemConfig()
        
        // Pass openRouter credentials if specified, fallback to Built-in key
        return@withContext GeminiNetworkService.generateContent(
            prompt = prompt,
            customModelName = config.defaultModel,
            openRouterKey = config.openRouterApiKey,
            systemModePrompt = systemModePrompt
        )
    }

    // ==========================================
    // ADMIN FUNCTIONS
    // ==========================================

    suspend fun fetchUsers(): List<UserAccount> = withContext(Dispatchers.IO) {
        database.userDao().getAllUsers()
    }

    suspend fun banUser(email: String, ban: Boolean) = withContext(Dispatchers.IO) {
        database.userDao().setBannedStatus(email, ban)
    }

    suspend fun changeUserPlan(email: String, role: String, isPremium: Boolean, billingDays: Int) = withContext(Dispatchers.IO) {
        val userDao = database.userDao()
        val expiryTime = if (isPremium) System.currentTimeMillis() + (billingDays.toLong() * 24 * 60 * 60 * 1000) else 0L
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            userDao.insertOrUpdateUser(
                user.copy(
                    role = role,
                    isPremium = isPremium,
                    premiumExpiryDate = expiryTime,
                    credits = if (isPremium) 9999 else 10
                )
            )
        }
    }

    suspend fun deleteUser(email: String) = withContext(Dispatchers.IO) {
        val userDao = database.userDao()
        val user = userDao.getUserByEmail(email)
        if (user != null) {
            userDao.deleteUser(user)
        }
    }

    suspend fun updateAppConfig(config: AppSystemConfig) = withContext(Dispatchers.IO) {
        database.configDao().insertOrUpdateConfig(config)
    }

    suspend fun approvePayment(txId: Long, approve: Boolean) = withContext(Dispatchers.IO) {
        val paymentDao = database.paymentDao()
        val status = if (approve) "Approved" else "Rejected"
        paymentDao.updatePaymentStatus(txId, status)

        if (approve) {
            // Grant Premium status to the paying user
            val payments = paymentDao.getPayments()
            val tx = payments.firstOrNull { it.id == txId }
            if (tx != null) {
                val billingDays = when (tx.planName) {
                    "1 Month" -> 30
                    "3 Month" -> 90
                    "6 Month" -> 180
                    "12 Month" -> 365
                    else -> 30
                }
                changeUserPlan(tx.email, "Premium User", true, billingDays)
            }
        }
    }

    suspend fun savePaymentTx(tx: PaymentTx) = withContext(Dispatchers.IO) {
        database.paymentDao().insertPayment(tx)
    }

    // ==========================================
    // REPORT DATABASE WRITING
    // ==========================================

    suspend fun saveReport(
        title: String,
        type: String,
        originalText: String,
        translated: String
    ) = withContext(Dispatchers.IO) {
        database.reportDao().insertReport(
            Report(
                title = title,
                repType = type,
                originalText = originalText,
                translatedText = translated,
                authorEmail = _currentUserEmail.value,
                timestamp = System.currentTimeMillis(),
                isFavorite = false
            )
        )
    }

    suspend fun setReportFavorite(id: Long, fav: Boolean) = withContext(Dispatchers.IO) {
        database.reportDao().setFavorite(id, fav)
    }

    suspend fun deleteReport(id: Long) = withContext(Dispatchers.IO) {
        database.reportDao().deleteReport(id)
    }

    // ==========================================
    // CHAT WRITING LOGIC
    // ==========================================

    suspend fun createChatSession(id: String, title: String): ChatSession = withContext(Dispatchers.IO) {
        val session = ChatSession(id = id, title = title, timestamp = System.currentTimeMillis())
        database.chatDao().insertSession(session)
        return@withContext session
    }

    suspend fun insertChatMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        database.chatDao().insertMessage(message)
    }

    suspend fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return database.chatDao().getMessagesFlow(sessionId)
    }

    suspend fun renameChatSession(id: String, title: String) = withContext(Dispatchers.IO) {
        database.chatDao().renameSession(id, title)
    }

    suspend fun deleteChatSession(id: String) = withContext(Dispatchers.IO) {
        database.chatDao().deleteSession(id)
        database.chatDao().deleteMessagesForSession(id)
    }

    suspend fun pinChatSession(id: String, pin: Boolean) = withContext(Dispatchers.IO) {
        database.chatDao().pinSession(id, pin)
    }
}
