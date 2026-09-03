package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import com.example.data.repository.KirtanRepository
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.DevotionalPalette
import com.example.ui.theme.ThemeMode
import com.example.ui.util.BackupManager
import com.example.ui.util.BackupPayload
import com.example.ui.util.Formatters
import com.example.ui.util.LocalBackupInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class LogType {
    DONATION, EXPENSE
}

data class LogItem(
    val id: Long,
    val type: LogType,
    val title: String,
    val amount: Double,
    val paymentMode: String,
    val dateMillis: Long,
    val subtitle: String,
    val referenceId: String,
    val notes: String,
    val kirtanName: String,
    val rawDonation: DonationEntity? = null,
    val rawExpense: ExpenseEntity? = null
)

data class DashboardSummary(
    val totalDonations: Double = 0.0,
    val cashDonations: Double = 0.0,
    val upiDonations: Double = 0.0,
    val donorCount: Int = 0,
    val totalExpenses: Double = 0.0,
    val cashExpenses: Double = 0.0,
    val upiExpenses: Double = 0.0,
    val expenseCount: Int = 0,
    val netBalance: Double = 0.0,
    val categoryExpenses: Map<String, Double> = emptyMap()
)

class KirtanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KirtanRepository

    val allKirtans: StateFlow<List<KirtanEntity>>
    private val _selectedKirtanId = MutableStateFlow<Long?>(null)
    val selectedKirtanId: StateFlow<Long?> = _selectedKirtanId

    private val allDonationsFlow: StateFlow<List<DonationEntity>>
    private val allExpensesFlow: StateFlow<List<ExpenseEntity>>

    // Current Kirtan filtered donations & expenses
    val currentDonations: StateFlow<List<DonationEntity>>
    val currentExpenses: StateFlow<List<ExpenseEntity>>

    // Dashboard summary
    val summary: StateFlow<DashboardSummary>

    // Combined History Logs
    val historyLogs: StateFlow<List<LogItem>>

    // Search and filter states
    val donationSearchQuery = MutableStateFlow("")
    val donationModeFilter = MutableStateFlow("ALL") // "ALL", "CASH", "UPI"

    val expenseSearchQuery = MutableStateFlow("")
    val expenseCategoryFilter = MutableStateFlow("ALL")

    val logTypeFilter = MutableStateFlow("ALL") // "ALL", "DONATION", "EXPENSE"
    val logSearchQuery = MutableStateFlow("")

    // Preferences & Settings State
    private val prefs = application.getSharedPreferences("kirtan_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name)
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode

    private val _devotionalPalette = MutableStateFlow(
        try {
            DevotionalPalette.valueOf(prefs.getString("devotional_palette", DevotionalPalette.SAFFRON.name) ?: DevotionalPalette.SAFFRON.name)
        } catch (e: Exception) {
            DevotionalPalette.SAFFRON
        }
    )
    val devotionalPalette: StateFlow<DevotionalPalette> = _devotionalPalette

    private val _appLanguage = MutableStateFlow(
        try {
            AppLanguage.valueOf(prefs.getString("app_language", AppLanguage.HINDI.name) ?: AppLanguage.HINDI.name)
        } catch (e: Exception) {
            AppLanguage.HINDI
        }
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setDevotionalPalette(palette: DevotionalPalette) {
        _devotionalPalette.value = palette
        prefs.edit().putString("devotional_palette", palette.name).apply()
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        prefs.edit().putString("app_language", language.name).apply()
    }

    fun toggleTheme() {
        val next = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(next)
    }

    fun toggleLanguage() {
        val next = when (_appLanguage.value) {
            AppLanguage.HINDI -> AppLanguage.ENGLISH
            AppLanguage.ENGLISH -> AppLanguage.HINGLISH
            AppLanguage.HINGLISH -> AppLanguage.HINDI
        }
        setAppLanguage(next)
    }

    // Backup & Restore States
    private val _autoBackupEnabled = MutableStateFlow(BackupManager.isAutoBackupEnabled(application))
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _lastBackupTime = MutableStateFlow(BackupManager.getLastAutoBackupTime(application))
    val lastBackupTime: StateFlow<Long> = _lastBackupTime

    private val _localBackups = MutableStateFlow<List<LocalBackupInfo>>(emptyList())
    val localBackups: StateFlow<List<LocalBackupInfo>> = _localBackups

    fun setAutoBackupEnabled(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        BackupManager.setAutoBackupEnabled(getApplication(), enabled)
        if (enabled) {
            triggerDailyAutoBackup()
        }
    }

    fun refreshLocalBackups() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = BackupManager.listLocalBackups(getApplication())
            _localBackups.value = list
            _lastBackupTime.value = BackupManager.getLastAutoBackupTime(getApplication())
        }
    }

    fun triggerDailyAutoBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            val kirtans = repository.getAllKirtansSync()
            val donations = repository.getAllDonationsSync()
            val expenses = repository.getAllExpensesSync()
            if (kirtans.isNotEmpty() || donations.isNotEmpty() || expenses.isNotEmpty()) {
                val file = BackupManager.performDailyAutoBackupIfNeeded(getApplication(), kirtans, donations, expenses)
                if (file != null) {
                    refreshLocalBackups()
                }
            }
        }
    }

    fun createManualBackup(onResult: (Boolean, String, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val kirtans = repository.getAllKirtansSync()
            val donations = repository.getAllDonationsSync()
            val expenses = repository.getAllExpensesSync()
            val file = BackupManager.createManualBackup(getApplication(), kirtans, donations, expenses)
            if (file != null) {
                val json = try { file.readText() } catch (e: Exception) { "" }
                refreshLocalBackups()
                withContext(Dispatchers.Main) {
                    onResult(true, "सफलतापूर्वक बैकअप फ़ाइल बनाई गई (${file.name})", json)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, "बैकअप बनाने में विफल", null)
                }
            }
        }
    }

    fun restoreFromLocalFile(file: File, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val parseResult = BackupManager.readFromFile(file)
            if (parseResult.isSuccess && parseResult.payload != null) {
                restorePayload(parseResult.payload, onResult)
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, parseResult.errorMessage ?: "अमान्य बैकअप फ़ाइल")
                }
            }
        }
    }

    fun restoreFromUri(uri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val parseResult = BackupManager.readFromUri(getApplication(), uri)
            if (parseResult.isSuccess && parseResult.payload != null) {
                restorePayload(parseResult.payload, onResult)
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, parseResult.errorMessage ?: "फ़ाइल से डेटा नहीं पढ़ा जा सका")
                }
            }
        }
    }

    private suspend fun restorePayload(payload: BackupPayload, onResult: (Boolean, String) -> Unit) {
        try {
            val currentK = repository.getAllKirtansSync()
            val currentD = repository.getAllDonationsSync()
            val currentE = repository.getAllExpensesSync()
            if (currentK.isNotEmpty()) {
                BackupManager.createManualBackup(getApplication(), currentK, currentD, currentE, "pre_restore")
            }

            repository.restoreAllData(payload.kirtans, payload.donations, payload.expenses)
            _selectedKirtanId.value = payload.kirtans.firstOrNull()?.id
            refreshLocalBackups()
            withContext(Dispatchers.Main) {
                onResult(true, "डेटा सफलतापूर्वक रीस्टोर किया गया (${payload.kirtans.size} कीर्तन, ${payload.donations.size} दान, ${payload.expenses.size} खर्च)")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult(false, "रीस्टोर विफल: ${e.localizedMessage}")
            }
        }
    }

    fun deleteLocalBackup(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            BackupManager.deleteBackupFile(file)
            refreshLocalBackups()
        }
    }

    fun defaultReset(keepSampleData: Boolean, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentK = repository.getAllKirtansSync()
                val currentD = repository.getAllDonationsSync()
                val currentE = repository.getAllExpensesSync()
                if (currentK.isNotEmpty()) {
                    BackupManager.createManualBackup(getApplication(), currentK, currentD, currentE, "pre_reset")
                }

                repository.clearAllData()
                if (keepSampleData) {
                    seedDevotionalData()
                } else {
                    _selectedKirtanId.value = null
                }
                refreshLocalBackups()
                withContext(Dispatchers.Main) {
                    onResult(true, if (keepSampleData) "डिफ़ॉल्ट स्थिति में रीसेट सफल (नमूना डेटा लोड किया गया)" else "डेटाबेस पूर्णतः रिक्त और रीसेट कर दिया गया")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "रीसेट में त्रुटि: ${e.localizedMessage}")
                }
            }
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = KirtanRepository(
            database.kirtanDao(),
            database.donationDao(),
            database.expenseDao()
        )

        allKirtans = repository.allKirtans.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        allDonationsFlow = repository.allDonations.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        allExpensesFlow = repository.allExpenses.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        currentDonations = combine(allDonationsFlow, _selectedKirtanId) { donations, kirtanId ->
            if (kirtanId == null) donations else donations.filter { it.kirtanId == kirtanId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        currentExpenses = combine(allExpensesFlow, _selectedKirtanId) { expenses, kirtanId ->
            if (kirtanId == null) expenses else expenses.filter { it.kirtanId == kirtanId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        summary = combine(currentDonations, currentExpenses) { donations, expenses ->
            var totalD = 0.0
            var cashD = 0.0
            var upiD = 0.0
            donations.forEach {
                totalD += it.amount
                if (it.paymentMode.equals("UPI", ignoreCase = true)) {
                    upiD += it.amount
                } else {
                    cashD += it.amount
                }
            }

            var totalE = 0.0
            var cashE = 0.0
            var upiE = 0.0
            val catMap = mutableMapOf<String, Double>()
            expenses.forEach {
                totalE += it.amount
                if (it.paymentMode.equals("UPI", ignoreCase = true)) {
                    upiE += it.amount
                } else {
                    cashE += it.amount
                }
                val cat = it.category.ifBlank { "Other" }
                catMap[cat] = (catMap[cat] ?: 0.0) + it.amount
            }

            DashboardSummary(
                totalDonations = totalD,
                cashDonations = cashD,
                upiDonations = upiD,
                donorCount = donations.size,
                totalExpenses = totalE,
                cashExpenses = cashE,
                upiExpenses = upiE,
                expenseCount = expenses.size,
                netBalance = totalD - totalE,
                categoryExpenses = catMap
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

        historyLogs = combine(
            currentDonations,
            currentExpenses,
            allKirtans
        ) { donations, expenses, kirtans ->
            val kirtanMap = kirtans.associate { it.id to it.name }
            val list = mutableListOf<LogItem>()

            donations.forEach { d ->
                list.add(
                    LogItem(
                        id = d.id,
                        type = LogType.DONATION,
                        title = d.donorName,
                        amount = d.amount,
                        paymentMode = d.paymentMode,
                        dateMillis = d.dateMillis,
                        subtitle = if (d.mobileNumber.isNotBlank()) "Mob: ${d.mobileNumber}" else "Received by: ${d.receivedBy}",
                        referenceId = d.referenceId,
                        notes = d.notes,
                        kirtanName = kirtanMap[d.kirtanId] ?: "Kirtan",
                        rawDonation = d
                    )
                )
            }

            expenses.forEach { e ->
                list.add(
                    LogItem(
                        id = e.id,
                        type = LogType.EXPENSE,
                        title = e.title,
                        amount = e.amount,
                        paymentMode = e.paymentMode,
                        dateMillis = e.dateMillis,
                        subtitle = "${e.category}${if (e.paidTo.isNotBlank()) " • Paid to: ${e.paidTo}" else ""}",
                        referenceId = e.referenceId,
                        notes = e.notes,
                        kirtanName = kirtanMap[e.kirtanId] ?: "Kirtan",
                        rawExpense = e
                    )
                )
            }

            list.sortedByDescending { it.dateMillis }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Seed initial sample kirtan if none exist on very first install
        val hasInitialized = prefs.getBoolean("has_initialized_db", false)
        viewModelScope.launch {
            allKirtans.collect { list ->
                if (list.isEmpty() && !hasInitialized) {
                    prefs.edit().putBoolean("has_initialized_db", true).apply()
                    seedDevotionalData()
                } else if (_selectedKirtanId.value == null && list.isNotEmpty()) {
                    // Default to the first (latest) kirtan
                    _selectedKirtanId.value = list.first().id
                }
            }
        }

        // Initialize local backups and run daily auto backup check
        viewModelScope.launch {
            refreshLocalBackups()
            triggerDailyAutoBackup()
        }
    }

    private suspend fun seedDevotionalData() {
        val now = System.currentTimeMillis()
        val kirtan1Id = repository.insertKirtan(
            KirtanEntity(
                name = "श्री संकीर्तन महोत्सव",
                organizer = "श्री संकीर्तन सेवा मंडल",
                location = "रामलीला ग्राउंड, दिल्ली",
                dateMillis = now - 86400000L * 2,
                notes = "वार्षिक भव्य संकीर्तन व छप्पन भोग उत्सव",
                isCompleted = false
            )
        )

        val kirtan2Id = repository.insertKirtan(
            KirtanEntity(
                name = "माता की पावन चौकी",
                organizer = "भक्त मंडल",
                location = "सामुदायिक भवन, मॉडल टाउन",
                dateMillis = now - 86400000L * 10,
                notes = "शुभ नवरात्र चौकी व भंडारा",
                isCompleted = true
            )
        )

        // Donations for Kirtan 1
        repository.insertDonation(
            DonationEntity(
                kirtanId = kirtan1Id,
                donorName = "श्री रमेश शर्मा जी",
                amount = 5100.0,
                mobileNumber = "9876543210",
                paymentMode = "CASH",
                referenceId = "REC-101",
                dateMillis = now - 86400000L * 2 + 3600000L,
                notes = "परिवार कल्याण हेतु विशेष सहयोग",
                receivedBy = "गोपाल सेठी"
            )
        )
        repository.insertDonation(
            DonationEntity(
                kirtanId = kirtan1Id,
                donorName = "राजेश गुप्ता",
                amount = 2100.0,
                mobileNumber = "9811223344",
                paymentMode = "UPI",
                referenceId = "UPI/9811/498213",
                dateMillis = now - 86400000L * 2 + 7200000L,
                notes = "भोग सेवा",
                receivedBy = "अमित जी"
            )
        )
        repository.insertDonation(
            DonationEntity(
                kirtanId = kirtan1Id,
                donorName = "श्रीमती सुनीता वर्मा",
                amount = 3100.0,
                mobileNumber = "9988776655",
                paymentMode = "CASH",
                referenceId = "REC-102",
                dateMillis = now - 86400000L * 1 + 5000000L,
                notes = "फूल बंगला सेवा",
                receivedBy = "गोपाल सेठी"
            )
        )
        repository.insertDonation(
            DonationEntity(
                kirtanId = kirtan1Id,
                donorName = "विकास मित्तल",
                amount = 1100.0,
                mobileNumber = "9871100223",
                paymentMode = "UPI",
                referenceId = "UPI/7721/889211",
                dateMillis = now - 86400000L * 1 + 9000000L,
                notes = "सामान्य चंदा (सहयोग संकल्प)",
                receivedBy = "अमित जी",
                isPaymentReceived = false
            )
        )

        // Expenses for Kirtan 1
        repository.insertExpense(
            ExpenseEntity(
                kirtanId = kirtan1Id,
                title = "फूल बंगला एवं भव्य शृंगार",
                category = "Flowers & Decoration",
                amount = 3500.0,
                paymentMode = "CASH",
                paidTo = "माली राम एंड संस",
                referenceId = "BILL-84",
                dateMillis = now - 86400000L * 2 + 10000000L,
                notes = "ताजे गुलाब व गेंदा माला शृंगार"
            )
        )
        repository.insertExpense(
            ExpenseEntity(
                kirtanId = kirtan1Id,
                title = "साउंड एवं माइक व्यवस्था",
                category = "Sound & Audio",
                amount = 2500.0,
                paymentMode = "UPI",
                paidTo = "स्टार साउंड सर्विस",
                referenceId = "TXN48921",
                dateMillis = now - 86400000L * 2 + 12000000L,
                notes = "4 माइक, 2 बेस, 1 मॉनिटर"
            )
        )
        repository.insertExpense(
            ExpenseEntity(
                kirtanId = kirtan1Id,
                title = "प्रसाद एवं पेड़ा वितरण",
                category = "Prasad & Bhog",
                amount = 1800.0,
                paymentMode = "CASH",
                paidTo = "बीकानेर स्वीट्स",
                referenceId = "REC-541",
                dateMillis = now - 86400000L * 2 + 14000000L,
                notes = "10 किलो खोया पेड़ा भोग हेतु"
            )
        )

        // Data for Kirtan 2 (Mata Ki Chowki)
        repository.insertDonation(
            DonationEntity(
                kirtanId = kirtan2Id,
                donorName = "आनंद कुमार",
                amount = 2500.0,
                mobileNumber = "9955112233",
                paymentMode = "UPI",
                referenceId = "UPI/9955/223311",
                dateMillis = now - 86400000L * 10,
                notes = "माता की चुनरी सेवा",
                receivedBy = "सुभाष जी"
            )
        )
        repository.insertExpense(
            ExpenseEntity(
                kirtanId = kirtan2Id,
                title = "हलवा-चना प्रसाद",
                category = "Prasad & Bhog",
                amount = 1400.0,
                paymentMode = "CASH",
                paidTo = "हलवाई कालू राम",
                referenceId = "CASH-PAID",
                dateMillis = now - 86400000L * 10,
                notes = "कंजक पूजन प्रसाद"
            )
        )

        _selectedKirtanId.value = kirtan1Id
    }

    fun selectKirtan(id: Long?) {
        _selectedKirtanId.value = id
    }

    fun getSelectedKirtan(): KirtanEntity? {
        val currentId = _selectedKirtanId.value ?: return null
        return allKirtans.value.find { it.id == currentId }
    }

    // Kirtan CRUD
    fun addKirtan(
        name: String,
        organizer: String,
        location: String,
        dateMillis: Long,
        notes: String,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val newId = repository.insertKirtan(
                KirtanEntity(
                    name = name.trim(),
                    organizer = organizer.trim(),
                    location = location.trim(),
                    dateMillis = dateMillis,
                    notes = notes.trim(),
                    isCompleted = false
                )
            )
            _selectedKirtanId.value = newId
            onSuccess(newId)
        }
    }

    fun updateKirtan(kirtan: KirtanEntity) {
        viewModelScope.launch {
            repository.updateKirtan(kirtan)
        }
    }

    fun deleteKirtan(kirtan: KirtanEntity) {
        viewModelScope.launch {
            repository.deleteKirtan(kirtan)
            if (_selectedKirtanId.value == kirtan.id) {
                val remaining = allKirtans.value.filter { it.id != kirtan.id }
                _selectedKirtanId.value = remaining.firstOrNull()?.id
            }
        }
    }

    // Donation CRUD
    fun addDonation(
        donorName: String,
        amount: Double,
        mobileNumber: String,
        paymentMode: String,
        referenceId: String,
        dateMillis: Long,
        notes: String,
        receivedBy: String,
        kirtanId: Long? = null,
        isPaymentReceived: Boolean = true
    ) {
        val targetKirtanId = kirtanId ?: _selectedKirtanId.value ?: allKirtans.value.firstOrNull()?.id ?: return
        viewModelScope.launch {
            repository.insertDonation(
                DonationEntity(
                    kirtanId = targetKirtanId,
                    donorName = donorName.trim(),
                    amount = amount,
                    mobileNumber = mobileNumber.trim(),
                    paymentMode = paymentMode.uppercase().trim(),
                    referenceId = referenceId.trim(),
                    dateMillis = dateMillis,
                    notes = notes.trim(),
                    receivedBy = receivedBy.trim(),
                    isPaymentReceived = isPaymentReceived
                )
            )
        }
    }

    fun updateDonation(donation: DonationEntity) {
        viewModelScope.launch {
            repository.updateDonation(donation)
        }
    }

    fun deleteDonation(donation: DonationEntity) {
        viewModelScope.launch {
            repository.deleteDonation(donation)
        }
    }

    // Expense CRUD
    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        paymentMode: String,
        paidTo: String,
        referenceId: String,
        dateMillis: Long,
        notes: String,
        kirtanId: Long? = null
    ) {
        val targetKirtanId = kirtanId ?: _selectedKirtanId.value ?: allKirtans.value.firstOrNull()?.id ?: return
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    kirtanId = targetKirtanId,
                    title = title.trim(),
                    category = category.trim(),
                    amount = amount,
                    paymentMode = paymentMode.uppercase().trim(),
                    paidTo = paidTo.trim(),
                    referenceId = referenceId.trim(),
                    dateMillis = dateMillis,
                    notes = notes.trim()
                )
            )
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Generate Shareable Transparency Report
    fun generateTransparencyReport(): String {
        val kirtan = getSelectedKirtan()
        val eventTitle = kirtan?.name ?: "समस्त कीर्तन सेवा (All Kirtans)"
        val eventDate = kirtan?.let { Formatters.formatDateOnly(it.dateMillis) } ?: Formatters.formatDateOnly(System.currentTimeMillis())
        val s = summary.value

        val categoryText = if (s.categoryExpenses.isNotEmpty()) {
            val sb = StringBuilder("\n📋 *खर्च श्रेणी विवरण (Expense Breakdown):*\n")
            s.categoryExpenses.forEach { (cat, amt) ->
                sb.append(" • $cat: ${Formatters.formatCurrency(amt)}\n")
            }
            sb.toString()
        } else ""

        return """
🙏 *कीर्तन सेवा हिसाब-किताब* 🙏
━━━━━━━━━━━━━━━━━━━━
📍 *कार्यक्रम / कीर्तन:* $eventTitle
📅 *दिनांक:* $eventDate
${if (!kirtan?.location.isNullOrBlank()) "🏛️ *स्थान:* ${kirtan?.location}\n" else ""}${if (!kirtan?.organizer.isNullOrBlank()) "👥 *आयोजक:* ${kirtan?.organizer}\n" else ""}━━━━━━━━━━━━━━━━━━━━

💰 *कुल प्राप्त सहयोग (Total Donations):* ${Formatters.formatCurrency(s.totalDonations)}
   • 💵 नकद (Cash): ${Formatters.formatCurrency(s.cashDonations)}
   • 📱 ऑनलाइन (UPI): ${Formatters.formatCurrency(s.upiDonations)}
   • 👥 कुल दानदाता (Donors): ${s.donorCount}

💸 *कुल खर्च (Total Expenses):* ${Formatters.formatCurrency(s.totalExpenses)}
   • 💵 नकद भुगतान (Cash): ${Formatters.formatCurrency(s.cashExpenses)}
   • 📱 ऑनलाइन भुगतान (UPI): ${Formatters.formatCurrency(s.upiExpenses)}
   • 📝 कुल मद (Entries): ${s.expenseCount}
$categoryText
━━━━━━━━━━━━━━━━━━━━
⚖️ *शेष शुद्ध बचत / राशि (Net Balance):* ${Formatters.formatCurrency(s.netBalance)}
━━━━━━━━━━━━━━━━━━━━
✨ पूर्ण पारदर्शिता हेतु यह रिपोर्ट प्रस्तुत की गई है। सभी दानदाताओं एवं सेवादारों का हृदय से धन्यवाद!
🌺 *सादर धन्यवाद!* 🌺
        """.trimIndent()
    }

    // Generate Single Donor Receipt
    fun generateDonorReceipt(donation: DonationEntity): String {
        val kirtan = allKirtans.value.find { it.id == donation.kirtanId }
        val kirtanName = kirtan?.name ?: "कीर्तन सेवा"
        val dateStr = Formatters.formatDate(donation.dateMillis)
        val recNo = if (donation.referenceId.isNotBlank()) donation.referenceId else "REC-${donation.id}"
        val statusText = if (donation.isPaymentReceived) "✅ भुगतान प्राप्त (Payment Received)" else "⏳ भुगतान अप्राप्त (Payment Not Received)"

        return """
🙏 *कीर्तन सेवा - दान रसीद (Donation Receipt)* 🙏
━━━━━━━━━━━━━━━━━━━━
🧾 *रसीद संख्या (Receipt No):* #$recNo
📅 *दिनांक:* $dateStr
📌 *भुगतान स्थिति:* $statusText
━━━━━━━━━━━━━━━━━━━━
👤 *दानदाता:* ${donation.donorName}
${if (donation.mobileNumber.isNotBlank()) "📞 *मोबाईल:* ${donation.mobileNumber}\n" else ""}💰 *सहयोग राशि:* ${Formatters.formatCurrency(donation.amount)}
💳 *माध्यम:* ${donation.paymentMode} ${if (donation.referenceId.isNotBlank()) "(${donation.referenceId})" else ""}
📍 *कीर्तन:* $kirtanName
${if (donation.notes.isNotBlank()) "📝 *सेवा संकल्प:* ${donation.notes}\n" else ""}${if (donation.receivedBy.isNotBlank()) "✍️ *प्राप्तकर्ता:* ${donation.receivedBy}\n" else ""}━━━━━━━━━━━━━━━━━━━━
कीर्तन सेवा में आपके पावन सहयोग के लिए हार्दिक धन्यवाद! प्रभु की कृपा आप और आपके परिवार पर सदा बनी रहे।

🌺 *सादर धन्यवाद!* 🌺
        """.trimIndent()
    }
}
