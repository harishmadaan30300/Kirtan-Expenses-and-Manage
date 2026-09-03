package com.example.ui.util

import android.content.Context
import android.net.Uri
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPayload(
    val version: Int = 1,
    val app: String = "Kirtan Seva",
    val exportedAt: Long,
    val exportedDate: String,
    val kirtans: List<KirtanEntity>,
    val donations: List<DonationEntity>,
    val expenses: List<ExpenseEntity>
)

data class LocalBackupInfo(
    val file: File,
    val fileName: String,
    val timestamp: Long,
    val sizeBytes: Long,
    val kirtanCount: Int,
    val donationCount: Int,
    val expenseCount: Int,
    val formattedDate: String,
    val isAuto: Boolean
)

data class BackupParseResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val payload: BackupPayload? = null
)

object BackupManager {
    private const val PREFS_NAME = "kirtan_preferences"
    private const val KEY_AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    private const val KEY_LAST_AUTO_BACKUP_DATE = "last_auto_backup_date"
    private const val KEY_LAST_AUTO_BACKUP_TIME = "last_auto_backup_time"
    private const val BACKUP_DIR_NAME = "backups"
    private const val MAX_AUTO_BACKUPS_TO_KEEP = 7

    private fun getBackupDir(context: Context): File {
        val dir = File(context.filesDir, BACKUP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun isAutoBackupEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_BACKUP_ENABLED, true)
    }

