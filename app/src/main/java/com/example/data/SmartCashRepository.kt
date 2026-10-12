package com.example.data

import android.content.Context
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SmartCashRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private val dataFile = File(context.filesDir, "smartcash_data_v1.json")
    private val _dataState = MutableStateFlow(defaultState())
    val dataState: StateFlow<AppDataState> = _dataState.asStateFlow()

    private var lastDeletedTransaction: Transaction? = null

    init {
        loadData()
        processRecurringRules()
    }

    fun getTodayISO(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun defaultCategories(): List<Category> {
        return listOf(
            Category(id = "cat_1", nameKey = "cat_salary", icon = "💼", color = "#3F7D5C", type = "income"),
            Category(id = "cat_2", nameKey = "cat_gift", icon = "🎁", color = "#C08A2E", type = "income"),
            Category(id = "cat_3", nameKey = "cat_other_income", icon = "➕", color = "#3F7D5C", type = "income"),
            Category(id = "cat_4", nameKey = "cat_food", icon = "🍽️", color = "#B33F3F", type = "expense"),
            Category(id = "cat_5", nameKey = "cat_transport", icon = "🚗", color = "#6B7280", type = "expense"),
            Category(id = "cat_6", nameKey = "cat_bills", icon = "🧾", color = "#14213D", type = "expense"),
            Category(id = "cat_7", nameKey = "cat_shopping", icon = "🛍️", color = "#C08A2E", type = "expense"),
            Category(id = "cat_8", nameKey = "cat_health", icon = "💊", color = "#B33F3F", type = "expense"),
            Category(id = "cat_9", nameKey = "cat_home", icon = "🏠", color = "#6B7280", type = "expense"),
            Category(id = "cat_10", nameKey = "cat_leisure", icon = "🎬", color = "#3F7D5C", type = "expense"),
            Category(id = "cat_11", nameKey = "cat_other_expense", icon = "➖", color = "#6B7280", type = "expense")
        )
    }

    private fun defaultState(): AppDataState {
        val defaultLang = when (Locale.getDefault().language) {
            "ar" -> "ar"
            "it" -> "it"
            else -> "en"
        }
        return AppDataState(
            version = 1,
            cashboxes = emptyList(),
            transactions = emptyList(),
            categories = defaultCategories(),
            recurring = emptyList(),
            budgets = emptyList(),
            settings = AppSettings(
                language = defaultLang,
                theme = "auto",
                dashboardCurrency = "EUR"
            )
        )
    }

    private fun loadData() {
        if (dataFile.exists()) {
            try {
                val text = dataFile.readText()
                val parsed = json.decodeFromString<AppDataState>(text)
                val categories = if (parsed.categories.isEmpty()) defaultCategories() else parsed.categories
                _dataState.value = parsed.copy(categories = categories)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _dataState.value = defaultState()
    }

    private fun persist(state: AppDataState) {
        _dataState.value = state
        try {
            val text = json.encodeToString(state)
            dataFile.writeText(text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateState(transform: (AppDataState) -> AppDataState) {
        val current = _dataState.value
        val updated = transform(current)
        persist(updated)
    }

    // --- Cashbox Operations ---
    fun addCashbox(name: String, currency: String, icon: String, color: String, initialBalance: Double) {
        val id = UUID.randomUUID().toString()
        val newCashbox = Cashbox(
            id = id,
            name = name,
            currency = currency,
            icon = icon,
            color = color,
            initialBalance = initialBalance
        )
        updateState { current ->
            current.copy(
                cashboxes = current.cashboxes + newCashbox,
                settings = current.settings.copy(
                    onboarded = true,
                    dashboardCurrency = if (current.cashboxes.isEmpty()) currency else current.settings.dashboardCurrency
                )
            )
        }
    }

    fun updateCashbox(id: String, name: String, currency: String, icon: String, color: String) {
        updateState { current ->
            current.copy(
                cashboxes = current.cashboxes.map { cb ->
                    if (cb.id == id) cb.copy(name = name, currency = currency, icon = icon, color = color) else cb
                }
            )
        }
    }

    fun deleteCashbox(id: String) {
        updateState { current ->
            current.copy(
                cashboxes = current.cashboxes.filter { it.id != id },
                transactions = current.transactions.map { tx ->
                    if (tx.cashboxId == id || tx.toCashboxId == id) tx.copy(deleted = true) else tx
                },
                recurring = current.recurring.filter { it.cashboxId != id }
            )
        }
    }

    // --- Transaction Operations ---
    fun addTransaction(
        type: String,
        amount: Double,
        cashboxId: String,
        toCashboxId: String? = null,
        toAmount: Double? = null,
        categoryId: String? = null,
        description: String = "",
        date: String = getTodayISO(),
        recurringFrequency: String? = null
    ) {
        val newTx = Transaction(
            id = UUID.randomUUID().toString(),
            type = type,
            amount = amount,
            cashboxId = cashboxId,
            toCashboxId = toCashboxId,
            toAmount = toAmount,
            categoryId = categoryId,
            description = description,
            date = date,
            createdAt = System.currentTimeMillis()
        )

        var newRec: RecurringRule? = null
        if (recurringFrequency != null && type != "transfer") {
            newRec = RecurringRule(
                id = UUID.randomUUID().toString(),
                type = type,
                amount = amount,
                cashboxId = cashboxId,
                categoryId = categoryId ?: "",
                description = description,
                frequency = recurringFrequency,
                startDate = date,
                nextDate = nextDateFrom(date, recurringFrequency),
                active = true
            )
        }

        updateState { current ->
            val updatedTxns = current.transactions + newTx
            val updatedRecurring = if (newRec != null) current.recurring + newRec else current.recurring
            current.copy(
                transactions = updatedTxns,
                recurring = updatedRecurring,
                settings = current.settings.copy(lastCashboxId = cashboxId)
            )
        }
    }

    fun updateTransaction(
        id: String,
        type: String,
        amount: Double,
        cashboxId: String,
        categoryId: String?,
        description: String,
        date: String
    ) {
        updateState { current ->
            current.copy(
                transactions = current.transactions.map { tx ->
                    if (tx.id == id) {
                        tx.copy(
                            type = type,
                            amount = amount,
                            cashboxId = cashboxId,
                            categoryId = categoryId,
                            description = description,
                            date = date
                        )
                    } else tx
                }
            )
        }
    }

    fun softDeleteTransaction(id: String) {
        val current = _dataState.value
        val tx = current.transactions.find { it.id == id } ?: return
        lastDeletedTransaction = tx
        updateState { state ->
            state.copy(
                transactions = state.transactions.map {
                    if (it.id == id) it.copy(deleted = true) else it
                }
            )
        }
    }

    fun undoDeleteTransaction() {
        val tx = lastDeletedTransaction ?: return
        lastDeletedTransaction = null
        updateState { state ->
            state.copy(
                transactions = state.transactions.map {
                    if (it.id == tx.id) it.copy(deleted = false) else it
                }
            )
        }
    }

    // --- Category Operations ---
    fun addCategory(name: String, icon: String, color: String, type: String) {
        val newCat = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            icon = icon,
            color = color,
            type = type
        )
        updateState { it.copy(categories = it.categories + newCat) }
    }

    fun updateCategory(id: String, name: String, icon: String, color: String, type: String) {
        updateState { current ->
            current.copy(
                categories = current.categories.map {
                    if (it.id == id) it.copy(name = name, nameKey = null, icon = icon, color = color, type = type) else it
                }
            )
        }
    }

    fun deleteCategory(id: String) {
        updateState { current ->
            current.copy(
                categories = current.categories.filter { it.id != id },
                budgets = current.budgets.filter { it.categoryId != id }
            )
        }
    }

    // --- Budget Operations ---
    fun setBudget(categoryId: String, limit: Double, currency: String) {
        updateState { current ->
            val existing = current.budgets.find { it.categoryId == categoryId }
            val updated = if (existing != null) {
                current.budgets.map {
                    if (it.categoryId == categoryId) it.copy(limit = limit, currency = currency) else it
                }
            } else {
                current.budgets + Budget(
                    id = UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    limit = limit,
                    currency = currency
                )
            }
            current.copy(budgets = updated)
        }
    }

    fun removeBudget(id: String) {
        updateState { it.copy(budgets = it.budgets.filter { b -> b.id != id }) }
    }

    // --- Recurring Rule Operations ---
    fun addRecurringRule(
        type: String,
        amount: Double,
        cashboxId: String,
        categoryId: String,
        description: String,
        frequency: String,
        startDate: String,
        endDate: String?
    ) {
        val rule = RecurringRule(
            id = UUID.randomUUID().toString(),
            type = type,
            amount = amount,
            cashboxId = cashboxId,
            categoryId = categoryId,
            description = description,
            frequency = frequency,
            startDate = startDate,
            nextDate = startDate,
            endDate = endDate,
            active = true
        )
        updateState { it.copy(recurring = it.recurring + rule) }
        processRecurringRules()
    }

    fun updateRecurringRule(
        id: String,
        type: String,
        amount: Double,
        cashboxId: String,
        categoryId: String,
        description: String,
        frequency: String,
        startDate: String,
        endDate: String?
    ) {
        updateState { current ->
            current.copy(
                recurring = current.recurring.map { r ->
                    if (r.id == id) {
                        val next = if (r.nextDate < startDate) startDate else r.nextDate
                        r.copy(
                            type = type,
                            amount = amount,
                            cashboxId = cashboxId,
                            categoryId = categoryId,
                            description = description,
                            frequency = frequency,
                            startDate = startDate,
                            nextDate = next,
                            endDate = endDate
                        )
                    } else r
                }
            )
        }
        processRecurringRules()
    }

    fun toggleRecurringRule(id: String) {
        updateState { current ->
            current.copy(
                recurring = current.recurring.map {
                    if (it.id == id) it.copy(active = !it.active) else it
                }
            )
        }
    }

    fun deleteRecurringRule(id: String) {
        updateState { current ->
            current.copy(recurring = current.recurring.filter { it.id != id })
        }
    }

    // --- Settings & Reset ---
    fun updateLanguage(lang: String) {
        updateState { it.copy(settings = it.settings.copy(language = lang)) }
    }

    fun updateTheme(theme: String) {
        updateState { it.copy(settings = it.settings.copy(theme = theme)) }
    }

    fun updateDashboardCurrency(cur: String) {
        updateState { it.copy(settings = it.settings.copy(dashboardCurrency = cur)) }
    }

    fun setPin(pin: String) {
        updateState { it.copy(settings = it.settings.copy(pinEnabled = true, pinCode = pin)) }
    }

    fun disablePin() {
        updateState { it.copy(settings = it.settings.copy(pinEnabled = false, pinCode = null)) }
    }

    fun resetAllData() {
        val def = defaultState()
        persist(def)
    }

    fun restoreBackup(jsonString: String): Boolean {
        return try {
            val parsed = json.decodeFromString<AppDataState>(jsonString)
            persist(parsed)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun exportBackupJson(): String {
        return json.encodeToString(_dataState.value)
    }

    fun exportCsv(): String {
        val current = _dataState.value
        val sb = StringBuilder()
        sb.append("date,type,cashbox,currency,category,description,amount\n")
        val activeTxns = current.transactions.filter { !it.deleted }
            .sortedBy { it.date }
        for (tx in activeTxns) {
            val cb = current.cashboxes.find { it.id == tx.cashboxId }
            val cat = current.categories.find { it.id == tx.categoryId }
            val catName = cat?.name ?: cat?.nameKey ?: ""
            val desc = tx.description.replace("\"", "\"\"")
            sb.append("${tx.date},${tx.type},\"${cb?.name ?: ""}\",${cb?.currency ?: ""},\"$catName\",\"$desc\",${tx.amount}\n")
        }
        return sb.toString()
    }

    // --- Recurring Rule Processing ---
    private fun processRecurringRules() {
        val today = getTodayISO()
        val current = _dataState.value
        var changed = false
        val newTxns = mutableListOf<Transaction>()
        val updatedRules = current.recurring.map { rule ->
            if (!rule.active) rule
            else {
                var curRule = rule
                var guard = 0
                while (curRule.nextDate <= today && guard < 600 && (curRule.endDate == null || curRule.nextDate <= curRule.endDate)) {
                    newTxns.add(
                        Transaction(
                            id = UUID.randomUUID().toString(),
                            type = curRule.type,
                            amount = curRule.amount,
                            cashboxId = curRule.cashboxId,
                            categoryId = curRule.categoryId,
                            description = curRule.description,
                            date = curRule.nextDate,
                            recurringId = curRule.id,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    val next = nextDateFrom(curRule.nextDate, curRule.frequency)
                    val ended = curRule.endDate != null && next > curRule.endDate
                    curRule = curRule.copy(
                        nextDate = next,
                        active = !ended
                    )
                    changed = true
                    guard++
                    if (ended) break
                }
                curRule
            }
        }
        if (changed) {
            persist(current.copy(
                transactions = current.transactions + newTxns,
                recurring = updatedRules
            ))
        }
    }

    fun nextDateFrom(dateISO: String, frequency: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        try {
            val d = sdf.parse(dateISO) ?: Date()
            cal.time = d
            when (frequency) {
                "daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                "weekly" -> cal.add(Calendar.DAY_OF_YEAR, 7)
                "monthly" -> cal.add(Calendar.MONTH, 1)
                else -> cal.add(Calendar.MONTH, 1)
            }
            return sdf.format(cal.time)
        } catch (e: Exception) {
            return dateISO
        }
    }
}
