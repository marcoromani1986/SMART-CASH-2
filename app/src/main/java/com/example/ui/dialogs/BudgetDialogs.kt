package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.I18n
import com.example.ui.theme.ColorClay
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsManagerSheet(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    val expenseCategories = state.categories.filter { it.type == "expense" || it.type == "both" }
    var selectedCategoryId by remember { mutableStateOf(expenseCategories.firstOrNull()?.id ?: "") }
    var currency by remember { mutableStateOf("EUR") }
    var amountText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("budgets_sheet")
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
                    text = I18n.t("budgets", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current budgets list
            if (state.budgets.isEmpty()) {
                Text(
                    text = I18n.t("no_budgets", lang),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        state.budgets.forEach { b ->
                            val cat = viewModel.getCategory(b.categoryId)
                            val catName = cat?.name ?: cat?.nameKey?.let { I18n.t(it, lang) } ?: "Category"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = cat?.icon ?: "📁", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = catName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = I18n.fmtAmount(b.limit, b.currency),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = SpaceMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    IconButton(onClick = { viewModel.repository.removeBudget(b.id) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove budget",
                                            tint = ColorClay,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = I18n.t("add_budget", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            Text(
                text = I18n.t("category", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(expenseCategories) { cat ->
                    val catName = cat.name ?: cat.nameKey?.let { I18n.t(it, lang) } ?: ""
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text("${cat.icon} $catName") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currency
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("EUR", "USD", "EGP").forEachIndexed { index, cur ->
                    SegmentedButton(
                        selected = currency == cur,
                        onClick = { currency = cur },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(cur)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Limit Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(I18n.t("budget_limit", lang)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val limit = amountText.toDoubleOrNull()
                    if (limit != null && limit > 0 && selectedCategoryId.isNotEmpty()) {
                        viewModel.repository.setBudget(selectedCategoryId, limit, currency)
                        amountText = ""
                        viewModel.showToast(I18n.t("saved", lang))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = I18n.t("add_budget", lang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
