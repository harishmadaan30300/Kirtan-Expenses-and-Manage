package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import com.example.ui.components.shareText
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.util.Formatters
import com.example.ui.util.Localization
import com.example.ui.viewmodel.LogItem
import com.example.ui.viewmodel.LogType

private fun extractLogInitials(title: String, default: String): String {
    val trimmed = title.trim()
    if (trimmed.isEmpty()) return default
    val parts = trimmed.split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        parts[0].length >= 2 -> parts[0].take(2).uppercase()
        else -> parts[0].uppercase()
    }
}

@Composable
fun HistoryAndEventsScreen(
    logs: List<LogItem>,
    kirtans: List<KirtanEntity>,
    selectedKirtanId: Long?,
    onSelectKirtan: (Long?) -> Unit,
    onAddNewKirtan: () -> Unit,
    onEditKirtan: (KirtanEntity) -> Unit,
    onDeleteKirtan: (KirtanEntity) -> Unit,
    onToggleKirtanStatus: (KirtanEntity) -> Unit,
    onViewDonationReceipt: (DonationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    var selectedTab by remember { mutableIntStateOf(0) }
    var logSearchQuery by remember { mutableStateOf("") }
    var logTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "DONATION", "EXPENSE", "CASH", "UPI"
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .testTag("history_and_events_screen")
    ) {
        // Tab Selector Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.cardBg,
            contentColor = colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.primary,
                    height = 2.5.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.tabLogs, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("tab_logs")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${strings.tabKirtans} (${kirtans.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                modifier = Modifier.testTag("tab_kirtans")
            )
        }

        Divider(color = colors.divider, thickness = 0.8.dp)

        // TAB 1: HISTORY & AUDIT LOGS
        if (selectedTab == 0) {
            val filteredLogs = remember(logs, logSearchQuery, logTypeFilter) {
                logs.filter { item ->
                    val matchesFilter = when (logTypeFilter) {
                        "DONATION" -> item.type == LogType.DONATION
                        "EXPENSE" -> item.type == LogType.EXPENSE
                        "CASH" -> item.paymentMode.equals("CASH", ignoreCase = true)
                        "UPI" -> item.paymentMode.equals("UPI", ignoreCase = true)
                        else -> true
                    }
                    val rawQ = logSearchQuery.trim()
                    val q = rawQ.lowercase()
                    val queryDigits = rawQ.filter { it.isDigit() }
                    val donorDigits = item.rawDonation?.mobileNumber?.filter { it.isDigit() } ?: ""
                    val matchesDonorMobile = if (queryDigits.isNotEmpty()) {
                        donorDigits.contains(queryDigits) || (item.rawDonation?.mobileNumber?.contains(rawQ, ignoreCase = true) == true)
                    } else {
                        item.rawDonation?.mobileNumber?.contains(rawQ, ignoreCase = true) == true
                    }

                    val matchesQuery = rawQ.isEmpty() ||
                            item.title.lowercase().contains(q) ||
                            item.subtitle.lowercase().contains(q) ||
                            item.referenceId.lowercase().contains(q) ||
                            item.notes.lowercase().contains(q) ||
                            item.kirtanName.lowercase().contains(q) ||
                            matchesDonorMobile

                    matchesFilter && matchesQuery
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Search and Filters
                Surface(
                    color = colors.cardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        OutlinedTextField(
                            value = logSearchQuery,
                            onValueChange = { logSearchQuery = it },
                            placeholder = { Text("${strings.searchLogsPlaceholder}...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = colors.textSecondary)
                            },
                            trailingIcon = {
                                if (logSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { logSearchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("log_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to "${strings.filterAll} (${logs.size})",
                                "DONATION" to "🌸 ${strings.tabDonations}",
                                "EXPENSE" to "💸 ${strings.tabExpenses}",
                                "CASH" to "💵 ${strings.cash}",
                                "UPI" to "📱 ${strings.upi}"
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = logTypeFilter == key,
                                    onClick = { logTypeFilter = key },
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
                    }
                }

                Divider(color = colors.divider, thickness = 0.8.dp)

                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📜", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.noLogsFound,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.noLogsFound,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredLogs, key = { "${it.type}_${it.id}" }) { logItem ->
                            LogItemCard(
                                logItem = logItem,
                                onClick = {
                                    if (logItem.type == LogType.DONATION && logItem.rawDonation != null) {
                                        onViewDonationReceipt(logItem.rawDonation)
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // TAB 2: KIRTANS MANAGER
        if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("kirtans_manager_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Action Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "एकाधिक कीर्तन प्रबंधन",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onPrimaryContainer
                                )
                                Text(
                                    text = "प्रत्येक कीर्तन का अलग-अलग चंदा व खर्च रिकॉर्ड रखें",
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary
                                )
                            }

                            Button(
                                onClick = onAddNewKirtan,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("add_kirtan_tab_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = strings.addNewKirtan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // All Kirtans Aggregate Option
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedKirtanId == null) colors.primaryContainer.copy(alpha = 0.5f) else colors.cardBg
                        ),
                        border = BorderStroke(
                            width = if (selectedKirtanId == null) 1.5.dp else 1.dp,
                            color = if (selectedKirtanId == null) colors.primary else colors.cardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectKirtan(null) }
                            .testTag("select_all_kirtans_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🌐", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.allKirtans,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "सभी कीर्तनों का कुल दान एवं खर्च एक साथ देखें",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            if (selectedKirtanId == null) {
                                Surface(
                                    shape = CircleShape,
                                    color = colors.primary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "आयोजित कीर्तन सूचि (${kirtans.size}):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // List of Individual Kirtans
                items(kirtans, key = { it.id }) { kirtan ->
                    val isSelected = selectedKirtanId == kirtan.id
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) colors.primaryContainer.copy(alpha = 0.4f) else colors.cardBg
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) colors.primary else colors.cardBorder
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectKirtan(kirtan.id) }
                            .testTag("kirtan_item_${kirtan.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "🌺", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = kirtan.name,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = Formatters.formatDateOnly(kirtan.dateMillis),
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (kirtan.isCompleted) colors.primaryContainer else colors.donationGreenLight,
                                    modifier = Modifier.clickable { onToggleKirtanStatus(kirtan) }
                                ) {
                                    Text(
                                        text = if (kirtan.isCompleted) strings.statusCompleted else strings.statusActive,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (kirtan.isCompleted) colors.onPrimaryContainer else colors.donationGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (kirtan.organizer.isNotBlank() || kirtan.location.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                if (kirtan.organizer.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.People, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = kirtan.organizer, fontSize = 11.5.sp, color = colors.textSecondary)
                                    }
                                }
                                if (kirtan.location.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = kirtan.location, fontSize = 11.5.sp, color = colors.textSecondary)
                                    }
                                }
                            }

                            if (kirtan.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "नोट: ${kirtan.notes}", fontSize = 11.sp, color = colors.textSecondary)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = colors.divider, thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Bottom actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isSelected) strings.currentlySelected else strings.tapToSelect,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.primary else colors.textSecondary
                                )

                                Row {
                                    IconButton(onClick = { onEditKirtan(kirtan) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = strings.edit, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { onDeleteKirtan(kirtan) }, modifier = Modifier.size(32.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = strings.delete, tint = colors.expenseRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LogItemCard(
    logItem: LogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    val isDonation = logItem.type == LogType.DONATION
    val avatarBg = if (isDonation) colors.donationGreenLight else colors.expenseRedLight
    val avatarText = if (isDonation) colors.donationGreen else colors.expenseRed
    val initials = extractLogInitials(logItem.title, if (isDonation) "DA" else "EX")

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("log_item_${logItem.type}_${logItem.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Initials avatar badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = avatarText
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = logItem.title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (logItem.paymentMode == "UPI") colors.upiBlueLight else colors.cashAmberLight
                        ) {
                            Text(
                                text = logItem.paymentMode,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (logItem.paymentMode == "UPI") colors.upiBlue else colors.cashAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${Formatters.formatDate(logItem.dateMillis)} • ${logItem.subtitle}",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )

                    if (logItem.notes.isNotBlank()) {
                        Text(
                            text = "नोट: ${logItem.notes}",
                            fontSize = 10.5.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isDonation) "+" else "-"}${Formatters.formatCurrency(logItem.amount)}",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDonation) colors.donationGreen else colors.expenseRed
                )

                Text(
                    text = if (isDonation) "दान (${strings.cash}/${strings.upi})" else "खर्च",
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

