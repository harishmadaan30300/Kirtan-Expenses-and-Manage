package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import com.example.ui.theme.CashAmber
import com.example.ui.theme.CashAmberLight
import com.example.ui.theme.DevotionalGold
import com.example.ui.theme.DevotionalGoldLight
import com.example.ui.theme.DonationGreen
import com.example.ui.theme.DonationGreenLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.PeaceCardBorder
import com.example.ui.theme.PeaceIvory
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronDark
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UpiBlue
import com.example.ui.theme.UpiBlueLight
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.DevotionalPalette
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.ThemeMode
import com.example.ui.util.Formatters
import com.example.ui.util.Localization
import com.example.ui.util.LocalBackupInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Predefined Devotional Categories
val ExpenseCategories = listOf(
    "Sound & Audio",
    "Tent & Stage",
    "Flowers & Decoration",
    "Prasad & Bhog",
    "Dakshina / Artists",
    "Puja Samagri",
    "Light & Electric",
    "Transport",
    "Miscellaneous"
)

// Helper to share text
fun shareText(context: Context, title: String, message: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}

// -----------------------------------------------------------------------------
// ADD / EDIT DONATION DIALOG
// -----------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddDonationDialog(
    initialDonation: DonationEntity? = null,
    kirtans: List<KirtanEntity>,
    selectedKirtanId: Long?,
    onDismiss: () -> Unit,
    onSave: (
        donorName: String,
        amount: Double,
        mobileNumber: String,
        paymentMode: String,
        referenceId: String,
        notes: String,
        receivedBy: String,
        kirtanId: Long,
        isPaymentReceived: Boolean
    ) -> Unit
) {
    var donorName by remember { mutableStateOf(initialDonation?.donorName ?: "") }
    var amountText by remember {
        mutableStateOf(if (initialDonation != null) initialDonation.amount.toInt().toString() else "")
    }
    var mobileNumber by remember { mutableStateOf(initialDonation?.mobileNumber ?: "") }
    var paymentMode by remember { mutableStateOf(initialDonation?.paymentMode ?: "CASH") }
    var referenceId by remember {
        mutableStateOf(
            initialDonation?.referenceId
                ?: "REC-${(System.currentTimeMillis() % 90000 + 10000)}"
        )
    }
    var isPaymentReceived by remember {
        mutableStateOf(initialDonation?.isPaymentReceived ?: true)
    }
    var notes by remember { mutableStateOf(initialDonation?.notes ?: "") }
    var receivedBy by remember { mutableStateOf(initialDonation?.receivedBy ?: "") }
    var targetKirtanId by remember {
        mutableStateOf(
            initialDonation?.kirtanId
                ?: selectedKirtanId
                ?: kirtans.firstOrNull()?.id
                ?: 0L
        )
    }

    var donorNameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_donation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialDonation == null) "दान रसीद बनाएं (Make Receipt)" else "दान विवरण संपादित करें",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "Enter Donation Information & Receipt Details",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kirtan selection if multiple
                if (kirtans.size > 1 && initialDonation == null) {
                    Text(
                        text = "कीर्तन चयन करें:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        kirtans.forEach { k ->
                            FilterChip(
                                selected = targetKirtanId == k.id,
                                onClick = { targetKirtanId = k.id },
                                label = { Text(k.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronContainer,
                                    selectedLabelColor = SaffronDark
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 1. RECEIPT NUMBER AT TOP OF MAKE RECEIPT
                OutlinedTextField(
                    value = referenceId,
                    onValueChange = { referenceId = it },
                    label = { Text("रसीद संख्या (Receipt Number / Slip No) *") },
                    placeholder = { Text("उदा. REC-101 / पर्ची संख्या") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Receipt Number",
                            tint = SaffronPrimary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donation_reference_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Donor Name Field
                OutlinedTextField(
                    value = donorName,
                    onValueChange = {
                        donorName = it
                        donorNameError = it.isBlank()
                    },
                    label = { Text("दानदाता का नाम (Donor Name) *") },
                    isError = donorNameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donor_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )
                if (donorNameError) {
                    Text(
                        text = "कृपया दानदाता का नाम दर्ज करें",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount Field (Quick suggestions 101, 251, 501, 1100, 2100 removed)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amountText = it
                            amountError = it.toDoubleOrNull() == null || it.toDouble() <= 0
                        }
                    },
                    label = { Text("राशि (Amount in ₹) *") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = DonationGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donation_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )
                if (amountError) {
                    Text(
                        text = "कृपया सही राशि दर्ज करें",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mobile Number Field
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { if (it.length <= 15) mobileNumber = it },
                    label = { Text("मोबाइल नंबर (Mobile Number)") },
                    placeholder = { Text("10 digit number") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = TextSecondary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donor_mobile_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Mode Selection (CASH / UPI)
                Text(
                    text = "भुगतान माध्यम (Payment Mode):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { paymentMode = "CASH" }
                            .border(
                                width = if (paymentMode == "CASH") 2.dp else 1.dp,
                                color = if (paymentMode == "CASH") CashAmber else PeaceCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("mode_cash_button"),
                        color = if (paymentMode == "CASH") SaffronContainer else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "💵 ", fontSize = 16.sp)
                            Text(
                                text = "CASH (नकद)",
                                fontWeight = if (paymentMode == "CASH") FontWeight.Bold else FontWeight.Normal,
                                color = if (paymentMode == "CASH") CashAmber else TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { paymentMode = "UPI" }
                            .border(
                                width = if (paymentMode == "UPI") 2.dp else 1.dp,
                                color = if (paymentMode == "UPI") UpiBlue else PeaceCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("mode_upi_button"),
                        color = if (paymentMode == "UPI") UpiBlueLight else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📱 ", fontSize = 16.sp)
                            Text(
                                text = "UPI (ऑनलाइन)",
                                fontWeight = if (paymentMode == "UPI") FontWeight.Bold else FontWeight.Normal,
                                color = if (paymentMode == "UPI") UpiBlue else TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Status: Tick option for "Payment Received" and "Payment Not Received"
                Text(
                    text = "भुगतान स्थिति (Payment Status):",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Payment Received (Tick Option)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isPaymentReceived = true }
                            .border(
                                width = if (isPaymentReceived) 2.dp else 1.dp,
                                color = if (isPaymentReceived) DonationGreen else PeaceCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("payment_received_option"),
                        color = if (isPaymentReceived) DonationGreenLight else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPaymentReceived) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = "Received",
                                tint = if (isPaymentReceived) DonationGreen else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "भुगतान प्राप्त",
                                    fontSize = 12.sp,
                                    fontWeight = if (isPaymentReceived) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPaymentReceived) DonationGreen else TextPrimary
                                )
                                Text(
                                    text = "Received (जमा)",
                                    fontSize = 10.sp,
                                    color = if (isPaymentReceived) DonationGreen else TextSecondary
                                )
                            }
                        }
                    }

                    // Option 2: Payment Not Received (Tick Option)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isPaymentReceived = false }
                            .border(
                                width = if (!isPaymentReceived) 2.dp else 1.dp,
                                color = if (!isPaymentReceived) ExpenseRed else PeaceCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("payment_not_received_option"),
                        color = if (!isPaymentReceived) ExpenseRedLight else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (!isPaymentReceived) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = "Not Received",
                                tint = if (!isPaymentReceived) ExpenseRed else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "भुगतान अप्राप्त",
                                    fontSize = 12.sp,
                                    fontWeight = if (!isPaymentReceived) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isPaymentReceived) ExpenseRed else TextPrimary
                                )
                                Text(
                                    text = "Not Received (बाक़ी)",
                                    fontSize = 10.sp,
                                    color = if (!isPaymentReceived) ExpenseRed else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Received By (Volunteer)
                OutlinedTextField(
                    value = receivedBy,
                    onValueChange = { receivedBy = it },
                    label = { Text("प्राप्तकर्ता सेवादार (Received By)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes / Purpose
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("सेवा संकल्प / टिप्पणी (Notes / Purpose)") },
                    placeholder = { Text("उदा. भोग सेवा, परिवार कल्याण हेतु") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("रद्द करें (Cancel)", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val parsedAmount = amountText.toDoubleOrNull()
                            if (donorName.isBlank()) {
                                donorNameError = true
                                return@Button
                            }
                            if (parsedAmount == null || parsedAmount <= 0) {
                                amountError = true
                                return@Button
                            }

                            onSave(
                                donorName,
                                parsedAmount,
                                mobileNumber,
                                paymentMode,
                                referenceId,
                                notes,
                                receivedBy,
                                targetKirtanId,
                                isPaymentReceived
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        modifier = Modifier.testTag("save_donation_button")
                    ) {
                        Text(
                            text = if (initialDonation == null) "दान सुरक्षित करें (Save)" else "अपडेट करें (Update)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ADD / EDIT EXPENSE DIALOG
// -----------------------------------------------------------------------------
@Composable
fun AddExpenseDialog(
    initialExpense: ExpenseEntity? = null,
    kirtans: List<KirtanEntity>,
    selectedKirtanId: Long?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        amount: Double,
        paymentMode: String,
        paidTo: String,
        referenceId: String,
        notes: String,
        kirtanId: Long
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var category by remember { mutableStateOf(initialExpense?.category ?: ExpenseCategories.first()) }
    var amountText by remember {
        mutableStateOf(if (initialExpense != null) initialExpense.amount.toInt().toString() else "")
    }
    var paymentMode by remember { mutableStateOf(initialExpense?.paymentMode ?: "CASH") }
    var paidTo by remember { mutableStateOf(initialExpense?.paidTo ?: "") }
    var referenceId by remember { mutableStateOf(initialExpense?.referenceId ?: "") }
    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }
    var targetKirtanId by remember {
        mutableStateOf(
            initialExpense?.kirtanId
                ?: selectedKirtanId
                ?: kirtans.firstOrNull()?.id
                ?: 0L
        )
    }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_expense_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialExpense == null) "खर्चा प्रविष्टि (Expense Entry)" else "खर्चा विवरण संपादित करें",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Text(
                            text = "Track Kirtan Expense & Vendor Payment",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kirtan selection if multiple
                if (kirtans.size > 1 && initialExpense == null) {
                    Text(
                        text = "कीर्तन चयन करें:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        kirtans.forEach { k ->
                            FilterChip(
                                selected = targetKirtanId == k.id,
                                onClick = { targetKirtanId = k.id },
                                label = { Text(k.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronContainer,
                                    selectedLabelColor = SaffronDark
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = it.isBlank()
                    },
                    label = { Text("मद / विवरण (Expense Title / Item) *") },
                    placeholder = { Text("उदा. साउंड सिस्टम, फूल शृंगार, पेड़ा प्रसाद") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )
                if (titleError) {
                    Text(
                        text = "कृपया मद या खर्च का विवरण दर्ज करें",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selection
                Text(
                    text = "श्रेणी (Category):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronContainer,
                                selectedLabelColor = SaffronDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            amountText = it
                            amountError = it.toDoubleOrNull() == null || it.toDouble() <= 0
                        }
                    },
                    label = { Text("खर्च राशि (Amount in ₹) *") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = ExpenseRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )
                if (amountError) {
                    Text(
                        text = "कृपया सही राशि दर्ज करें",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Mode Selection (CASH / UPI)
                Text(
                    text = "भुगतान माध्यम (Payment Mode):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { paymentMode = "CASH" }
                            .border(
                                width = if (paymentMode == "CASH") 2.dp else 1.dp,
                                color = if (paymentMode == "CASH") CashAmber else PeaceCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("expense_mode_cash_button"),
                        color = if (paymentMode == "CASH") SaffronContainer else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "💵 ", fontSize = 16.sp)
                            Text(
                                text = "CASH (नकद)",
                                fontWeight = if (paymentMode == "CASH") FontWeight.Bold else FontWeight.Normal,
                                color = if (paymentMode == "CASH") CashAmber else TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { paymentMode = "UPI" }
                            .border(
                                width = if (paymentMode == "UPI") 2.dp else 1.dp,
                                color = if (paymentMode == "UPI") UpiBlue else PeaceCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("expense_mode_upi_button"),
                        color = if (paymentMode == "UPI") UpiBlueLight else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📱 ", fontSize = 16.sp)
                            Text(
                                text = "UPI (ऑनलाइन)",
                                fontWeight = if (paymentMode == "UPI") FontWeight.Bold else FontWeight.Normal,
                                color = if (paymentMode == "UPI") UpiBlue else TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Paid To (Vendor / Recipient)
                OutlinedTextField(
                    value = paidTo,
                    onValueChange = { paidTo = it },
                    label = { Text("भुगतान प्राप्तकर्ता (Paid To / Vendor Name)") },
                    placeholder = { Text("उदा. स्टार साउंड, बीकानेर स्वीट्स") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Ref / Bill Number
                OutlinedTextField(
                    value = referenceId,
                    onValueChange = { referenceId = it },
                    label = { Text(if (paymentMode == "UPI") "UPI Txn ID / UTR" else "बिल / वाउचर सं (Bill / Voucher No)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("विवरण / टिप्पणी (Notes / Remarks)") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("रद्द करें (Cancel)", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val parsedAmount = amountText.toDoubleOrNull()
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }
                            if (parsedAmount == null || parsedAmount <= 0) {
                                amountError = true
                                return@Button
                            }

                            onSave(
                                title,
                                category,
                                parsedAmount,
                                paymentMode,
                                paidTo,
                                referenceId,
                                notes,
                                targetKirtanId
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        modifier = Modifier.testTag("save_expense_button")
                    ) {
                        Text(
                            text = if (initialExpense == null) "खर्चा सुरक्षित करें (Save)" else "अपडेट करें (Update)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// ADD / EDIT KIRTAN EVENT DIALOG
// -----------------------------------------------------------------------------
@Composable
fun AddKirtanDialog(
    initialKirtan: KirtanEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        organizer: String,
        location: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialKirtan?.name ?: "") }
    var organizer by remember { mutableStateOf(initialKirtan?.organizer ?: "") }
    var location by remember { mutableStateOf(initialKirtan?.location ?: "") }
    var notes by remember { mutableStateOf(initialKirtan?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_kirtan_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialKirtan == null) "नया कीर्तन जोड़ें" else "कीर्तन संपादित करें",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "Create / Manage Kirtan Event",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("कीर्तन / कार्यक्रम का नाम (Kirtan Name) *") },
                    placeholder = { Text("उदा. श्री संकीर्तन, माता की चौकी") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kirtan_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )
                if (nameError) {
                    Text(
                        text = "कृपया कीर्तन का नाम दर्ज करें",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Organizer
                OutlinedTextField(
                    value = organizer,
                    onValueChange = { organizer = it },
                    label = { Text("आयोजक / मंडल (Organizer / Mandal)") },
                    placeholder = { Text("उदा. श्री संकीर्तन सेवा मंडल, शर्मा परिवार") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("स्थान / पता (Location / Venue)") },
                    placeholder = { Text("उदा. सामुदायिक भवन, मॉडल टाउन") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("विवरण / संकल्प (Description / Notes)") },
                    placeholder = { Text("उदा. छप्पन भोग एवं भव्य शृंगार सहित") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        focusedLabelColor = SaffronPrimary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("रद्द करें (Cancel)", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            onSave(name, organizer, location, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        modifier = Modifier.testTag("save_kirtan_button")
                    ) {
                        Text(
                            text = if (initialKirtan == null) "कीर्तन जोड़ें (Create)" else "अपडेट करें (Update)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DONOR RECEIPT SLIP DIALOG
// -----------------------------------------------------------------------------
@Composable
fun ReceiptDialog(
    donation: DonationEntity,
    kirtanName: String,
    receiptText: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PeaceIvory),
            border = BorderStroke(1.5.dp, DevotionalGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("receipt_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Receipt Header
                Text(
                    text = "कीर्तन सेवा - दान रसीद",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SaffronDark
                )
                Text(
                    text = "Donation Acknowledgement Slip",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dashed receipt card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PeaceCardBorder, RoundedCornerShape(14.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Prominent Receipt Number and Status at Top of Receipt Card
                        val recNo = if (donation.referenceId.isNotBlank()) donation.referenceId else "REC-${donation.id}"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SaffronContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = SaffronDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "रसीद सं: #$recNo",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronDark
                                    )
                                }
                            }

                            // Payment Status Pill (Tick option result)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (donation.isPaymentReceived) DonationGreenLight else ExpenseRedLight
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (donation.isPaymentReceived) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (donation.isPaymentReceived) DonationGreen else ExpenseRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (donation.isPaymentReceived) "प्राप्त (Received)" else "अप्राप्त (Pending)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (donation.isPaymentReceived) DonationGreen else ExpenseRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "दिनांक: ${Formatters.formatDateOnly(donation.dateMillis)}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = PeaceCardBorder)

                        // Donor Name
                        Text(text = "दानदाता (Donor Name)", fontSize = 11.sp, color = TextSecondary)
                        Text(text = donation.donorName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Amount Highlight
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DonationGreenLight, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "सहयोग राशि (Amount):", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DonationGreen)
                            Text(
                                text = Formatters.formatCurrency(donation.amount),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = DonationGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mode & Mobile
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "माध्यम (Payment Mode):", fontSize = 11.sp, color = TextSecondary)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (donation.paymentMode == "UPI") UpiBlueLight else CashAmberLight
                                ) {
                                    Text(
                                        text = "${if (donation.paymentMode == "UPI") "📱 UPI" else "💵 CASH"}${if (donation.referenceId.isNotBlank()) " (${donation.referenceId})" else ""}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (donation.paymentMode == "UPI") UpiBlue else CashAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (donation.mobileNumber.isNotBlank()) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "मोबाईल (Mobile):", fontSize = 11.sp, color = TextSecondary)
                                    Text(text = donation.mobileNumber, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Kirtan
                        Text(text = "कीर्तन / आयोजन (Kirtan Event):", fontSize = 11.sp, color = TextSecondary)
                        Text(text = kirtanName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SaffronDark)

                        if (donation.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "संकल्प / विवरण:", fontSize = 11.sp, color = TextSecondary)
                            Text(text = donation.notes, fontSize = 12.sp, color = TextPrimary)
                        }

                        if (donation.receivedBy.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "प्राप्तकर्ता (Received By):", fontSize = 11.sp, color = TextSecondary)
                            Text(text = donation.receivedBy, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Share, Call, Copy
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share via WhatsApp
                    Button(
                        onClick = {
                            shareText(context, "Kirtan Donation Receipt", receiptText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Share (व्हाट्सएप)", fontSize = 12.sp)
                    }

                    // Copy Receipt Text
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(receiptText))
                            copied = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronDark),
                        border = BorderStroke(1.dp, SaffronPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (copied) "कॉपी हुआ!" else "Copy Slip", fontSize = 12.sp)
                    }
                }

                if (donation.mobileNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${donation.mobileNumber}")
                            }
                            context.startActivity(callIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, PeaceCardBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = DonationGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("दानदाता को कॉल करें (${donation.mobileNumber})", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Edit / Delete / Close Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = SaffronPrimary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseRed)
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text("बंद करें (Close)", color = TextSecondary)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TRANSPARENCY REPORT DIALOG
// -----------------------------------------------------------------------------
@Composable
fun TransparencyReportDialog(
    reportText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("transparency_report_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "पारदर्शिता रिपोर्ट (Transparency Report)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Text(
                            text = "Ready to share with Committee & WhatsApp Group",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Report Preview Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 340.dp)
                        .border(1.dp, PeaceCardBorder, RoundedCornerShape(12.dp)),
                    color = SaffronContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = reportText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            shareText(context, "Kirtan Transparency Report", reportText)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("share_transparency_whatsapp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp पर शेयर करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(reportText))
                            copied = true
                        },
                        modifier = Modifier.weight(0.8f),
                        border = BorderStroke(1.dp, SaffronPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronDark)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (copied) "कॉपी हुआ!" else "Copy", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CONFIRM DELETE DIALOG
// -----------------------------------------------------------------------------
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, color = ExpenseRed) },
        text = { Text(text = message, color = TextPrimary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("हटाएं (Delete)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें (Cancel)", color = TextSecondary)
            }
        }
    )
}

// -----------------------------------------------------------------------------
// SETTINGS DIALOG (LANGUAGE & THEME)
// -----------------------------------------------------------------------------
@Composable
fun SettingsDialog(
    currentThemeMode: ThemeMode,
    currentPalette: DevotionalPalette,
    currentLanguage: AppLanguage,
    autoBackupEnabled: Boolean = true,
    lastBackupTime: Long = 0L,
    localBackups: List<LocalBackupInfo> = emptyList(),
    onThemeModeSelected: (ThemeMode) -> Unit,
    onPaletteSelected: (DevotionalPalette) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onToggleAutoBackup: (Boolean) -> Unit = {},
    onBackupNow: () -> Unit = {},
    onRestoreFromUri: (Uri) -> Unit = {},
    onRestoreFromFile: (File) -> Unit = {},
    onDeleteBackup: (File) -> Unit = {},
    onDefaultReset: (keepSampleData: Boolean) -> Unit = {},
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val strings = Localization.get(currentLanguage)
    val context = LocalContext.current

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var restoreTargetFile by remember { mutableStateOf<File?>(null) }
    var restoreTargetUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            restoreTargetUri = uri
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.settingsDialogTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "ऐप प्राथमिकताएं एवं बैकअप (Preferences & Data)",
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = colors.divider, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: LANGUAGE SELECTION
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🌐", fontSize = 16.sp)
                    Text(
                        text = strings.languageSectionTitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val languages = listOf(
                        Triple(AppLanguage.HINDI, "हिन्दी (Hindi)", "पूर्णतः हिन्दी में सभी मेनू एवं रसीदें"),
                        Triple(AppLanguage.ENGLISH, "English", "All accounts, slips, and tabs in English"),
                        Triple(AppLanguage.HINGLISH, "हिंग्लिश (Hinglish)", "Daan, Kharcha & Seva bilingual mix")
                    )

                    languages.forEach { (lang, title, subtitle) ->
                        val isSelected = currentLanguage == lang
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) colors.primaryContainer.copy(alpha = if (colors.isDark) 0.4f else 0.6f) else colors.canvas,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.cardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onLanguageSelected(lang) }
                                .testTag("lang_option_${lang.code}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.primary else colors.textPrimary
                                    )
                                    Text(
                                        text = subtitle,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        color = colors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = colors.divider, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: THEME MODE SELECTION
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🎨", fontSize = 16.sp)
                    Text(
                        text = strings.themeSectionTitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.values().forEach { mode ->
                        val isSelected = currentThemeMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.primaryContainer.copy(alpha = if (colors.isDark) 0.5f else 0.7f) else colors.canvas,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.cardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onThemeModeSelected(mode) }
                                .testTag("theme_mode_${mode.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = mode.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.ENGLISH) mode.titleEn else mode.titleHi,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colors.primary else colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = colors.divider, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: DEVOTIONAL PALETTE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🌸", fontSize = 16.sp)
                    Text(
                        text = strings.paletteSectionTitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DevotionalPalette.values().forEach { palette ->
                        val isSelected = currentPalette == palette
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.primaryContainer.copy(alpha = if (colors.isDark) 0.35f else 0.5f) else colors.canvas,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.cardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPaletteSelected(palette) }
                                .testTag("palette_${palette.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(palette.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.ENGLISH) palette.titleEn else palette.titleHi,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "● चयनित",
                                        color = colors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = colors.divider, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 4: AUTOMATIC DAILY BACKUP IN MOBILE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "📱", fontSize = 16.sp)
                        Column {
                            Text(
                                text = strings.autoBackupTitle,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = strings.autoBackupDesc,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Switch(
                        checked = autoBackupEnabled,
                        onCheckedChange = onToggleAutoBackup,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.canvas
                        ),
                        modifier = Modifier.testTag("auto_backup_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Last backup timestamp banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.canvas,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.lastBackupLabel,
                            fontSize = 11.5.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = if (lastBackupTime > 0) {
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastBackupTime))
                            } else {
                                if (currentLanguage == AppLanguage.ENGLISH) "No backup yet" else "अभी तक कोई नहीं"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (lastBackupTime > 0) colors.primary else colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Manual Backup Now Button
                Button(
                    onClick = onBackupNow,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backup_now_button")
                ) {
                    Text(
                        text = strings.backupNowBtn,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 12.5.sp
                    )
                }

                // Local Backups List on Mobile
                if (localBackups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.localBackupsTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        localBackups.take(4).forEach { backup ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.canvas,
                                border = BorderStroke(0.8.dp, colors.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (backup.isAuto) colors.primary.copy(alpha = 0.15f) else DonationGreenLight
                                            ) {
                                                Text(
                                                    text = if (backup.isAuto) "ऑटो" else "मैनुअल",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (backup.isAuto) colors.primary else DonationGreen,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = backup.formattedDate,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textPrimary
                                            )
                                        }
                                        Text(
                                            text = "${backup.kirtanCount} कीर्तन • ${backup.donationCount} दान • ${backup.expenseCount} खर्च",
                                            fontSize = 10.sp,
                                            color = colors.textSecondary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Share backup button
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_SUBJECT, "Kirtan Seva Backup")
                                                        putExtra(Intent.EXTRA_TEXT, backup.file.readText())
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "बैकअप शेयर करें"))
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        // Restore button
                                        OutlinedButton(
                                            onClick = { restoreTargetFile = backup.file },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, colors.primary),
                                            modifier = Modifier
                                                .height(28.dp)
                                                .padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = strings.restoreBtn,
                                                fontSize = 10.sp,
                                                color = colors.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Delete button
                                        IconButton(
                                            onClick = { onDeleteBackup(backup.file) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = ExpenseRed.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = colors.divider, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 5: RESTORE FROM MOBILE (FILE PICKER)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "📂", fontSize = 16.sp)
                    Text(
                        text = strings.restoreSectionTitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            filePickerLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("choose_backup_file_button")
                ) {
                    Text(
                        text = strings.restoreFromFileBtn,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary,
                        fontSize = 12.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = colors.divider, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 6: DEFAULT RESET
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "⚠️", fontSize = 16.sp)
                    Text(
                        text = strings.resetSectionTitle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showResetConfirmDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("default_reset_button")
                ) {
                    Text(
                        text = strings.resetBtn,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed,
                        fontSize = 12.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_settings_button")
                ) {
                    Text(
                        text = strings.doneBtn,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }

    // Confirm Restore from File Dialog
    if (restoreTargetFile != null) {
        val file = restoreTargetFile!!
        AlertDialog(
            onDismissRequest = { restoreTargetFile = null },
            title = { Text(text = "डेटा रीस्टोर की पुष्टि (Confirm Restore)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = "क्या आप '${file.name}' बैकअप फ़ाइल से डेटा रीस्टोर करना चाहते हैं?\n\nआपकी सुरक्षा के लिए मौजूदा डेटा का बैकअप स्वतः सुरक्षित कर लिया जाएगा।",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreFromFile(file)
                        restoreTargetFile = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(text = strings.confirmBtn, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreTargetFile = null }) {
                    Text(text = strings.cancelBtn)
                }
            }
        )
    }

    // Confirm Restore from Uri Dialog
    if (restoreTargetUri != null) {
        val uri = restoreTargetUri!!
        AlertDialog(
            onDismissRequest = { restoreTargetUri = null },
            title = { Text(text = "डेटा रीस्टोर की पुष्टि (Confirm Restore)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    text = "क्या आप चुनी गई फ़ाइल से सारा कीर्तन, दान और खर्च डेटा रीस्टोर करना चाहते हैं?\n\nसुरक्षा हेतु वर्तमान डेटा का स्वतः बैकअप लिया जाएगा।",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreFromUri(uri)
                        restoreTargetUri = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text(text = strings.confirmBtn, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreTargetUri = null }) {
                    Text(text = strings.cancelBtn)
                }
            }
        )
    }

    // Default Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = strings.resetWarningTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ExpenseRed
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.resetWarningMessage,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.canvas,
                        border = BorderStroke(0.8.dp, colors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 रीसेट से पहले आपके वर्तमान डेटा की एक 'pre_reset' बैकअप कॉपी फोन में ऑटो-सेव हो जाएगी।",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            showResetConfirmDialog = false
                            onDefaultReset(true)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = strings.resetSampleBtn, color = Color.White, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            showResetConfirmDialog = false
                            onDefaultReset(false)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = strings.resetBlankBtn, color = ExpenseRed, fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { showResetConfirmDialog = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = strings.cancelBtn)
                    }
                }
            },
            dismissButton = null
        )
    }
}
