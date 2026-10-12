package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Budget
import com.example.model.Cashbox
import com.example.model.Category
import com.example.model.Transaction
import com.example.ui.i18n.I18n
import com.example.ui.theme.*

@Composable
fun HeroBalanceCard(
    currentCurrency: String,
    currencies: List<String>,
    totalAmount: Double,
    onCurrencySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorInk
        ),
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
                Text(
                    text = "Total balance",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ColorBrassSoft,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = I18n.fmtAmount(totalAmount, currentCurrency),
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = ColorPaper,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.testTag("hero_balance_amount")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { cur ->
                        val isSelected = cur == currentCurrency
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) ColorBrassBright else Color.White.copy(alpha = 0.08f)
                                )
                                .clickable { onCurrencySelect(cur) }
                                .padding(vertical = 8.dp)
                                .testTag("currency_tab_$cur"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cur,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isSelected) ColorInk else Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashboxHorizontalList(
    cashboxes: List<Cashbox>,
    balanceProvider: (String) -> Double,
    onCashboxClick: (String) -> Unit,
    onAddCashboxClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.testTag("cashboxes_row"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
        items(cashboxes) { cb ->
            val bal = balanceProvider(cb.id)
            val parsedColor = try {
                Color(android.graphics.Color.parseColor(cb.color))
            } catch (e: Exception) {
                ColorBrass
            }
            Card(
                modifier = Modifier
                    .width(155.dp)
                    .clickable { onCashboxClick(cb.id) }
                    .testTag("cashbox_card_${cb.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(parsedColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cb.icon, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = cb.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = I18n.fmtAmount(bal, cb.currency),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (bal < 0) ColorClay else MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .width(120.dp)
                    .height(105.dp)
                    .clickable { onAddCashboxClick() }
                    .testTag("add_cashbox_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add cashbox",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "New Box",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    cashbox: Cashbox?,
    toCashbox: Cashbox?,
    category: Category?,
    lang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == "income"
    val isExpense = transaction.type == "expense"
    val isTransfer = transaction.type == "transfer"

    val (icon, color) = when {
        isIncome -> (category?.icon ?: "➕") to (try { Color(android.graphics.Color.parseColor(category?.color ?: "#3F7D5C")) } catch (e: Exception) { ColorSage })
        isExpense -> (category?.icon ?: "➖") to (try { Color(android.graphics.Color.parseColor(category?.color ?: "#B33F3F")) } catch (e: Exception) { ColorClay })
        else -> "🔁" to ColorBrass
    }

    val desc = transaction.description.ifEmpty {
        if (isTransfer) I18n.t("transfer", lang)
        else category?.name ?: (category?.nameKey?.let { I18n.t(it, lang) } ?: transaction.type)
    }

    val meta = when {
        isTransfer -> "${cashbox?.name ?: ""} → ${toCashbox?.name ?: ""}"
        else -> listOfNotNull(
            category?.name ?: category?.nameKey?.let { I18n.t(it, lang) },
            cashbox?.name
        ).joinToString(" · ")
    }

    val currency = cashbox?.currency ?: "EUR"
    val sign = if (isIncome) "+" else if (isExpense) "-" else ""
    val amtColor = if (isIncome) ColorSage else if (isExpense) ColorClay else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag("transaction_row_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = desc,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = meta,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$sign${I18n.fmtAmount(transaction.amount, currency)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = SpaceMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = amtColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = I18n.fmtDateShort(transaction.date),
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}

@Composable
fun BudgetItemRow(
    budget: Budget,
    category: Category?,
    spent: Double,
    lang: String,
    modifier: Modifier = Modifier
) {
    val catName = category?.name ?: (category?.nameKey?.let { I18n.t(it, lang) } ?: "Category")
    val pct = if (budget.limit > 0) (spent / budget.limit).coerceIn(0.0, 1.0).toFloat() else 0f
    val isOver = spent >= budget.limit
    val isWarn = !isOver && pct >= 0.8f

    val barColor = when {
        isOver -> ColorClay
        isWarn -> ColorBrass
        else -> ColorSage
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("budget_item_${budget.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = category?.icon ?: "📁", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = catName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = "${I18n.fmtNum(spent)} / ${I18n.fmtNum(budget.limit)} ${I18n.CURRENCY_SYMBOLS[budget.currency] ?: budget.currency}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = SpaceMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

val EMOJI_PRESETS = listOf(
    "💼", "🎁", "➕", "🍽️", "🚗", "🧾", "🛍️", "💊", "🏠", "🎬",
    "➖", "☕", "✈️", "🎓", "🐾", "📱", "🎮", "👶", "💇", "🧴",
    "🚕", "⚡", "💧", "🎵", "📚", "🧰", "🌿", "🍺", "🎂", "💳",
    "💰", "🪙", "🏦", "📈", "🛡️"
)

val COLOR_PRESETS = listOf(
    "#14213D", "#C08A2E", "#3F7D5C", "#B33F3F",
    "#6B7280", "#2F6F8F", "#8A4FBE", "#C2554E"
)

@Composable
fun EmojiGridSelector(
    selectedEmoji: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Icon",
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(EMOJI_PRESETS) { emoji ->
                val isSel = emoji == selectedEmoji
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSel) ColorBrassBright.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isSel) 2.dp else 0.dp,
                            color = if (isSel) ColorBrass else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun ColorPaletteSelector(
    selectedHex: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            COLOR_PRESETS.forEach { hex ->
                val isSel = hex.equals(selectedHex, ignoreCase = true)
                val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { ColorBrass }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            width = if (isSel) 3.dp else 1.dp,
                            color = if (isSel) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelect(hex) }
                )
            }
        }
    }
}
