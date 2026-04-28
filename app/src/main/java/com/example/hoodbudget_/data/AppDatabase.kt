package com.example.hoodbudget_.data

import android.content.Context
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, Category::class, ExpenseEntry::class, MonthlyGoal::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `username` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` TEXT NOT NULL,
                        `startTime` TEXT NOT NULL,
                        `endTime` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `categoryName` TEXT NOT NULL,
                        `photoUri` TEXT
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `monthly_goals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `username` TEXT NOT NULL,
                        `month` TEXT NOT NULL,
                        `minAmount` REAL NOT NULL,
                        `maxAmount` REAL NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                        "`index_monthly_goals_username_month` ON `monthly_goals` (`username`, `month`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hood_budget_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
