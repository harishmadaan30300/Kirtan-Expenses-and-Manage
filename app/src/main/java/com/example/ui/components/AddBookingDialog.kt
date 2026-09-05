package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.BookingEntity
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.util.BookingCategory
import com.example.ui.util.Formatters
import com.example.ui.util.Localization

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddBookingDialog(
    initialBooking: BookingEntity? = null,
    defaultCategoryId: String = BookingCategory.GARDEN_HALL.id,
    onDismiss: () -> Unit,
    onConfirm: (
        categoryId: String,
        serviceTitle: String,
        vendorName: String,
        contactNumber: String,
        eventDateMillis: Long,
        totalAmount: Double,
        advancePaid: Double,
        status: String,
        notes: String
    ) -> Unit
) {
    val colors = LocalAppColors.current
    val language = LocalAppLanguage.current
    val strings = Localization.get(language)

    var selectedCategory by remember {
        mutableStateOf(
            if (initialBooking != null) BookingCategory.fromId(initialBooking.categoryId)
            else BookingCategory.fromId(defaultCategoryId)
        )
    }

    var vendorName by remember { mutableStateOf(initialBooking?.vendorName ?: "") }
    var contactNumber by remember { mutableStateOf(initialBooking?.contactNumber ?: "") }
    var totalAmountStr by remember {
        mutableStateOf(
            if (initialBooking != null && initialBooking.totalAmount > 0)
                if (initialBooking.totalAmount % 1.0 == 0.0) initialBooking.totalAmount.toLong().toString()
                else initialBooking.totalAmount.toString()
            else ""
        )
    }
    var advancePaidStr by remember {
        mutableStateOf(
            if (initialBooking != null && initialBooking.advancePaid > 0)
                if (initialBooking.advancePaid % 1.0 == 0.0) initialBooking.advancePaid.toLong().toString()
                else initialBooking.advancePaid.toString()
            else ""
        )
    }
    var status by remember { mutableStateOf(initialBooking?.status ?: "CONFIRMED") }
    var notes by remember { mutableStateOf(initialBooking?.notes ?: "") }

    var vendorNameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0
    val advancePaid = advancePaidStr.toDoubleOrNull() ?: 0.0
    val balanceDue = (totalAmount - advancePaid).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp)
            .testTag("add_booking_dialog"),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (initialBooking == null) strings.addBookingTitle else strings.editBookingTitle,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${selectedCategory.icon} ${selectedCategory.getDisplayName(language)}",
                            fontSize = 12.sp,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection Section (All 13 Requested Categories)
                Text(
                    text = "सेवा श्रेणी चुनें (Select Service Category)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BookingCategory.ALL_CATEGORIES.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) colors.primaryContainer else colors.cardBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primary else colors.cardBorder
                            ),
                            modifier = Modifier.testTag("category_chip_${category.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = category.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = category.englishTitle,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.onPrimaryContainer else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vendor / Service Provider Name
                OutlinedTextField(
                    value = vendorName,
                    onValueChange = {
                        vendorName = it
                        vendorNameError = false
                    },
                    label = { Text(strings.vendorNameLabel) },
                    placeholder = { Text(strings.vendorNamePlaceholder) },
                    isError = vendorNameError,
                    supportingText = if (vendorNameError) {
                        { Text(strings.vendorNameError, color = colors.expenseRed) }
                    } else null,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = colors.primary)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_vendor_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Contact Number
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text(strings.contactNumberLabel) },
                    placeholder = { Text(strings.contactNumberPlaceholder) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = colors.primary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_contact_number_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Amounts: Total & Advance Paid (2 Column)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalAmountStr,
                        onValueChange = {
                            totalAmountStr = it
                            amountError = false
                        },
                        label = { Text(strings.totalAmountLabel) },
                        placeholder = { Text("0") },
                        isError = amountError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedContainerColor = colors.cardBg,
                            unfocusedContainerColor = colors.cardBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("booking_total_amount_input")
                    )

                    OutlinedTextField(
                        value = advancePaidStr,
                        onValueChange = { advancePaidStr = it },
                        label = { Text(strings.advancePaidLabel) },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedContainerColor = colors.cardBg,
                            unfocusedContainerColor = colors.cardBg
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("booking_advance_paid_input")
                    )
                }

                // Balance Calculation Preview Card
                if (totalAmount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.balanceDueLabel}:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onPrimaryContainer
                            )
                            Text(
                                text = Formatters.formatCurrency(balanceDue),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (balanceDue > 0) colors.expenseRed else colors.donationGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Booking Status Selector
                Text(
                    text = strings.bookingStatusLabel,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val statuses = listOf(
                    "CONFIRMED" to strings.statusConfirmed,
                    "ADVANCE_PAID" to strings.statusAdvancePaid,
                    "PENDING" to strings.statusPending,
                    "COMPLETED" to strings.statusCompleted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statuses.forEach { (code, label) ->
                        val isSelected = status == code
                        Surface(
                            onClick = { status = code },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) colors.primary else colors.cardBg,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primary else colors.cardBorder
                            ),
                            modifier = Modifier.testTag("status_chip_$code")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes / Details
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.bookingNotesLabel) },
                    placeholder = { Text("उदा. समय, माइक संख्या, विशेष सामग्री या शर्ते") },
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedContainerColor = colors.cardBg,
                        unfocusedContainerColor = colors.cardBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = vendorName.trim()
                    if (trimmedName.isEmpty()) {
                        vendorNameError = true
                        return@Button
                    }
                    onConfirm(
                        selectedCategory.id,
                        selectedCategory.englishTitle,
                        trimmedName,
                        contactNumber.trim(),
                        System.currentTimeMillis(),
                        totalAmount,
                        advancePaid,
                        status,
                        notes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_submit_btn")
            ) {
                Text(
                    text = if (initialBooking == null) strings.addBookingBtn else strings.saveBookingBtn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = null,
        shape = RoundedCornerShape(24.dp),
        containerColor = colors.cardBg
    )
}
