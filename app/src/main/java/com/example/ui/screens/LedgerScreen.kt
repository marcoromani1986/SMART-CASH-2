package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.components.TransactionRow
import com.example.ui.i18n.I18n
import com.example.ui.theme.ColorBrass
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    viewModel: SmartCashViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    val search by viewModel.ledgerSearch.collectAsState()
    val selectedCb by viewModel.ledgerCashboxFilter.collectAsState()
    val selectedCat by viewModel.ledgerCategoryFilter.collectAsState()
    val sort by viewModel.ledgerSort.collectAsState()

    var filtered = state.transactions.filter { !it.deleted }

    if (selectedCb != "all") {
        filtered = filtered.filter { it.cashboxId == selectedCb || it.toCashboxId == selectedCb }
    }

    if (selectedCat != "all") {
        filtered = filtered.filter { it.categoryId == selectedCat }
    }

    if (search.isNotBlank()) {
        val q = search.trim().lowercase()
        filtered = filtered.filter { tx ->
            val cat = viewModel.getCategory(tx.categoryId)
            val catName = (cat?.name ?: cat?.nameKey?.let { I18n.t(it, lang) } ?: "").lowercase()
            tx.description.lowercase().contains(q) || catName.contains(q)
        }
    }

    val sortedList = if (sort == "newest") {
        filtered.sortedWith(compareByDescending<com.example.model.Transaction> { it.date }.thenByDescending { it.createdAt })
    } else {
        filtered.sortedWith(compareBy<com.example.model.Transaction> { it.date }.thenBy { it.createdAt })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ledger_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Text(
                text = I18n.t("ledger_title", lang),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { viewModel.ledgerSearch.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_search_field"),
                placeholder = { Text(I18n.t("search_ph", lang)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { viewModel.ledgerSearch.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Cashbox Filter Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCb == "all",
                        onClick = { viewModel.ledgerCashboxFilter.value = "all" },
                        label = { Text(I18n.t("all_cashboxes", lang)) },
                        modifier = Modifier.testTag("filter_cb_all")
                    )
                }
                items(state.cashboxes) { cb ->
                    FilterChip(
                        selected = selectedCb == cb.id,
                        onClick = { viewModel.ledgerCashboxFilter.value = cb.id },
                        label = { Text("${cb.icon} ${cb.name}") },
                        modifier = Modifier.testTag("filter_cb_${cb.id}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Category Filter Pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCat == "all",
                        onClick = { viewModel.ledgerCategoryFilter.value = "all" },
                        label = { Text(I18n.t("all_categories", lang)) },
                        modifier = Modifier.testTag("filter_cat_all")
                    )
                }
                items(state.categories) { cat ->
                    val catName = cat.name ?: cat.nameKey?.let { I18n.t(it, lang) } ?: ""
                    FilterChip(
                        selected = selectedCat == cat.id,
                        onClick = { viewModel.ledgerCategoryFilter.value = cat.id },
                        label = { Text("${cat.icon} $catName") },
                        modifier = Modifier.testTag("filter_cat_${cat.id}")
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Sort Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = sort == "newest",
                        onClick = { viewModel.ledgerSort.value = "newest" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(I18n.t("newest", lang))
                    }
                    SegmentedButton(
                        selected = sort == "oldest",
                        onClick = { viewModel.ledgerSort.value = "oldest" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(I18n.t("oldest", lang))
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (sortedList.isEmpty()) {
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
                        Text(text = "🔎", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = I18n.t("no_results", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                        sortedList.forEach { tx ->
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
                            if (tx != sortedList.last()) {
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
