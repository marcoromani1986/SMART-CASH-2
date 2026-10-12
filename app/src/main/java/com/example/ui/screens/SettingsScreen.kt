package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
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
import com.example.ui.i18n.I18n
import com.example.ui.theme.ColorClay
import com.example.ui.theme.ColorClayBg
import com.example.viewmodel.SmartCashViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SmartCashViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dataState.collectAsState()
    val settings = state.settings
    val lang = settings.language

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Text(
                text = I18n.t("settings_title", lang),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Appearance Section
        item {
            Text(
                text = I18n.t("appearance", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = I18n.t("language", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("en" to "EN", "it" to "IT", "ar" to "عربي").forEach { (code, label) ->
                                val isSel = lang == code
                                FilterChip(
                                    selected = isSel,
                                    onClick = { viewModel.repository.updateLanguage(code) },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("lang_chip_$code")
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // Theme
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = I18n.t("appearance", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = settings.theme == "auto",
                                onClick = { viewModel.repository.updateTheme("auto") },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                            ) {
                                Text(I18n.t("auto", lang))
                            }
                            SegmentedButton(
                                selected = settings.theme == "light",
                                onClick = { viewModel.repository.updateTheme("light") },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                            ) {
                                Text(I18n.t("light", lang))
                            }
                            SegmentedButton(
                                selected = settings.theme == "dark",
                                onClick = { viewModel.repository.updateTheme("dark") },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                            ) {
                                Text(I18n.t("dark", lang))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Security Section
        item {
            Text(
                text = I18n.t("security", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = I18n.t("app_lock", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = I18n.t("app_lock_sub", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Switch(
                        checked = settings.pinEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                viewModel.showPinSetupDialog.value = true
                            } else {
                                viewModel.repository.disablePin()
                                viewModel.showToast("App lock disabled")
                            }
                        },
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Overview / Shortcuts
        item {
            Text(
                text = I18n.t("overview", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    SettingsNavigationRow(
                        title = I18n.t("categories_title", lang),
                        subtitle = I18n.t("manage_categories", lang),
                        onClick = { viewModel.navigateTo("categories") }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    SettingsNavigationRow(
                        title = I18n.t("recurring_title", lang),
                        subtitle = I18n.t("manage_recurring", lang),
                        onClick = { viewModel.navigateTo("recurring") }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    SettingsNavigationRow(
                        title = I18n.t("budgets", lang),
                        subtitle = I18n.t("manage_budgets", lang),
                        onClick = { viewModel.showBudgetsDialog.value = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Data Management Section
        item {
            Text(
                text = I18n.t("data_title", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    SettingsNavigationRow(
                        title = I18n.t("backup", lang),
                        subtitle = I18n.t("backup_sub", lang),
                        icon = Icons.Default.Download,
                        onClick = { viewModel.showBackupDialog.value = true }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    SettingsNavigationRow(
                        title = I18n.t("export_csv", lang),
                        subtitle = I18n.t("export_csv_sub", lang),
                        icon = Icons.Default.Download,
                        onClick = { viewModel.showCsvExportDialog.value = true }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                    SettingsNavigationRow(
                        title = I18n.t("restore", lang),
                        subtitle = I18n.t("restore_sub", lang),
                        icon = Icons.Default.Upload,
                        onClick = { viewModel.showRestoreDialog.value = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Danger Zone
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ColorClayBg.copy(alpha = 0.3f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ColorClay))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = I18n.t("danger_zone", lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ColorClay
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = I18n.t("clear_all_sub", lang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.showClearAllDialog.value = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_all_data_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorClayBg,
                            contentColor = ColorClay
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = I18n.t("clear_all", lang),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "${I18n.t("app_name", lang)} · ${I18n.t("version", lang)}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Icon(
            imageVector = icon ?: Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
