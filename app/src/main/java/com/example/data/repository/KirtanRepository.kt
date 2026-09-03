package com.example.data.repository

import com.example.data.dao.DonationDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.KirtanDao
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class KirtanRepository(
    private val kirtanDao: KirtanDao,
    private val donationDao: DonationDao,
    private val expenseDao: ExpenseDao
) {
    val allKirtans: Flow<List<KirtanEntity>> = kirtanDao.getAllKirtans()
    val allDonations: Flow<List<DonationEntity>> = donationDao.getAllDonations()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getDonationsForKirtan(kirtanId: Long): Flow<List<DonationEntity>> =
        donationDao.getDonationsForKirtan(kirtanId)

    fun getExpensesForKirtan(kirtanId: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesForKirtan(kirtanId)

    suspend fun insertKirtan(kirtan: KirtanEntity): Long = withContext(Dispatchers.IO) {
        kirtanDao.insertKirtan(kirtan)
    }

    suspend fun updateKirtan(kirtan: KirtanEntity) = withContext(Dispatchers.IO) {
        kirtanDao.updateKirtan(kirtan)
    }

    suspend fun deleteKirtan(kirtan: KirtanEntity) = withContext(Dispatchers.IO) {
        donationDao.deleteDonationsForKirtan(kirtan.id)
        expenseDao.deleteExpensesForKirtan(kirtan.id)
        kirtanDao.deleteKirtan(kirtan)
    }

    suspend fun insertDonation(donation: DonationEntity): Long = withContext(Dispatchers.IO) {
        donationDao.insertDonation(donation)
    }

    suspend fun updateDonation(donation: DonationEntity) = withContext(Dispatchers.IO) {
        donationDao.updateDonation(donation)
    }

    suspend fun deleteDonation(donation: DonationEntity) = withContext(Dispatchers.IO) {
        donationDao.deleteDonation(donation)
    }

    suspend fun deleteDonationById(id: Long) = withContext(Dispatchers.IO) {
        donationDao.deleteDonationById(id)
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun getAllKirtansSync(): List<KirtanEntity> = withContext(Dispatchers.IO) {
        kirtanDao.getAllKirtansSync()
    }

    suspend fun getAllDonationsSync(): List<DonationEntity> = withContext(Dispatchers.IO) {
        donationDao.getAllDonationsSync()
    }

    suspend fun getAllExpensesSync(): List<ExpenseEntity> = withContext(Dispatchers.IO) {
        expenseDao.getAllExpensesSync()
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        donationDao.deleteAllDonations()
        expenseDao.deleteAllExpenses()
        kirtanDao.deleteAllKirtans()
    }

    suspend fun restoreAllData(
        kirtans: List<KirtanEntity>,
        donations: List<DonationEntity>,
        expenses: List<ExpenseEntity>
    ) = withContext(Dispatchers.IO) {
        // Clear existing tables
        donationDao.deleteAllDonations()
        expenseDao.deleteAllExpenses()
        kirtanDao.deleteAllKirtans()

        // Insert new records
        if (kirtans.isNotEmpty()) {
            kirtanDao.insertAllKirtans(kirtans)
        }
        if (donations.isNotEmpty()) {
            donationDao.insertAllDonations(donations)
        }
        if (expenses.isNotEmpty()) {
            expenseDao.insertAllExpenses(expenses)
        }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Will be called when starting if no kirtan exists
    }
}
