package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Cashbox(
    val id: String,
    val name: String,
    val currency: String, // "EUR", "USD", "EGP"
    val icon: String = "💰",
    val color: String = "#C08A2E",
    val initialBalance: Double = 0.0
)

@Serializable
data class Transaction(
    val id: String,
    val type: String, // "income", "expense", "transfer"
    val amount: Double,
    val cashboxId: String,
    val toCashboxId: String? = null,
    val toAmount: Double? = null,
    val categoryId: String? = null,
    val description: String = "",
    val date: String, // YYYY-MM-DD
    val recurringId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val deleted: Boolean = false
)

@Serializable
data class Category(
    val id: String,
    val nameKey: String? = null,
    val name: String? = null,
    val icon: String = "📁",
    val color: String = "#6B7280",
    val type: String = "expense" // "income", "expense", "both"
)

@Serializable
data class RecurringRule(
    val id: String,
    val type: String, // "income", "expense"
    val amount: Double,
    val cashboxId: String,
    val categoryId: String,
    val description: String = "",
    val frequency: String = "monthly", // "daily", "weekly", "monthly"
    val startDate: String,
    val nextDate: String,
    val endDate: String? = null,
    val active: Boolean = true
)

@Serializable
data class Budget(
    val id: String,
    val categoryId: String,
    val limit: Double,
    val currency: String = "EUR"
)

@Serializable
data class AppSettings(
    val language: String = "en", // "en", "it", "ar"
    val theme: String = "auto", // "auto", "light", "dark"
    val pinEnabled: Boolean = false,
    val pinCode: String? = null,
    val lastCashboxId: String? = null,
    val dashboardCurrency: String = "EUR",
    val onboarded: Boolean = false
)

@Serializable
data class AppDataState(
    val version: Int = 1,
    val cashboxes: List<Cashbox> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val recurring: List<RecurringRule> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val settings: AppSettings = AppSettings()
)
