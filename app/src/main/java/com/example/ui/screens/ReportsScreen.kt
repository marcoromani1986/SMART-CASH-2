package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BudgetItemRow
import com.example.ui.i18n.I18n
import com.example.ui.theme.*
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: SmartCashViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    val range by viewModel.reportsRange.collectAsState()
    val fromDate by viewModel.reportsFrom.collectAsState()
    val toDate by viewModel.reportsTo.collectAsState()

    val today = viewModel.todayISO()
    val (effFrom, effTo) = when (range) {
        "today" -> today to today
        "month" -> (today.take(7) + "-01") to today
        else -> fromDate to toDate
    }

    val filteredTxns = state.transactions.filter { tx ->
        !tx.deleted && tx.type != "transfer" && tx.date in effFrom..effTo
    }

    // Totals by currency
    val totals = mutableMapOf<String, Pair<Double, Double>>() // currency -> (income, expense)
    for (tx in filteredTxns) {
        val cb = viewModel.getCashbox(tx.cashboxId) ?: continue
        val current = totals[cb.currency] ?: (0.0 to 0.0)
        if (tx.type == "income") {
            totals[cb.currency] = (current.first + tx.amount) to current.second
        } else if (tx.type == "expense") {
            totals[cb.currency] = current.first to (current.second + tx.amount)
        }
    }

    // Expense by category
    val catExpenses = mutableMapOf<String, Double>()
    for (tx in filteredTxns) {
        if (tx.type == "expense" && tx.categoryId != null) {
            catExpenses[tx.categoryId] = (catExpenses[tx.categoryId] ?: 0.0) + tx.amount
        }
    }
    val maxCatExpense = catExpenses.values.maxOrNull() ?: 1.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Text(
                text = I18n.t("reports_title", lang),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Range Segmented Buttons
        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = range == "today",
                    onClick = { viewModel.reportsRange.value = "today" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                ) {
                    Text(I18n.t("today", lang))
                }
                SegmentedButton(
                    selected = range == "month",
                    onClick = { viewModel.reportsRange.value = "month" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                ) {
                    Text(I18n.t("this_month", lang))
                }
                SegmentedButton(
                    selected = range == "custom",
                    onClick = { viewModel.reportsRange.value = "custom" },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                ) {
                    Text(I18n.t("custom_range", lang))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (range == "custom") {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = fromDate,
                        onValueChange = { viewModel.reportsFrom.value = it },
                        modifier = Modifier.weight(1f),
                        label = { Text(I18n.t("from", lang)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = toDate,
                        onValueChange = { viewModel.reportsTo.value = it },
                        modifier = Modifier.weight(1f),
                        label = { Text(I18n.t("to", lang)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Totals by Currency Grid
        if (totals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📊", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = I18n.t("no_transactions", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    totals.forEach { (currency, pair) ->
                        val (income, expense) = pair
                        val net = income - expense
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "$currency Summary",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = I18n.t("total_income", lang),
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = "+${I18n.fmtAmount(income, currency)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorSage
                                            )
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = I18n.t("total_expense", lang),
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = "-${I18n.fmtAmount(expense, currency)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorClay
                                            )
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = I18n.t("net", lang),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = I18n.fmtAmount(net, currency),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (net >= 0) ColorSage else ColorClay
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Budgets Section
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
                TextButton(onClick = { viewModel.showBudgetsDialog.value = true }) {
                    Text(
                        text = if (state.budgets.isEmpty()) I18n.t("set_budget", lang) else I18n.t("manage_budgets", lang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ColorBrass,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (state.budgets.isEmpty()) {
                Text(
                    text = I18n.t("no_budgets", lang),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        state.budgets.forEach { b ->
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
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // By Category Section
        item {
            Text(
                text = I18n.t("by_category", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (catExpenses.isEmpty()) {
            item {
                Text(
                    text = I18n.t("no_transactions", lang),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val sortedCats = catExpenses.toList().sortedByDescending { it.second }
                        sortedCats.forEach { (catId, amt) ->
                            val cat = viewModel.getCategory(catId)
                            val catName = cat?.name ?: cat?.nameKey?.let { I18n.t(it, lang) } ?: "Category"
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat?.color ?: "#C08A2E"))
                            } catch (e: Exception) {
                                ColorBrass
                            }
                            val fraction = (amt / maxCatExpense).coerceIn(0.0, 1.0).toFloat()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${cat?.icon ?: "📁"} $catName",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.width(90.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(catColor)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = I18n.fmtNum(amt),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
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
