package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BookingEntity
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import com.example.ui.theme.AvatarAmberBg
import com.example.ui.theme.AvatarAmberText
import com.example.ui.theme.AvatarBlueBg
import com.example.ui.theme.AvatarBlueText
import com.example.ui.theme.AvatarRedBg
import com.example.ui.theme.AvatarRedText
import com.example.ui.theme.CashAmber
import com.example.ui.theme.CashAmberLight
import com.example.ui.theme.DevotionalGold
import com.example.ui.theme.DevotionalGoldLight
import com.example.ui.theme.DividerLight
import com.example.ui.theme.DonationButtonBg
import com.example.ui.theme.DonationButtonText
import com.example.ui.theme.DonationGreen
import com.example.ui.theme.DonationGreenLight
import com.example.ui.theme.ExpenseButtonBg
import com.example.ui.theme.ExpenseButtonText
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
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
import com.example.ui.util.BookingCategory
import com.example.ui.util.Formatters
import com.example.ui.util.Localization
import com.example.ui.viewmodel.BookingSummary
import com.example.ui.viewmodel.DashboardSummary

private fun extractInitials(name: String, fallback: String = "KS"): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return fallback
    val parts = trimmed.split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        parts[0].length >= 2 -> parts[0].take(2).uppercase()
        else -> parts[0].uppercase()
    }
}