    fun setAutoBackupEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_BACKUP_ENABLED, enabled).apply()
    }

    fun getLastAutoBackupTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_AUTO_BACKUP_TIME, 0L)
    }

    fun toJsonString(
        kirtans: List<KirtanEntity>,
        donations: List<DonationEntity>,
        expenses: List<ExpenseEntity>,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val root = JSONObject()
        root.put("app", "Kirtan Seva")
        root.put("version", 1)
        root.put("exportedAt", timestamp)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        root.put("exportedDate", dateFormat.format(Date(timestamp)))

        // Kirtans
        val kirtansArray = JSONArray()
        kirtans.forEach { k ->
            val obj = JSONObject().apply {
                put("id", k.id)
                put("name", k.name)
                put("organizer", k.organizer)
                put("location", k.location)
                put("dateMillis", k.dateMillis)
                put("notes", k.notes)
                put("isCompleted", k.isCompleted)
                put("createdAt", k.createdAt)
            }
            kirtansArray.put(obj)
        }
        root.put("kirtans", kirtansArray)

        // Donations
        val donationsArray = JSONArray()
        donations.forEach { d ->
            val obj = JSONObject().apply {
                put("id", d.id)
                put("kirtanId", d.kirtanId)
                put("donorName", d.donorName)
                put("amount", d.amount)
                put("mobileNumber", d.mobileNumber)
                put("paymentMode", d.paymentMode)
                put("referenceId", d.referenceId)
                put("dateMillis", d.dateMillis)
                put("notes", d.notes)
                put("receivedBy", d.receivedBy)
                put("isPaymentReceived", d.isPaymentReceived)
            }
            donationsArray.put(obj)
        }
        root.put("donations", donationsArray)

        // Expenses
        val expensesArray = JSONArray()
        expenses.forEach { e ->
            val obj = JSONObject().apply {
                put("id", e.id)
                put("kirtanId", e.kirtanId)
                put("title", e.title)
                put("category", e.category)
                put("amount", e.amount)
                put("paymentMode", e.paymentMode)
                put("paidTo", e.paidTo)
                put("referenceId", e.referenceId)
                put("dateMillis", e.dateMillis)
                put("notes", e.notes)
            }
            expensesArray.put(obj)
        }
        root.put("expenses", expensesArray)

        return root.toString(2)
    }

    fun parseJsonString(jsonStr: String): BackupParseResult {
        return try {
            val root = JSONObject(jsonStr)
            val version = root.optInt("version", 1)
            val app = root.optString("app", "Kirtan Seva")
            val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())
            val exportedDate = root.optString("exportedDate", "")

            val kirtansList = mutableListOf<KirtanEntity>()
            val kirtansArray = root.optJSONArray("kirtans") ?: JSONArray()
            for (i in 0 until kirtansArray.length()) {
                val obj = kirtansArray.getJSONObject(i)
                kirtansList.add(
                    KirtanEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "कीर्तन"),
                        organizer = obj.optString("organizer", ""),
                        location = obj.optString("location", ""),
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        notes = obj.optString("notes", ""),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val donationsList = mutableListOf<DonationEntity>()
            val donationsArray = root.optJSONArray("donations") ?: JSONArray()
            for (i in 0 until donationsArray.length()) {
                val obj = donationsArray.getJSONObject(i)
                donationsList.add(
                    DonationEntity(
                        id = obj.optLong("id", 0L),
                        kirtanId = obj.optLong("kirtanId", 1L),
                        donorName = obj.optString("donorName", "दानदाता"),
                        amount = obj.optDouble("amount", 0.0),
                        mobileNumber = obj.optString("mobileNumber", ""),
                        paymentMode = obj.optString("paymentMode", "CASH"),
                        referenceId = obj.optString("referenceId", ""),
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        notes = obj.optString("notes", ""),
                        receivedBy = obj.optString("receivedBy", ""),
                        isPaymentReceived = obj.optBoolean("isPaymentReceived", true)
                    )
                )
            }

            val expensesList = mutableListOf<ExpenseEntity>()
            val expensesArray = root.optJSONArray("expenses") ?: JSONArray()
            for (i in 0 until expensesArray.length()) {
                val obj = expensesArray.getJSONObject(i)
                expensesList.add(
                    ExpenseEntity(
                        id = obj.optLong("id", 0L),
                        kirtanId = obj.optLong("kirtanId", 1L),
                        title = obj.optString("title", "खर्च"),
                        category = obj.optString("category", "अन्य"),
                        amount = obj.optDouble("amount", 0.0),
                        paymentMode = obj.optString("paymentMode", "CASH"),
                        paidTo = obj.optString("paidTo", ""),
                        referenceId = obj.optString("referenceId", ""),
                        dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                        notes = obj.optString("notes", "")
                    )
                )
            }

            BackupParseResult(
                isSuccess = true,
                payload = BackupPayload(
                    version = version,
                    app = app,
                    exportedAt = exportedAt,
                    exportedDate = exportedDate,
                    kirtans = kirtansList,
                    donations = donationsList,
                    expenses = expensesList
                )
            )
        } catch (e: Exception) {
            BackupParseResult(
                isSuccess = false,
                errorMessage = e.localizedMessage ?: "अमान्य बैकअप फ़ाइल प्रारूप (Invalid backup format)"
            )
        }
    }

    /**
     * Checks if a daily backup is needed and performs it automatically if enabled.
     */
    fun performDailyAutoBackupIfNeeded(
        context: Context,
        kirtans: List<KirtanEntity>,
        donations: List<DonationEntity>,
        expenses: List<ExpenseEntity>
    ): File? {
        if (!isAutoBackupEnabled(context)) return null
        if (kirtans.isEmpty() && donations.isEmpty() && expenses.isEmpty()) return null

        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDateStr = prefs.getString(KEY_LAST_AUTO_BACKUP_DATE, "")

        // Also check if file exists
        val backupDir = getBackupDir(context)
        val autoBackupFile = File(backupDir, "kirtan_auto_backup_$todayDateStr.json")

        if (todayDateStr == lastDateStr && autoBackupFile.exists()) {
            // Already backed up today
            return autoBackupFile
        }

        return try {
            val json = toJsonString(kirtans, donations, expenses)
            FileOutputStream(autoBackupFile).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
            }
            prefs.edit()
                .putString(KEY_LAST_AUTO_BACKUP_DATE, todayDateStr)
                .putLong(KEY_LAST_AUTO_BACKUP_TIME, System.currentTimeMillis())
                .apply()

            pruneOldAutoBackups(context)
            autoBackupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates an immediate manual backup file in mobile storage.
     */
    fun createManualBackup(
        context: Context,
        kirtans: List<KirtanEntity>,
        donations: List<DonationEntity>,
        expenses: List<ExpenseEntity>,
        prefix: String = "manual"
    ): File? {
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(now))
        val backupDir = getBackupDir(context)
        val file = File(backupDir, "kirtan_${prefix}_backup_$dateStr.json")

        return try {
            val json = toJsonString(kirtans, donations, expenses, now)
            FileOutputStream(file).use { fos ->
                fos.write(json.toByteArray(Charsets.UTF_8))
            }
            // Update last backup time
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_AUTO_BACKUP_TIME, now)
                .apply()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Lists all saved local backup files on the mobile device.
     */
    fun listLocalBackups(context: Context): List<LocalBackupInfo> {
        val dir = getBackupDir(context)
        val files = dir.listFiles { f -> f.extension == "json" } ?: return emptyList()

        val list = mutableListOf<LocalBackupInfo>()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        files.forEach { file ->
            try {
                val content = FileInputStream(file).use { fis ->
                    fis.bufferedReader(Charsets.UTF_8).use { it.readText() }
                }
                val parseResult = parseJsonString(content)
                if (parseResult.isSuccess && parseResult.payload != null) {
                    val p = parseResult.payload
                    val time = if (p.exportedAt > 0) p.exportedAt else file.lastModified()
                    val isAuto = file.name.contains("auto")
                    list.add(
                        LocalBackupInfo(
                            file = file,
                            fileName = file.name,
                            timestamp = time,
                            sizeBytes = file.length(),
                            kirtanCount = p.kirtans.size,
                            donationCount = p.donations.size,
                            expenseCount = p.expenses.size,
                            formattedDate = dateFormat.format(Date(time)),
                            isAuto = isAuto
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore corrupt or unreadable files
            }
        }

        return list.sortedByDescending { it.timestamp }
    }

    /**
     * Reads a backup JSON string from a Uri (from Android File Picker).
     */
    fun readFromUri(context: Context, uri: Uri): BackupParseResult {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: return BackupParseResult(false, "फ़ाइल खोली नहीं जा सकी (Could not open file)")

            parseJsonString(content)
        } catch (e: Exception) {
            BackupParseResult(false, e.localizedMessage ?: "फ़ाइल पढ़ने में त्रुटि (File read error)")
        }
    }

    /**
     * Reads a backup JSON from a local File.
     */
    fun readFromFile(file: File): BackupParseResult {
        return try {
            val content = FileInputStream(file).use { fis ->
                fis.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
            parseJsonString(content)
        } catch (e: Exception) {
            BackupParseResult(false, e.localizedMessage ?: "फ़ाइल पढ़ने में त्रुटि (File read error)")
        }
    }

    fun deleteBackupFile(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }

    private fun pruneOldAutoBackups(context: Context) {
        val dir = getBackupDir(context)
        val autoFiles = dir.listFiles { f -> f.extension == "json" && f.name.contains("auto") } ?: return
        if (autoFiles.size > MAX_AUTO_BACKUPS_TO_KEEP) {
            autoFiles.sortedBy { it.lastModified() }
                .take(autoFiles.size - MAX_AUTO_BACKUPS_TO_KEEP)
                .forEach { it.delete() }
        }
    }
}
