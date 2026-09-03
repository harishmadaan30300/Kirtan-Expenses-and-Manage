package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import com.example.ui.util.BackupManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kirtan Seva", appName)
  }

  @Test
  fun `test backup serialization and deserialization`() {
    val testKirtan = KirtanEntity(
        id = 101L,
        name = "श्री संकीर्तन महोत्सव",
        organizer = "श्री संकीर्तन परिवार",
        location = "वृन्दावन धाम",
        dateMillis = 1725350000000L,
        notes = "भव्य कीर्तन",
        isCompleted = false,
        createdAt = 1725300000000L
    )

    val testDonation = DonationEntity(
        id = 201L,
        kirtanId = 101L,
        donorName = "राम शरण जी",
        amount = 5100.0,
        mobileNumber = "9876543210",
        paymentMode = "UPI",
        referenceId = "UPI-9876543210",
        dateMillis = 1725355000000L,
        notes = "प्रसाद सेवा",
        receivedBy = "गोपाल दास"
    )

    val testExpense = ExpenseEntity(
        id = 301L,
        kirtanId = 101L,
        title = "माइक व साउंड सिस्टम",
        category = "साउंड व ऑडियो",
        amount = 15000.0,
        paymentMode = "CASH",
        paidTo = "स्टार साउंड सर्विस",
        referenceId = "VOUCHER-01",
        dateMillis = 1725360000000L,
        notes = "एडवांस भुगतान"
    )

    val jsonString = BackupManager.toJsonString(
        kirtans = listOf(testKirtan),
        donations = listOf(testDonation),
        expenses = listOf(testExpense)
    )

    assertNotNull(jsonString)
    assertTrue(jsonString.contains("श्री संकीर्तन महोत्सव"))
    assertTrue(jsonString.contains("राम शरण जी"))
    assertTrue(jsonString.contains("माइक व साउंड सिस्टम"))

    val parseResult = BackupManager.parseJsonString(jsonString)
    assertTrue("Parsing should succeed", parseResult.isSuccess)
    assertNotNull(parseResult.payload)

    val payload = parseResult.payload!!
    assertEquals(1, payload.kirtans.size)
    assertEquals("श्री संकीर्तन महोत्सव", payload.kirtans[0].name)
    assertEquals(1, payload.donations.size)
    assertEquals(5100.0, payload.donations[0].amount, 0.001)
    assertEquals(1, payload.expenses.size)
    assertEquals("स्टार साउंड सर्विस", payload.expenses[0].paidTo)
  }
}
