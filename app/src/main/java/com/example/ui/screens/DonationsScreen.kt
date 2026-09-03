package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DonationEntity
import com.example.ui.theme.AvatarAmberBg
import com.example.ui.theme.AvatarAmberText
import com.example.ui.theme.AvatarBlueBg
import com.example.ui.theme.AvatarBlueText
import com.example.ui.theme.CashAmber
import com.example.ui.theme.CashAmberLight
import com.example.ui.theme.DividerLight
import com.example.ui.theme.DonationGreen
import com.example.ui.theme.DonationGreenLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.MinimalCanvas
import com.example.ui.theme.PeaceCardBorder
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UpiBlue
import com.example.ui.theme.UpiBlueLight
import com.example.ui.util.Formatters
import com.example.ui.util.Localization

private fun extractDonorInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "DA"
    val parts = trimmed.split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        parts[0].length >= 2 -> parts[0].take(2).uppercase()
        else -> parts[0].uppercase()
    }
}

@Composable
fun DonationsScreen(
    donations: List<DonationEntity>,
    onAddDonation: () -> Unit,
    onViewReceipt: (DonationEntity) -> Unit,
    onEditDonation: (DonationEntity) -> Unit,
    onDeleteDonation: (DonationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "CASH", "UPI", "WITH_PHONE"
    val context = LocalContext.current

    val filteredDonations = remember(donations, searchQuery, selectedFilter) {
        val rawQuery = searchQuery.trim()
        val queryDigits = rawQuery.filter { it.isDigit() }

        donations.filter { d ->
            val matchesFilter = when (selectedFilter) {
                "CASH" -> d.paymentMode.equals("CASH", ignoreCase = true)
                "UPI" -> d.paymentMode.equals("UPI", ignoreCase = true)
                "RECEIVED" -> d.isPaymentReceived
                "PENDING" -> !d.isPaymentReceived
                "WITH_PHONE" -> d.mobileNumber.isNotBlank()
                else -> true
            }

            if (rawQuery.isEmpty()) {
                matchesFilter
            } else {
                // Filter donors by name (case-insensitive)
                val matchesName = d.donorName.contains(rawQuery, ignoreCase = true)
                
                // Filter donors by mobile number (both exact substring and normalized digit match)
                val donorDigits = d.mobileNumber.filter { it.isDigit() }
                val matchesMobile = if (queryDigits.isNotEmpty()) {
                    donorDigits.contains(queryDigits) || d.mobileNumber.contains(rawQuery, ignoreCase = true)
                } else {
                    d.mobileNumber.contains(rawQuery, ignoreCase = true)
                }
                
                // Also match receipt number or notes for ease of finding
                val matchesRef = d.referenceId.contains(rawQuery, ignoreCase = true)
                val matchesNotes = d.notes.contains(rawQuery, ignoreCase = true)

                matchesFilter && (matchesName || matchesMobile || matchesRef || matchesNotes)
            }
        }
    }

    val totalFilteredAmount = remember(filteredDonations) {
        filteredDonations.sumOf { it.amount }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.canvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDonation,
                containerColor = colors.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_donation_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = strings.addDonation)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.canvas)
                .testTag("donations_screen")
        ) {
            // Search & Filter Header
            Surface(
                color = colors.cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Title Bar: Donation History Log Header & Total count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.donationHistoryTitle,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${filteredDonations.size} / ${donations.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Enhanced Search Bar for Donors (by Name or Mobile Number)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = strings.searchDonationsPlaceholder,
                                fontSize = 12.5.sp,
                                color = colors.textSecondary.copy(alpha = 0.75f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Donors",
                                tint = if (searchQuery.isNotEmpty()) colors.primary else colors.textSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.testTag("clear_donation_search_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = strings.clearSearch,
                                        tint = colors.textSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donation_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips & Total Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to "${strings.filterAll} (${donations.size})",
                                "RECEIVED" to "✓ ${strings.paymentReceived}",
                                "PENDING" to "⏳ ${strings.paymentNotReceived}",
                                "CASH" to strings.filterCash,
                                "UPI" to strings.filterUpi,
                                "WITH_PHONE" to strings.filterWithPhone
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedFilter == key,
                                    onClick = { selectedFilter = key },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primaryContainer,
                                        selectedLabelColor = colors.onPrimaryContainer,
                                        containerColor = colors.cardBg,
                                        labelColor = colors.textSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = Formatters.formatCurrency(totalFilteredAmount),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.donationGreen
                        )
                    }

                    // Active Search Feedback Indicator
                    if (searchQuery.isNotBlank() || selectedFilter != "ALL") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = colors.primaryContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, colors.primary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Text("🔍 ", fontSize = 11.sp)
                                    Text(
                                        text = if (searchQuery.isNotBlank()) {
                                            "\"$searchQuery\": ${filteredDonations.size} दानदाता मिले"
                                        } else {
                                            "${filteredDonations.size} दानदाता मिले"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${strings.clearSearch} ✕",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            searchQuery = ""
                                            selectedFilter = "ALL"
                                        }
                                        .padding(4.dp)
                                        .testTag("reset_active_search_tag")
                                )
                            }
                        }
                    }
                }
            }

            Divider(color = colors.divider, thickness = 0.8.dp)

            // Donations List
            if (filteredDonations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (searchQuery.isNotEmpty()) "🔍" else "🪔", fontSize = 46.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "कोई दानदाता नहीं मिला" else strings.noDonations,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "'$searchQuery' से मिलता कोई दानदाता या मोबाइल नंबर नहीं है।"
                            } else {
                                "नीचे दिए गए '+' बटन से नया दान जोड़ें"
                            },
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (searchQuery.isNotEmpty() || selectedFilter != "ALL") {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedFilter = "ALL"
                                },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.primary
                                ),
                                border = BorderStroke(1.dp, colors.primary),
                                modifier = Modifier.testTag("empty_state_clear_search_btn")
                            ) {
                                Text(
                                    text = "सभी दानदाता दिखाएँ (${strings.clearSearch})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredDonations, key = { it.id }) { donation ->
                        DonationCard(
                            donation = donation,
                            onViewReceipt = { onViewReceipt(donation) },
                            onEdit = { onEditDonation(donation) },
                            onDelete = { onDeleteDonation(donation) },
                            onCall = {
                                if (donation.mobileNumber.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${donation.mobileNumber}")
                                    }
                                    context.startActivity(intent)
                                }
                            }
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
fun DonationCard(
    donation: DonationEntity,
    onViewReceipt: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    val isUpi = donation.paymentMode == "UPI"
    val avatarBg = if (isUpi) colors.upiBlueLight else colors.cashAmberLight
    val avatarText = if (isUpi) colors.upiBlue else colors.cashAmber
    val initials = extractDonorInitials(donation.donorName)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(1.dp, colors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() }
            .testTag("donation_card_${donation.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Donor Name + Amount
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
                        Text(
                            text = donation.donorName,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = Formatters.formatDate(donation.dateMillis),
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Text(
                    text = "+${Formatters.formatCurrency(donation.amount)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.donationGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = colors.divider, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Details Row: Status Pill, Mode Badge, Ref, Mobile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Payment Status Pill (Tick Received / Pending)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (donation.isPaymentReceived) colors.donationGreenLight else colors.expenseRedLight
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Icon(
                                imageVector = if (donation.isPaymentReceived) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (donation.isPaymentReceived) colors.donationGreen else colors.expenseRed,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (donation.isPaymentReceived) "प्राप्त" else "अप्राप्त",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (donation.isPaymentReceived) colors.donationGreen else colors.expenseRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Payment Mode Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (donation.paymentMode == "UPI") colors.upiBlueLight else colors.cashAmberLight
                    ) {
                        Text(
                            text = if (donation.paymentMode == "UPI") "📱 UPI" else "💵 CASH",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (donation.paymentMode == "UPI") colors.upiBlue else colors.cashAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                        )
                    }

                    if (donation.referenceId.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "#${donation.referenceId}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }
                }

                // Mobile with call button if available
                if (donation.mobileNumber.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCall() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Donor",
                            tint = colors.donationGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = donation.mobileNumber,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Optional Notes or Received By
            if (donation.notes.isNotBlank() || donation.receivedBy.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (donation.notes.isNotBlank()) {
                        Text(
                            text = "संकल्प: ${donation.notes}",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (donation.receivedBy.isNotBlank()) {
                        Text(
                            text = "प्राप्तकर्ता: ${donation.receivedBy}",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Row: Slip, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clickable { onViewReceipt() }
                        .padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = strings.viewReceipt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.onPrimaryContainer)
                    }
                }

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

