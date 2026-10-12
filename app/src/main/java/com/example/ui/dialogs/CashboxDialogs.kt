package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
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
import com.example.model.Cashbox
import com.example.ui.components.ColorPaletteSelector
import com.example.ui.components.EmojiGridSelector
import com.example.ui.i18n.I18n
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCashboxSheet(
    cashbox: Cashbox?,
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val isEdit = cashbox != null

    var name by remember { mutableStateOf(cashbox?.name ?: "") }
    var currency by remember { mutableStateOf(cashbox?.currency ?: "EUR") }
    var initialBalanceText by remember { mutableStateOf(if (cashbox != null) cashbox.initialBalance.toString() else "0") }
    var icon by remember { mutableStateOf(cashbox?.icon ?: "💰") }
    var color by remember { mutableStateOf(cashbox?.color ?: "#C08A2E") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("cashbox_sheet")
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
                    text = if (isEdit) I18n.t("edit_cashbox", lang) else I18n.t("new_cashbox", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(I18n.t("cashbox_name", lang)) },
                placeholder = { Text(I18n.t("cashbox_name_ph", lang)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cb_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Currency
            Text(
                text = I18n.t("currency", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
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

            if (!isEdit) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text(I18n.t("initial_balance", lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            EmojiGridSelector(
                selectedEmoji = icon,
                onSelect = { icon = it }
            )

            Spacer(modifier = Modifier.height(14.dp))
            ColorPaletteSelector(
                selectedHex = color,
                onSelect = { color = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (isEdit && cashbox != null) {
                            viewModel.repository.updateCashbox(
                                id = cashbox.id,
                                name = name.trim(),
                                currency = currency,
                                icon = icon,
                                color = color
                            )
                        } else {
                            val initBal = initialBalanceText.toDoubleOrNull() ?: 0.0
                            viewModel.repository.addCashbox(
                                name = name.trim(),
                                currency = currency,
                                icon = icon,
                                color = color,
                                initialBalance = initBal
                            )
                        }
                        viewModel.showToast(I18n.t("saved", lang))
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cb_save_button"),
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
