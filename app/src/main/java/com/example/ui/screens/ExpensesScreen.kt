package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ExpenseEntity
import com.example.ui.components.ExpenseCategories
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.util.Formatters
import com.example.ui.util.Localization

private fun extractExpenseInitials(title: String): String {
    val trimmed = title.trim()
    if (trimmed.isEmpty()) return "EX"
    val parts = trimmed.split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        parts[0].length >= 2 -> parts[0].take(2).uppercase()
        else -> parts[0].uppercase()
    }
}

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    onAddExpense: () -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedMode by remember { mutableStateOf("ALL") }

    val filteredExpenses = remember(expenses, searchQuery, selectedCategory, selectedMode) {
        expenses.filter { e ->
            val matchesCategory = selectedCategory == "ALL" || e.category == selectedCategory
            val matchesMode = when (selectedMode) {
                "CASH" -> e.paymentMode.equals("CASH", ignoreCase = true)
                "UPI" -> e.paymentMode.equals("UPI", ignoreCase = true)
                else -> true
            }
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    e.title.lowercase().contains(q) ||
                    e.paidTo.lowercase().contains(q) ||
                    e.category.lowercase().contains(q) ||
                    e.referenceId.lowercase().contains(q) ||
                    e.notes.lowercase().contains(q)

            matchesCategory && matchesMode && matchesQuery
        }
    }

    val totalFilteredAmount = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.canvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = colors.expenseRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = strings.addExpense)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.canvas)
                .testTag("expenses_screen")
        ) {
            // Search & Filters Header
            Surface(
                color = colors.cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(strings.searchExpensesPlaceholder, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = colors.textSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Filters & Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ALL" to "${strings.filterAll} (${expenses.size})", "CASH" to "💵 ${strings.cash}", "UPI" to "📱 ${strings.upi}").forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedMode == key,
                                    onClick = { selectedMode = key },
                                    label = { Text(label, fontSize = 11.5.sp) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primaryContainer,
                                        selectedLabelColor = colors.onPrimaryContainer,
                                        containerColor = colors.cardBg,
                                        labelColor = colors.textSecondary
                                    )
                                )
                            }
                        }

                        Text(
                            text = Formatters.formatCurrency(totalFilteredAmount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.expenseRed
                        )
                    }

                    // Category horizontal scroll chips
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == "ALL",
                            onClick = { selectedCategory = "ALL" },
                            label = { Text(strings.allCategories, fontSize = 11.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.onPrimaryContainer,
                                containerColor = colors.cardBg,
                                labelColor = colors.textSecondary
                            )
                        )

                        ExpenseCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryContainer,
                                    selectedLabelColor = colors.onPrimaryContainer,
                                    containerColor = colors.cardBg,
                                    labelColor = colors.textSecondary
                                )
                            )
                        }
                    }
                }
            }

            Divider(color = colors.divider, thickness = 0.8.dp)

            // Expense List
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🧾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) strings.noExpensesFound else strings.noExpenses,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "नीचे दिए गए '+' बटन से नया खर्च जोड़ें",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEdit = { onEditExpense(expense) },
                            onDelete = { onDeleteExpense(expense) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(
    expense: ExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    val initials = extractExpenseInitials(expense.title)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_card_${expense.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Title + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.expenseRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.expenseRed
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = expense.title,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = Formatters.formatDate(expense.dateMillis),
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Text(
                    text = "-${Formatters.formatCurrency(expense.amount)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.expenseRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = colors.divider, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Details: Category, Paid to, Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.primaryContainer
                    ) {
                        Text(
                            text = expense.category,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Payment Mode Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (expense.paymentMode == "UPI") colors.upiBlueLight else colors.cashAmberLight
                    ) {
                        Text(
                            text = if (expense.paymentMode == "UPI") "📱 UPI" else "💵 CASH",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (expense.paymentMode == "UPI") colors.upiBlue else colors.cashAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (expense.paidTo.isNotBlank()) {
                    Text(
                        text = "${strings.paidTo}: ${expense.paidTo}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }

            // Ref & Notes
            if (expense.referenceId.isNotBlank() || expense.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (expense.referenceId.isNotBlank()) {
                        Text(
                            text = "बिल / Txn: ${expense.referenceId}",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (expense.notes.isNotBlank()) {
                        Text(
                            text = "नोट: ${expense.notes}",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Row: Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = strings.edit, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = strings.delete, tint = colors.expenseRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

