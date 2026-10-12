package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BudgetItemRow
import com.example.ui.components.CashboxHorizontalList
import com.example.ui.components.HeroBalanceCard
import com.example.ui.components.TransactionRow
import com.example.ui.i18n.I18n
import com.example.ui.theme.ColorBrass
import com.example.viewmodel.SmartCashViewModel

@Composable
fun DashboardScreen(
    viewModel: SmartCashViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val cur = state.settings.dashboardCurrency
    val totals = viewModel.totalsByCurrency()
    val totalInCur = totals[cur] ?: 0.0

    val currencies = listOf("EUR", "USD", "EGP")
    val recentTxns = state.transactions
        .filter { !it.deleted }
        .sortedWith(compareByDescending<com.example.model.Transaction> { it.date }.thenByDescending { it.createdAt })
        .take(6)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Hero Balance
        item {
            HeroBalanceCard(
                currentCurrency = cur,
                currencies = currencies,
                totalAmount = totalInCur,
                onCurrencySelect = { viewModel.repository.updateDashboardCurrency(it) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Your Cashboxes Section Head
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = I18n.t("your_cashboxes", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = { viewModel.navigateTo("cashboxes") },
                    modifier = Modifier.testTag("view_cashboxes_button")
                ) {
                    Text(
                        text = I18n.t("view_all", lang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ColorBrass,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            CashboxHorizontalList(
                cashboxes = state.cashboxes,
                balanceProvider = { viewModel.cashboxBalance(it) },
                onCashboxClick = { cbId -> viewModel.navigateTo("cashbox_detail", cbId) },
                onAddCashboxClick = {
                    viewModel.editingCashbox.value = null
                    viewModel.showCashboxDialog.value = true
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Budgets Section (if any)
        if (state.budgets.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = I18n.t("budgets", lang),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(
                        onClick = { viewModel.showBudgetsDialog.value = true },
                        modifier = Modifier.testTag("manage_budgets_button")
                    ) {
                        Text(
                            text = I18n.t("manage_budgets", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ColorBrass,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        state.budgets.take(4).forEach { b ->
                            val cat = viewModel.getCategory(b.categoryId)
                            val spent = viewModel.monthSpendForCategory(b.categoryId)
                            BudgetItemRow(
                                budget = b,
                                category = cat,
                                spent = spent,
                                lang = lang
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Recent Activity Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = I18n.t("recent_activity", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (recentTxns.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.navigateTo("ledger") },
                        modifier = Modifier.testTag("view_all_transactions_button")
                    ) {
                        Text(
                            text = I18n.t("view_all", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ColorBrass,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (recentTxns.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📭", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = I18n.t("no_transactions", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = I18n.t("no_transactions_sub", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        recentTxns.forEach { tx ->
                            val cb = viewModel.getCashbox(tx.cashboxId)
                            val toCb = viewModel.getCashbox(tx.toCashboxId)
                            val cat = viewModel.getCategory(tx.categoryId)
                            TransactionRow(
                                transaction = tx,
                                cashbox = cb,
                                toCashbox = toCb,
                                category = cat,
                                lang = lang,
                                onClick = {
                                    viewModel.editingTransaction.value = tx
                                    viewModel.showTransactionDialog.value = true
                                }
                            )
                            if (tx != recentTxns.last()) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
