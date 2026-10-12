package com.example.ui.dialogs

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.RecurringRule
import com.example.ui.i18n.I18n
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringSheet(
    rule: RecurringRule?,
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val isEdit = rule != null

    var type by remember { mutableStateOf(rule?.type ?: "expense") }
    var amountText by remember { mutableStateOf(if (rule != null) rule.amount.toString() else "") }
    var cashboxId by remember { mutableStateOf(rule?.cashboxId ?: state.cashboxes.firstOrNull()?.id ?: "") }
    var categoryId by remember { mutableStateOf(rule?.categoryId ?: "") }
    var description by remember { mutableStateOf(rule?.description ?: "") }
    var frequency by remember { mutableStateOf(rule?.frequency ?: "monthly") }
    var startDate by remember { mutableStateOf(rule?.startDate ?: viewModel.todayISO()) }
    var endDate by remember { mutableStateOf(rule?.endDate ?: "") }

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
        modifier = Modifier.testTag("recurring_sheet")
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
                    text = if (isEdit) I18n.t("edit_recurring", lang) else I18n.t("new_recurring", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Type
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

            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(I18n.t("amount", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rec_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cashbox
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
                        label = { Text("${cb.icon} ${cb.name}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category
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
                        label = { Text("${cat.icon} $catName") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(I18n.t("description", lang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Frequency
            Text(
                text = I18n.t("frequency", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("monthly" to I18n.t("monthly", lang), "weekly" to I18n.t("weekly", lang), "daily" to I18n.t("daily", lang)).forEachIndexed { index, (f, label) ->
                    SegmentedButton(
                        selected = frequency == f,
                        onClick = { frequency = f },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Starts date
            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text(I18n.t("starts", lang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Ends date (optional)
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text(I18n.t("ends_optional", lang)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0 && cashboxId.isNotEmpty()) {
                        if (isEdit && rule != null) {
                            viewModel.repository.updateRecurringRule(
                                id = rule.id,
                                type = type,
                                amount = amt,
                                cashboxId = cashboxId,
                                categoryId = categoryId,
                                description = description.trim(),
                                frequency = frequency,
                                startDate = startDate,
                                endDate = endDate.takeIf { it.isNotBlank() }
                            )
                        } else {
                            viewModel.repository.addRecurringRule(
                                type = type,
                                amount = amt,
                                cashboxId = cashboxId,
                                categoryId = categoryId,
                                description = description.trim(),
                                frequency = frequency,
                                startDate = startDate,
                                endDate = endDate.takeIf { it.isNotBlank() }
                            )
                        }
                        viewModel.showToast(I18n.t("saved", lang))
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rec_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEdit) I18n.t("save", lang) else I18n.t("create", lang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
