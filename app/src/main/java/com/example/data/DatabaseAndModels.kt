package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================================
// 1. ENTITIES
// ==========================================================

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val email: String,
    val name: String,
    val role: String, // "User", "Premium User", "Admin", "Super Admin"
    val isPremium: Boolean = false,
    val premiumExpiryDate: Long = 0,
    val credits: Int = 10,
    val referralCode: String = "",
    val referencedBy: String = "",
    val dailyCheckInTimestamp: Long = 0,
    val loginDeviceCount: Int = 1,
    val lastLoginTime: Long = System.currentTimeMillis(),
    val isBanned: Boolean = false
)

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val role: String, // "user", "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String = ""
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val repType: String, // "Attendance", "Incident", "Leave", "Visitor", "Handover", "DailyLog", "Converter"
    val originalText: String,
    val translatedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val authorEmail: String = ""
)

@Entity(tableName = "payments")
data class PaymentTx(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val amount: Int,
    val planName: String, // "1 Month", "3 Month", "6 Month", "12 Month"
    val upiId: String,
    val txReference: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_config")
data class AppSystemConfig(
    @PrimaryKey val id: Int = 1,
    val websiteName: String = "Guard English AI",
    val themeColorHex: String = "#FF9800", // Default: Vibrant Security Safety Orange
    val isMaintenanceMode: Boolean = false,
    val adsEnabled: Boolean = false,
    val freeDailyLimit: Int = 15,
    val premiumDailyLimit: Int = 9999,
    val defaultModel: String = "gemini-3.5-flash",
    val openRouterApiKey: String = ""
)

// ==========================================================
// 2. DAOS
// ==========================================================

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserAccount)

    @Delete
    suspend fun deleteUser(user: UserAccount)

    @Query("UPDATE users SET isBanned = :banned WHERE email = :email")
    suspend fun setBannedStatus(email: String, banned: Boolean)

    @Query("UPDATE users SET role = :newRole, isPremium = :isPremium WHERE email = :email")
    suspend fun updateUserRoleAndPremium(email: String, newRole: String, isPremium: Boolean)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, timestamp DESC")
    fun getSessionsFlow(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions")
    suspend fun getSessions(): List<ChatSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Query("UPDATE chat_sessions SET title = :newTitle WHERE id = :id")
    suspend fun renameSession(id: String, newTitle: String)

    @Query("UPDATE chat_sessions SET isPinned = :pinned WHERE id = :id")
    suspend fun pinSession(id: String, pinned: Boolean)

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesFlow(sessionId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
    
    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getReportsFlow(): Flow<List<Report>>

    @Query("SELECT * FROM reports")
    suspend fun getReports(): List<Report>

    @Query("SELECT * FROM reports WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritesFlow(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("UPDATE reports SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: Long)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getPaymentsFlow(): Flow<List<PaymentTx>>

    @Query("SELECT * FROM payments")
    suspend fun getPayments(): List<PaymentTx>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(tx: PaymentTx)

    @Query("UPDATE payments SET status = :newStatus WHERE id = :id")
    suspend fun updatePaymentStatus(id: Long, newStatus: String)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppSystemConfig?

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppSystemConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AppSystemConfig)
}

// ==========================================================
// 3. DATABASE HOLDER
// ==========================================================

@Database(
    entities = [
        UserAccount::class,
        ChatSession::class,
        ChatMessage::class,
        Report::class,
        PaymentTx::class,
        AppSystemConfig::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun reportDao(): ReportDao
    abstract fun paymentDao(): PaymentDao
    abstract fun configDao(): AppConfigDao
}
