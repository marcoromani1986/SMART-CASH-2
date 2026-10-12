package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Transaction
import com.example.ui.i18n.I18n
import com.example.ui.theme.*
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    transaction: Transaction?,
    presetType: String?,
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val isEdit = transaction != null

    var type by remember { mutableStateOf(transaction?.type ?: presetType ?: "expense") }
    var amountText by remember { mutableStateOf(if (transaction != null) transaction.amount.toString() else "") }
    var cashboxId by remember {
        mutableStateOf(
            transaction?.cashboxId
                ?: state.settings.lastCashboxId?.takeIf { id -> state.cashboxes.any { it.id == id } }
                ?: state.cashboxes.firstOrNull()?.id
                ?: ""
        )
    }
    var categoryId by remember { mutableStateOf(transaction?.categoryId ?: "") }
    var description by remember { mutableStateOf(transaction?.description ?: "") }
    var date by remember { mutableStateOf(transaction?.date ?: viewModel.todayISO()) }

    var makeRecurring by remember { mutableStateOf(false) }
    var frequency by remember { mutableStateOf("monthly") }

    val categoriesForType = state.categories.filter { it.type == type || it.type == "both" }

    LaunchedEffect(type) {
        if (categoriesForType.none { it.id == categoryId }) {
            categoryId = categoriesForType.firstOrNull()?.id ?: ""
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("add_edit_transaction_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) I18n.t("edit_transaction", lang) else I18n.t("add_transaction", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onDismiss) {
                    Text(I18n.t("close", lang))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Type Segment
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == "expense",
                    onClick = { type = "expense" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(I18n.t("expense", lang))
                }
                SegmentedButton(
                    selected = type == "income",
                    onClick = { type = "income" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(I18n.t("income", lang))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(I18n.t("amount", lang)) },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cashbox selection
            Text(
                text = I18n.t("cashbox", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.cashboxes) { cb ->
                    FilterChip(
                        selected = cashboxId == cb.id,
                        onClick = { cashboxId = cb.id },
                        label = { Text("${cb.icon} ${cb.name} (${cb.currency})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category selection
            Text(
                text = I18n.t("category", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categoriesForType) { cat ->
                    val catName = cat.name ?: cat.nameKey?.let { I18n.t(it, lang) } ?: ""
                    FilterChip(
                        selected = categoryId == cat.id,
                        onClick = { categoryId = cat.id },
                        label = { Text("${cat.icon} $catName") },
                        modifier = Modifier.testTag("cat_chip_${cat.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(I18n.t("description", lang)) },
                placeholder = { Text(I18n.t("description_ph", lang)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_desc_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Date
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text(I18n.t("date", lang)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_date_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (!isEdit) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = I18n.t("make_recurring", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Switch(
                        checked = makeRecurring,
                        onCheckedChange = { makeRecurring = it }
                    )
                }
                if (makeRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = frequency == "monthly",
                            onClick = { frequency = "monthly" },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text(I18n.t("monthly", lang))
                        }
                        SegmentedButton(
                            selected = frequency == "weekly",
                            onClick = { frequency = "weekly" },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text(I18n.t("weekly", lang))
                        }
                        SegmentedButton(
                            selected = frequency == "daily",
                            onClick = { frequency = "daily" },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Text(I18n.t("daily", lang))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0 && cashboxId.isNotEmpty()) {
                        if (isEdit && transaction != null) {
                            viewModel.repository.updateTransaction(
                                id = transaction.id,
                                type = type,
                                amount = amt,
                                cashboxId = cashboxId,
                                categoryId = categoryId.takeIf { it.isNotEmpty() },
                                description = description.trim(),
                                date = date
                            )
                        } else {
                            viewModel.repository.addTransaction(
                                type = type,
                                amount = amt,
                                cashboxId = cashboxId,
                                categoryId = categoryId.takeIf { it.isNotEmpty() },
                                description = description.trim(),
                                date = date,
                                recurringFrequency = if (makeRecurring) frequency else null
                            )
                        }
                        viewModel.showToast(I18n.t("saved", lang))
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tx_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = I18n.t("save", lang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (isEdit && transaction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.repository.softDeleteTransaction(transaction.id)
                        viewModel.showToast(I18n.t("deleted", lang), I18n.t("undo", lang)) {
                            viewModel.repository.undoDeleteTransaction()
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_delete_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorClayBg,
                        contentColor = ColorClay
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = I18n.t("delete", lang),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    if (state.cashboxes.size < 2) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(I18n.t("transfer_title", lang)) },
            text = { Text(I18n.t("need_two_cashboxes", lang)) },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
            }
        )
        return
    }

    var fromId by remember { mutableStateOf(state.cashboxes[0].id) }
    var toId by remember { mutableStateOf(state.cashboxes[1].id) }
    var amountText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("1.0") }

    val fromBox = state.cashboxes.find { it.id == fromId }
    val toBox = state.cashboxes.find { it.id == toId }
    val needsRate = fromBox != null && toBox != null && fromBox.currency != toBox.currency

    val amt = amountText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 1.0
    val receivedAmount = amt * rate

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("transfer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = I18n.t("transfer_title", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // From Cashbox
            Text(
                text = I18n.t("from_cashbox", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.cashboxes) { cb ->
                    FilterChip(
                        selected = fromId == cb.id,
                        onClick = { fromId = cb.id },
                        label = { Text("${cb.icon} ${cb.name} (${cb.currency})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // To Cashbox
            Text(
                text = I18n.t("to_cashbox", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.cashboxes) { cb ->
                    FilterChip(
                        selected = toId == cb.id,
                        onClick = { toId = cb.id },
                        label = { Text("${cb.icon} ${cb.name} (${cb.currency})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount (From currency)
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("${I18n.t("amount", lang)} (${fromBox?.currency ?: ""})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transfer_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (needsRate) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("${I18n.t("exchange_rate", lang)} (1 ${fromBox?.currency} = ? ${toBox?.currency})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${I18n.t("received_amount", lang)}: ${I18n.fmtAmount(receivedAmount, toBox?.currency ?: "")}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SpaceMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = ColorSage
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (amt > 0 && fromId != toId) {
                        viewModel.repository.addTransaction(
                            type = "transfer",
                            amount = amt,
                            cashboxId = fromId,
                            toCashboxId = toId,
                            toAmount = if (needsRate) receivedAmount else amt,
                            date = viewModel.todayISO()
                        )
                        viewModel.showToast(I18n.t("saved", lang))
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transfer_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = I18n.t("save", lang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionChooserSheet(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("action_chooser_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = I18n.t("add_transaction", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onDismiss()
                    viewModel.editingTransaction.value = null
                    viewModel.presetTransactionType.value = "expense"
                    viewModel.showTransactionDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_expense_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorInk)
            ) {
                Text(
                    text = "➖ ${I18n.t("expense", lang)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ColorBrassBright,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onDismiss()
                    viewModel.editingTransaction.value = null
                    viewModel.presetTransactionType.value = "income"
                    viewModel.showTransactionDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_income_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorSage)
            ) {
                Text(
                    text = "➕ ${I18n.t("income", lang)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    onDismiss()
                    viewModel.showTransferDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("action_transfer_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "🔁 ${I18n.t("transfer", lang)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
