package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TransactionRow
import com.example.ui.i18n.I18n
import com.example.ui.theme.*
import com.example.viewmodel.SmartCashViewModel

@Composable
fun CashboxDetailScreen(
    cashboxId: String,
    viewModel: SmartCashViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val cb = state.cashboxes.find { it.id == cashboxId }

    if (cb == null) {
        LaunchedEffect(Unit) {
            viewModel.navigateTo("cashboxes")
        }
        return
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val bal = viewModel.cashboxBalance(cb.id)
    val txns = state.transactions
        .filter { !it.deleted && (it.cashboxId == cb.id || it.toCashboxId == cb.id) }
        .sortedWith(compareByDescending<com.example.model.Transaction> { it.date }.thenByDescending { it.createdAt })

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cashbox_detail_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            IconButton(
                onClick = { viewModel.navigateTo("cashboxes") },
                modifier = Modifier.testTag("cashbox_detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Hero Card for this Cashbox
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ColorInk),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF28345A), Color(0xFF121A33)),
                                radius = 800f
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = cb.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${cb.name} · ${cb.currency}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ColorBrassSoft,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = I18n.fmtAmount(bal, cb.currency),
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = if (bal < 0) ColorClay else ColorPaper,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Actions: Edit / Delete
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        viewModel.editingCashbox.value = cb
                        viewModel.showCashboxDialog.value = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_cashbox_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = I18n.t("edit", lang))
                }

                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("delete_cashbox_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorClayBg,
                        contentColor = ColorClay
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = I18n.t("delete", lang))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Activity Section
        item {
            Text(
                text = I18n.t("recent_activity", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (txns.isEmpty()) {
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
                        txns.forEach { tx ->
                            val fromCb = viewModel.getCashbox(tx.cashboxId)
                            val toCb = viewModel.getCashbox(tx.toCashboxId)
                            val cat = viewModel.getCategory(tx.categoryId)
                            TransactionRow(
                                transaction = tx,
                                cashbox = fromCb,
                                toCashbox = toCb,
                                category = cat,
                                lang = lang,
                                onClick = {
                                    viewModel.editingTransaction.value = tx
                                    viewModel.showTransactionDialog.value = true
                                }
                            )
                            if (tx != txns.last()) {
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = I18n.t("confirm_delete_title", lang)) },
            text = { Text(text = I18n.t("delete_cashbox_warn", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.repository.deleteCashbox(cb.id)
                        showDeleteConfirm = false
                        viewModel.navigateTo("cashboxes")
                        viewModel.showToast(I18n.t("cashbox_deleted", lang))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorClay)
                ) {
                    Text(text = I18n.t("delete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = I18n.t("cancel", lang))
                }
            }
        )
    }
}
