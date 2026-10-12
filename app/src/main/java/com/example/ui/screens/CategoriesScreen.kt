package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.Category
import com.example.ui.i18n.I18n
import com.example.ui.theme.ColorClay
import com.example.viewmodel.SmartCashViewModel

@Composable
fun CategoriesScreen(
    viewModel: SmartCashViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.dataState.collectAsState()
    val lang = state.settings.language

    val incomeCats = state.categories.filter { it.type == "income" || it.type == "both" }
    val expenseCats = state.categories.filter { it.type == "expense" || it.type == "both" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("categories_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            IconButton(
                onClick = { viewModel.navigateTo("settings") },
                modifier = Modifier.testTag("categories_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = I18n.t("categories_title", lang),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = I18n.t("manage_categories", lang),
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Income Categories
        item {
            Text(
                text = I18n.t("income", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(incomeCats) { cat ->
            CategoryListItem(
                category = cat,
                lang = lang,
                onEdit = {
                    viewModel.editingCategory.value = cat
                    viewModel.showCategoryDialog.value = true
                },
                onDelete = {
                    viewModel.repository.deleteCategory(cat.id)
                    viewModel.showToast(I18n.t("category_deleted", lang))
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = I18n.t("expense", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(expenseCats) { cat ->
            CategoryListItem(
                category = cat,
                lang = lang,
                onEdit = {
                    viewModel.editingCategory.value = cat
                    viewModel.showCategoryDialog.value = true
                },
                onDelete = {
                    viewModel.repository.deleteCategory(cat.id)
                    viewModel.showToast(I18n.t("category_deleted", lang))
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    viewModel.editingCategory.value = null
                    viewModel.showCategoryDialog.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_category_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = I18n.t("new_category", lang),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CategoryListItem(
    category: Category,
    lang: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val name = category.name ?: category.nameKey?.let { I18n.t(it, lang) } ?: "Category"
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(parsedColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.icon, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit category", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete category",
                    tint = ColorClay,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
