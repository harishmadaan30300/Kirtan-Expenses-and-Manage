package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.DonationDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.KirtanDao
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity

@Database(
    entities = [KirtanEntity::class, DonationEntity::class, ExpenseEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kirtanDao(): KirtanDao
    abstract fun donationDao(): DonationDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kirtan_seva_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
