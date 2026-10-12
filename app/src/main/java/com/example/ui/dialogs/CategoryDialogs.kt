package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Category
import com.example.ui.components.ColorPaletteSelector
import com.example.ui.components.EmojiGridSelector
import com.example.ui.i18n.I18n
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheet(
    category: Category?,
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val isEdit = category != null

    var name by remember {
        mutableStateOf(
            category?.name ?: category?.nameKey?.let { I18n.t(it, lang) } ?: ""
        )
    }
    var type by remember { mutableStateOf(category?.type ?: "expense") }
    var icon by remember { mutableStateOf(category?.icon ?: "📁") }
    var color by remember { mutableStateOf(category?.color ?: "#6B7280") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("category_sheet")
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
                    text = if (isEdit) I18n.t("edit_category", lang) else I18n.t("new_category", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(I18n.t("category_name", lang)) },
                placeholder = { Text(I18n.t("category_name_ph", lang)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cat_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Type
            Text(
                text = I18n.t("applies_to", lang),
                style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("expense" to I18n.t("expense", lang), "income" to I18n.t("income", lang), "both" to I18n.t("both", lang)).forEachIndexed { index, (valType, label) ->
                    SegmentedButton(
                        selected = type == valType,
                        onClick = { type = valType },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                    ) {
                        Text(label)
                    }
                }
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
                        if (isEdit && category != null) {
                            viewModel.repository.updateCategory(
                                id = category.id,
                                name = name.trim(),
                                icon = icon,
                                color = color,
                                type = type
                            )
                        } else {
                            viewModel.repository.addCategory(
                                name = name.trim(),
                                icon = icon,
                                color = color,
                                type = type
                            )
                        }
                        viewModel.showToast(I18n.t("saved", lang))
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cat_save_button"),
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
