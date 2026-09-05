package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BookingDao
import com.example.data.dao.DonationDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.KirtanDao
import com.example.data.entity.BookingEntity
import com.example.data.entity.DonationEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.KirtanEntity

@Database(
    entities = [KirtanEntity::class, DonationEntity::class, ExpenseEntity::class, BookingEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kirtanDao(): KirtanDao
    abstract fun donationDao(): DonationDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bookings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `kirtanId` INTEGER,
                        `categoryId` TEXT NOT NULL,
                        `serviceTitle` TEXT NOT NULL,
                        `vendorName` TEXT NOT NULL,
                        `contactNumber` TEXT NOT NULL,
                        `eventDateMillis` INTEGER NOT NULL,
                        `totalAmount` REAL NOT NULL,
                        `advancePaid` REAL NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kirtan_seva_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
