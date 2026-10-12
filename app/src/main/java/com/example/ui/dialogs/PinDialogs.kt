package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.I18n
import com.example.ui.theme.ColorClay
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.viewmodel.SmartCashViewModel

@Composable
fun PinSetupDialog(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    var stage by remember { mutableStateOf(1) } // 1: enter, 2: confirm
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = I18n.t("app_lock", lang),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (stage == 1) I18n.t("set_pin", lang) else I18n.t("confirm_pin", lang),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = currentPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            currentPin = it
                            errorMsg = null
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SpaceMonoFontFamily,
                        letterSpacing = 8.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = I18n.t(errorMsg!!, lang),
                        style = MaterialTheme.typography.bodySmall.copy(color = ColorClay)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentPin.length != 4) {
                        errorMsg = "invalid_pin"
                        return@Button
                    }
                    if (stage == 1) {
                        firstPin = currentPin
                        currentPin = ""
                        stage = 2
                    } else {
                        if (currentPin == firstPin) {
                            viewModel.repository.setPin(currentPin)
                            viewModel.showToast(I18n.t("saved", lang))
                            onDismiss()
                        } else {
                            errorMsg = "pin_mismatch"
                            currentPin = ""
                            stage = 1
                            firstPin = ""
                        }
                    }
                }
            ) {
                Text(text = if (stage == 1) "Next" else I18n.t("done", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(I18n.t("cancel", lang))
            }
        }
    )
}

@Composable
fun BackupDialog(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val jsonString = remember { viewModel.repository.exportBackupJson() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(I18n.t("backup", lang)) },
        text = {
            Column {
                Text(
                    text = I18n.t("backup_sub", lang),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = jsonString,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = SpaceMonoFontFamily)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("SmartCash Backup", jsonString))
                    viewModel.showToast("Backup JSON copied to clipboard")
                    onDismiss()
                }
            ) {
                Text("Copy to Clipboard")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
        }
    )
}

@Composable
fun CsvExportDialog(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    val csvString = remember { viewModel.repository.exportCsv() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(I18n.t("export_csv", lang)) },
        text = {
            Column {
                Text(
                    text = I18n.t("export_csv_sub", lang),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = csvString,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = SpaceMonoFontFamily)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("SmartCash CSV", csvString))
                    viewModel.showToast(I18n.t("csv_done", lang))
                    onDismiss()
                }
            ) {
                Text("Copy CSV")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(I18n.t("close", lang)) }
        }
    )
}

@Composable
fun RestoreDialog(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language
    var jsonText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(I18n.t("restore", lang)) },
        text = {
            Column {
                Text(
                    text = I18n.t("confirm_restore", lang),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it; error = null },
                    placeholder = { Text("Paste JSON backup here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = error!!, color = ColorClay, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonText.isNotBlank()) {
                        val success = viewModel.repository.restoreBackup(jsonText.trim())
                        if (success) {
                            viewModel.showToast(I18n.t("restored", lang))
                            onDismiss()
                        } else {
                            error = "Invalid backup JSON"
                        }
                    }
                }
            ) {
                Text(I18n.t("restore", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(I18n.t("cancel", lang)) }
        }
    )
}

@Composable
fun ClearAllConfirmDialog(
    viewModel: SmartCashViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(I18n.t("confirm_delete_title", lang)) },
        text = { Text(I18n.t("confirm_clear", lang)) },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.repository.resetAllData()
                    viewModel.navigateTo("dashboard")
                    viewModel.showToast(I18n.t("cleared", lang))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorClay)
            ) {
                Text(I18n.t("clear_all", lang))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(I18n.t("cancel", lang)) }
        }
    )
}
