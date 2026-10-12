package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SmartCashRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmartCashViewModel(application: Application) : AndroidViewModel(application) {

    val repository = SmartCashRepository(application)
    val dataState: StateFlow<AppDataState> = repository.dataState

    // Navigation state
    private val _currentRoute = MutableStateFlow("dashboard")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _selectedCashboxId = MutableStateFlow<String?>(null)
    val selectedCashboxId: StateFlow<String?> = _selectedCashboxId.asStateFlow()

    // PIN lock state
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _pinBuffer = MutableStateFlow("")
    val pinBuffer: StateFlow<String> = _pinBuffer.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Ledger filters
    val ledgerSearch = MutableStateFlow("")
    val ledgerCashboxFilter = MutableStateFlow("all")
    val ledgerCategoryFilter = MutableStateFlow("all")
    val ledgerSort = MutableStateFlow("newest") // "newest", "oldest"

    // Reports filters
    val reportsRange = MutableStateFlow("month") // "today", "month", "custom"
    val reportsFrom = MutableStateFlow(firstDayOfMonthISO())
    val reportsTo = MutableStateFlow(todayISO())

    // Snackbars
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _snackbarAction = MutableStateFlow<String?>(null)
    val snackbarAction: StateFlow<String?> = _snackbarAction.asStateFlow()

    private var onSnackbarActionCallback: (() -> Unit)? = null

    // Dialog & sheet states
    val showActionChooser = MutableStateFlow(false)
    val editingTransaction = MutableStateFlow<Transaction?>(null)
    val presetTransactionType = MutableStateFlow<String?>("expense")
    val showTransactionDialog = MutableStateFlow(false)

    val showTransferDialog = MutableStateFlow(false)

    val editingCashbox = MutableStateFlow<Cashbox?>(null)
    val showCashboxDialog = MutableStateFlow(false)

    val editingCategory = MutableStateFlow<Category?>(null)
    val showCategoryDialog = MutableStateFlow(false)

    val editingRecurring = MutableStateFlow<RecurringRule?>(null)
    val showRecurringDialog = MutableStateFlow(false)

    val showBudgetsDialog = MutableStateFlow(false)
    val showPinSetupDialog = MutableStateFlow(false)
    val showClearAllDialog = MutableStateFlow(false)
    val showRestoreDialog = MutableStateFlow(false)
    val showBackupDialog = MutableStateFlow(false)
    val showCsvExportDialog = MutableStateFlow(false)

    init {
        val settings = dataState.value.settings
        if (settings.pinEnabled && !settings.pinCode.isNullOrEmpty()) {
            _isLocked.value = true
        }
    }

    fun navigateTo(route: String, cashboxId: String? = null) {
        _currentRoute.value = route
        _selectedCashboxId.value = cashboxId
    }

    fun showToast(msg: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
        _snackbarMessage.value = msg
        _snackbarAction.value = actionLabel
        onSnackbarActionCallback = onAction
    }

    fun clearToast() {
        _snackbarMessage.value = null
        _snackbarAction.value = null
        onSnackbarActionCallback = null
    }

    fun triggerSnackbarAction() {
        onSnackbarActionCallback?.invoke()
        clearToast()
    }

    // --- PIN Pad ---
    fun onPinDigit(digit: String) {
        val current = _pinBuffer.value
        if (current.length < 4) {
            val updated = current + digit
            _pinBuffer.value = updated
            _pinError.value = null
            if (updated.length == 4) {
                val correctPin = dataState.value.settings.pinCode
                if (updated == correctPin) {
                    _isLocked.value = false
                    _pinBuffer.value = ""
                } else {
                    _pinError.value = "wrong_pin"
                    _pinBuffer.value = ""
                }
            }
        }
    }

    fun onPinBackspace() {
        val current = _pinBuffer.value
        if (current.isNotEmpty()) {
            _pinBuffer.value = current.dropLast(1)
        }
    }

    // --- Calculations ---
    fun cashboxBalance(cashboxId: String): Double {
        val state = dataState.value
        val cb = state.cashboxes.find { it.id == cashboxId } ?: return 0.0
        var bal = cb.initialBalance
        for (tx in state.transactions) {
            if (tx.deleted) continue
            if (tx.type == "income" && tx.cashboxId == cashboxId) {
                bal += tx.amount
            } else if (tx.type == "expense" && tx.cashboxId == cashboxId) {
                bal -= tx.amount
            } else if (tx.type == "transfer") {
                if (tx.cashboxId == cashboxId) bal -= tx.amount
                if (tx.toCashboxId == cashboxId) bal += (tx.toAmount ?: tx.amount)
            }
        }
        return bal
    }

    fun totalsByCurrency(): Map<String, Double> {
        val state = dataState.value
        val map = mutableMapOf("EUR" to 0.0, "USD" to 0.0, "EGP" to 0.0)
        for (cb in state.cashboxes) {
            map[cb.currency] = (map[cb.currency] ?: 0.0) + cashboxBalance(cb.id)
        }
        return map
    }

    fun monthSpendForCategory(categoryId: String): Double {
        val ym = todayISO().take(7)
        return dataState.value.transactions
            .filter { !it.deleted && it.type == "expense" && it.categoryId == categoryId && it.date.take(7) == ym }
            .sumOf { it.amount }
    }

    fun getCategory(id: String?): Category? {
        if (id == null) return null
        return dataState.value.categories.find { it.id == id }
    }

    fun getCashbox(id: String?): Cashbox? {
        if (id == null) return null
        return dataState.value.cashboxes.find { it.id == id }
    }

    fun todayISO(): String = repository.getTodayISO()

    private fun firstDayOfMonthISO(): String {
        val today = todayISO()
        return if (today.length >= 7) "${today.take(7)}-01" else today
    }
}