@Composable
fun DashboardScreen(
    currentKirtan: KirtanEntity?,
    summary: DashboardSummary,
    recentDonations: List<DonationEntity>,
    recentExpenses: List<ExpenseEntity>,
    bookings: List<BookingEntity> = emptyList(),
    bookingSummary: BookingSummary = BookingSummary(),
    onAddDonation: () -> Unit,
    onAddExpense: () -> Unit,
    onSelectDonation: (DonationEntity) -> Unit,
    onShareReport: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onOpenBookingMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Current Event Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentKirtan?.name ?: strings.allKirtans,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentKirtan != null) {
                                "${Formatters.formatDateOnly(currentKirtan.dateMillis)}${if (currentKirtan.location.isNotBlank()) " • ${currentKirtan.location}" else ""}"
                            } else {
                                strings.availableFunds
                            },
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.primaryContainer,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (currentKirtan?.isCompleted == true) strings.kirtanStatusCompleted else strings.kirtanStatusActive,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Net Balance Hero Card (Clean Minimalism design matching prompt)
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = strings.netBalance,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Formatters.formatCurrency(summary.netBalance),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = colors.cardBg.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = if (summary.netBalance >= 0) "LIVE" else "DEFICIT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Two Sub-cards inside Hero: Donations vs Expenses
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Donations pill card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.cardBg.copy(alpha = if (colors.isDark) 0.6f else 0.7f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = strings.totalDonations.uppercase(),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer.copy(alpha = 0.75f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+${Formatters.formatCurrency(summary.totalDonations)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.donationGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💵 ${Formatters.formatCurrency(summary.cashDonations)} • 📱 ${Formatters.formatCurrency(summary.upiDonations)}",
                                fontSize = 9.5.sp,
                                color = colors.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        // Expenses pill card
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.cardBg.copy(alpha = if (colors.isDark) 0.6f else 0.7f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = strings.totalExpenses.uppercase(),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer.copy(alpha = 0.75f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "-${Formatters.formatCurrency(summary.totalExpenses)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.expenseRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💵 ${Formatters.formatCurrency(summary.cashExpenses)} • 📱 ${Formatters.formatCurrency(summary.upiExpenses)}",
                                fontSize = 9.5.sp,
                                color = colors.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Two Quick Action Buttons (Grid styled matching Clean Minimalism design)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // New Donation Button
                Surface(
                    onClick = onAddDonation,
                    shape = RoundedCornerShape(24.dp),
                    color = colors.donationBtnBg,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .testTag("dashboard_add_donation_btn")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "+",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.donationBtnText
                        )
                        Text(
                            text = strings.addDonation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.donationBtnText
                        )
                    }
                }

                // Add Expense Button
                Surface(
                    onClick = onAddExpense,
                    shape = RoundedCornerShape(24.dp),
                    color = colors.expenseBtnBg,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .testTag("dashboard_add_expense_btn")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "−",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.expenseBtnText
                        )
                        Text(
                            text = strings.addExpense,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.expenseBtnText
                        )
                    }
                }
            }
        }

        // Booking Option Section (Home page par Booking naam se option with 13 services)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                border = BorderStroke(1.2.dp, colors.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenBookingMenu() }
                    .testTag("home_booking_option_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Row with Icon, Title, and Action button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎪", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = strings.bookingOption,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = colors.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "13 Services",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = strings.bookingSubtitle,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            onClick = onOpenBookingMenu,
                            shape = RoundedCornerShape(14.dp),
                            color = colors.primary,
                            modifier = Modifier.testTag("home_open_booking_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.openBookingMenu,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Booking Stats Bar if bookings exist
                    if (bookingSummary.totalBookings > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.primaryContainer.copy(alpha = 0.4f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 ${bookingSummary.totalBookings} सेवाएं बुक",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryContainer
                            )
                            Text(
                                text = "कुल: ${Formatters.formatCurrency(bookingSummary.totalAmount)} | एडवांस: ${Formatters.formatCurrency(bookingSummary.totalAdvancePaid)}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 13 Services Icons Preview Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BookingCategory.ALL_CATEGORIES.forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.canvas,
                                border = BorderStroke(0.8.dp, colors.cardBorder),
                                modifier = Modifier.clickable { onOpenBookingMenu() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = category.icon, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = category.englishTitle,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transparency Summary Banner (WhatsApp Share)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📢", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.shareReport,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "WhatsApp / Social Share",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Button(
                        onClick = onShareReport,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("dashboard_share_report_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Share", fontSize = 11.5.sp)
                    }
                }
            }
        }

        // Expense Category Breakdown Section
        if (summary.categoryExpenses.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.categoryBreakdown.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${summary.categoryExpenses.size} ${strings.entries}",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val totalExp = if (summary.totalExpenses > 0) summary.totalExpenses else 1.0
                        summary.categoryExpenses.entries.sortedByDescending { it.value }.forEach { entry ->
                            val progress = (entry.value / totalExp).toFloat()
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = entry.key,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${Formatters.formatCurrency(entry.value)} (${(progress * 100).toInt()}%)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.expenseRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = colors.primary,
                                    trackColor = colors.divider
                                )
                            }
                        }
                    }
                }
            }
        }

        // History Log Section (Clean Minimalism container matching Design HTML)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.tabHistory.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${strings.viewAll.uppercase()} (${summary.donorCount + summary.expenseCount})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.clickable { onNavigateToDonations() }
                )
            }
        }

        // Single Unified Minimal History Log Card
        item {
            val combinedHistoryEmpty = recentDonations.isEmpty() && recentExpenses.isEmpty()
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (combinedHistoryEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${strings.noDonations}\n'+ ${strings.addDonation}' / '${strings.addExpense}'",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Show recent donations
                        recentDonations.take(3).forEachIndexed { index, donation ->
                            val initials = extractInitials(donation.donorName, "DA")
                            val isUpi = donation.paymentMode == "UPI"
                            val avatarBg = if (isUpi) colors.upiBlueLight else colors.cashAmberLight
                            val avatarText = if (isUpi) colors.upiBlue else colors.cashAmber

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectDonation(donation) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("recent_donation_item_${donation.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
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
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${if (donation.mobileNumber.isNotBlank()) "${donation.mobileNumber} • " else ""}${if (donation.isPaymentReceived) "✓ प्राप्त" else "⏳ अप्राप्त"} • ${donation.paymentMode} • ${Formatters.formatDateOnly(donation.dateMillis)}",
                                            fontSize = 10.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "+${Formatters.formatCurrency(donation.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.donationGreen
                                    )
                                    Text(
                                        text = Formatters.formatTimeOnly(donation.dateMillis),
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Divider(color = colors.divider, thickness = 0.8.dp)
                        }

                        // Show recent expenses
                        recentExpenses.take(3).forEachIndexed { index, expense ->
                            val initials = extractInitials(expense.title, "EX")

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
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
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${expense.category}${if (expense.paidTo.isNotBlank()) " • ${expense.paidTo}" else ""} • ${expense.paymentMode}",
                                            fontSize = 10.5.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "-${Formatters.formatCurrency(expense.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.expenseRed
                                    )
                                    Text(
                                        text = Formatters.formatTimeOnly(expense.dateMillis),
                                        fontSize = 10.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            if (index < recentExpenses.take(3).size - 1) {
                                Divider(color = colors.divider, thickness = 0.8.dp)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

