package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BookingEntity
import com.example.data.entity.KirtanEntity
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.util.BookingCategory
import com.example.ui.util.Formatters
import com.example.ui.util.Localization
import com.example.ui.viewmodel.BookingSummary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    currentKirtan: KirtanEntity?,
    bookings: List<BookingEntity>,
    bookingSummary: BookingSummary,
    onBack: () -> Unit,
    onAddBooking: (categoryId: String) -> Unit,
    onEditBooking: (BookingEntity) -> Unit,
    onDeleteBooking: (BookingEntity) -> Unit,
    onShareReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)
    val context = LocalContext.current

    var selectedCategoryId by remember { mutableStateOf<String?>("ALL") }

    val filteredBookings = remember(bookings, selectedCategoryId) {
        if (selectedCategoryId == null || selectedCategoryId == "ALL") {
            bookings
        } else {
            bookings.filter { it.categoryId.equals(selectedCategoryId, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas)
            .testTag("booking_screen"),
        containerColor = colors.canvas,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddBooking(selectedCategoryId?.takeIf { it != "ALL" } ?: BookingCategory.GARDEN_HALL.id) },
                containerColor = colors.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("booking_fab_add")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = strings.addBookingBtn)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar with Back Button & Share
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("booking_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = strings.bookingMenuTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = currentKirtan?.name ?: strings.allKirtans,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Share button
                    Surface(
                        onClick = onShareReport,
                        shape = RoundedCornerShape(14.dp),
                        color = colors.primaryContainer,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("booking_share_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Booking Budget Summary Card
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
                            Column {
                                Text(
                                    text = strings.totalBookingCommitment.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Formatters.formatCurrency(bookingSummary.totalAmount),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.primaryContainer
                            ) {
                                Text(
                                    text = "${bookingSummary.totalBookings} सेवाएं बुक",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advance vs Balance Due Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Advance Paid
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.donationGreen.copy(alpha = 0.1f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = strings.totalAdvancePaidTitle,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.donationGreen
                                )
                                Text(
                                    text = Formatters.formatCurrency(bookingSummary.totalAdvancePaid),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.donationGreen
                                )
                            }

                            // Balance Due
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.expenseRed.copy(alpha = 0.1f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = strings.totalBalanceRemainingTitle,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.expenseRed
                                )
                                Text(
                                    text = Formatters.formatCurrency(bookingSummary.totalBalanceRemaining),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.expenseRed
                                )
                            }
                        }
                    }
                }
            }

            // Section: All 13 Categories Selector
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "बुकिंग श्रेणियां (Booking Services Menu)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "कुल 13 सेवाएं",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal scrolling pills for quick filter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All" pill
                        val isAllSelected = selectedCategoryId == "ALL"
                        Surface(
                            onClick = { selectedCategoryId = "ALL" },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isAllSelected) colors.primary else colors.cardBg,
                            border = BorderStroke(1.dp, if (isAllSelected) colors.primary else colors.cardBorder),
                            modifier = Modifier.testTag("filter_category_all")
                        ) {
                            Text(
                                text = "सभी (All) • ${bookings.size}",
                                fontSize = 12.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllSelected) Color.White else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }

                        // 13 Categories
                        BookingCategory.ALL_CATEGORIES.forEach { category ->
                            val isSelected = selectedCategoryId == category.id
                            val count = bookingSummary.categoryCounts[category.id] ?: 0
                            Surface(
                                onClick = { selectedCategoryId = category.id },
                                shape = RoundedCornerShape(18.dp),
                                color = if (isSelected) colors.primary else colors.cardBg,
                                border = BorderStroke(1.dp, if (isSelected) colors.primary else colors.cardBorder),
                                modifier = Modifier.testTag("filter_category_${category.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = category.icon, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = category.englishTitle,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else colors.textPrimary
                                    )
                                    if (count > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) Color.White.copy(alpha = 0.3f) else colors.primaryContainer
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else colors.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Add Button for Active Category
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeCat = if (selectedCategoryId != null && selectedCategoryId != "ALL") {
                        BookingCategory.fromId(selectedCategoryId!!)
                    } else null

                    Text(
                        text = if (activeCat != null) "${activeCat.icon} ${activeCat.getDisplayName(language)}" else "दर्ज बुकिंग सूची (${filteredBookings.size})",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Button(
                        onClick = { onAddBooking(selectedCategoryId?.takeIf { it != "ALL" } ?: BookingCategory.GARDEN_HALL.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("booking_add_entry_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = strings.addBookingBtn, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bookings List or Empty State
            if (filteredBookings.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = selectedCategoryId?.let { BookingCategory.fromId(it).icon } ?: "🎪",
                                fontSize = 40.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.noBookingsCategory,
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { onAddBooking(selectedCategoryId?.takeIf { it != "ALL" } ?: BookingCategory.GARDEN_HALL.id) },
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, colors.primary)
                            ) {
                                Text(
                                    text = "+ नई बुकिंग दर्ज करें",
                                    fontSize = 12.sp,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredBookings, key = { it.id }) { booking ->
                    BookingItemCard(
                        booking = booking,
                        onEdit = { onEditBooking(booking) },
                        onDelete = { onDeleteBooking(booking) },
                        onCall = {
                            if (booking.contactNumber.isNotBlank()) {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.contactNumber.trim()}"))
                                context.startActivity(dialIntent)
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun BookingItemCard(
    booking: BookingEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)
    val category = BookingCategory.fromId(booking.categoryId)
    val balanceDue = (booking.totalAmount - booking.advancePaid).coerceAtLeast(0.0)

    val (statusLabel, statusBg, statusText) = when (booking.status) {
        "CONFIRMED" -> Triple(strings.statusConfirmed, colors.donationGreen.copy(alpha = 0.15f), colors.donationGreen)
        "ADVANCE_PAID" -> Triple(strings.statusAdvancePaid, colors.primaryContainer, colors.onPrimaryContainer)
        "COMPLETED" -> Triple(strings.statusCompleted, Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF7C3AED))
        else -> Triple(strings.statusPending, colors.expenseRed.copy(alpha = 0.15f), colors.expenseRed)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("booking_card_${booking.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Category Badge & Status Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = category.englishTitle,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vendor Name & Contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.vendorName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (booking.contactNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📞 ${booking.contactNumber}",
                            fontSize = 12.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (booking.contactNumber.isNotBlank()) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer)
                            .testTag("call_vendor_${booking.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Vendor",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Breakdown: Total / Advance / Balance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primaryContainer.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "कुल राशि (Total)",
                        fontSize = 9.5.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = Formatters.formatCurrency(booking.totalAmount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Column {
                    Text(
                        text = "एडवांस (Advance)",
                        fontSize = 9.5.sp,
                        color = colors.donationGreen
                    )
                    Text(
                        text = Formatters.formatCurrency(booking.advancePaid),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.donationGreen
                    )
                }

                Column {
                    Text(
                        text = "बकाया (Due)",
                        fontSize = 9.5.sp,
                        color = if (balanceDue > 0) colors.expenseRed else colors.textSecondary
                    )
                    Text(
                        text = Formatters.formatCurrency(balanceDue),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (balanceDue > 0) colors.expenseRed else colors.donationGreen
                    )
                }
            }

            // Notes if any
            if (booking.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${booking.notes}",
                    fontSize = 11.5.sp,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Edit and Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("edit_booking_${booking.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Booking",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("delete_booking_${booking.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Booking",
                        tint = colors.expenseRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
