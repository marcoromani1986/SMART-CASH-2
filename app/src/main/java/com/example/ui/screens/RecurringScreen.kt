package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.I18n
import com.example.ui.theme.ColorClay
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.viewmodel.SmartCashViewModel

@Composable
fun RecurringScreen(
    viewModel: SmartCashViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recurring_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            IconButton(
                onClick = { viewModel.navigateTo("settings") },
                modifier = Modifier.testTag("recurring_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = I18n.t("recurring_title", lang),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = I18n.t("manage_recurring", lang),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        if (state.recurring.isEmpty()) {
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
                        Text(text = "🔁", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = I18n.t("no_recurring", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = I18n.t("no_recurring_sub", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        } else {
            items(state.recurring) { r ->
                val cat = viewModel.getCategory(r.categoryId)
                val cb = viewModel.getCashbox(r.cashboxId)
                val catName = cat?.name ?: cat?.nameKey?.let { I18n.t(it, lang) } ?: "Category"
                val desc = r.description.ifEmpty { catName }
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(cat?.color ?: "#C08A2E"))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(parsedColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = cat?.icon ?: "🔁", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${cb?.name ?: ""} · ${I18n.t(r.frequency, lang)} · ${I18n.fmtAmount(r.amount, cb?.currency ?: "EUR")}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = I18n.t("next_run", lang, mapOf("date" to I18n.fmtDateShort(r.nextDate))),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceMonoFontFamily,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = r.active,
                            onCheckedChange = { viewModel.repository.toggleRecurringRule(r.id) },
                            modifier = Modifier.testTag("toggle_recurring_${r.id}")
                        )
                        IconButton(onClick = {
                            viewModel.editingRecurring.value = r
                            viewModel.showRecurringDialog.value = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit rule", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = {
                            viewModel.repository.deleteRecurringRule(r.id)
                            viewModel.showToast(I18n.t("recurring_deleted", lang))
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete rule",
                                tint = ColorClay,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    viewModel.editingRecurring.value = null
                    viewModel.showRecurringDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_recurring_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = I18n.t("new_recurring", lang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
