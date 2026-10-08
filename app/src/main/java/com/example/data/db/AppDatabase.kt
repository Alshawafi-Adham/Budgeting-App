package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.JournalEntryEntity
import com.example.data.model.RecurringTransactionEntity
import com.example.data.model.SavingsContributionEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserSettingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        JournalEntryEntity::class,
        SavingsGoalEntity::class,
        SavingsContributionEntity::class,
        RecurringTransactionEntity::class,
        UserSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun journalDao(): JournalDao
    abstract fun savingsDao(): SavingsDao
    abstract fun recurringDao(): RecurringDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val PRESET_CATEGORIES = listOf(
            CategoryEntity(name = "Food", type = "expense", iconName = "restaurant", colorHex = "#EF4444", isPreset = true, subcategories = "Groceries,Restaurants,Coffee & Cafes,Food Delivery,Alcohol & Bars"),
            CategoryEntity(name = "Transport", type = "expense", iconName = "directions_car", colorHex = "#3B82F6", isPreset = true, subcategories = "Fuel / Petrol,Public Transit,Rideshare / Taxi,Parking,Vehicle Maintenance"),
            CategoryEntity(name = "Housing", type = "expense", iconName = "home", colorHex = "#8B5CF6", isPreset = true, subcategories = "Rent,Mortgage,Property Tax,Repairs & Maintenance,Furniture & Decor"),
            CategoryEntity(name = "Utilities", type = "expense", iconName = "bolt", colorHex = "#F59E0B", isPreset = true, subcategories = "Electricity,Water,High-speed Internet,Mobile / Phone,Gas,Waste / Trash"),
            CategoryEntity(name = "Entertainment", type = "expense", iconName = "movie", colorHex = "#EC4899", isPreset = true, subcategories = "Streaming Services,Movies & Concerts,Gaming,Hobbies & Crafts,Books & News"),
            CategoryEntity(name = "Health", type = "expense", iconName = "medical_services", colorHex = "#10B981", isPreset = true, subcategories = "Doctor & Dentist,Pharmacy & Meds,Fitness & Gym,Health Insurance,Therapy"),
            CategoryEntity(name = "Shopping", type = "expense", iconName = "shopping_bag", colorHex = "#06B6D4", isPreset = true, subcategories = "Clothing & Shoes,Electronics,Personal Care & Beauty,Home Essentials,Gifts"),
            CategoryEntity(name = "Education", type = "expense", iconName = "school", colorHex = "#6366F1", isPreset = true, subcategories = "Courses & Tuition,Books & Study Materials,Workshops,Certifications"),
            CategoryEntity(name = "Savings", type = "both", iconName = "savings", colorHex = "#14B8A6", isPreset = true, subcategories = "Emergency Fund,Retirement Fund,Investments,Vacation Goal"),
            CategoryEntity(name = "Other", type = "both", iconName = "more_horiz", colorHex = "#64748B", isPreset = true, subcategories = "General,Cash Gift,Reimbursement,Miscellaneous"),
            // Default income categories
            CategoryEntity(name = "Salary", type = "income", iconName = "payments", colorHex = "#10B981", isPreset = true, subcategories = "Base Salary,Performance Bonus,Overtime,Commission"),
            CategoryEntity(name = "Freelance", type = "income", iconName = "work", colorHex = "#3B82F6", isPreset = true, subcategories = "Client Projects,Consulting,Side Business"),
            CategoryEntity(name = "Investment", type = "income", iconName = "trending_up", colorHex = "#8B5CF6", isPreset = true, subcategories = "Dividends,Capital Gains,Interest,Rental Yield")
        )

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budgetflow.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.categoryDao().insertAll(PRESET_CATEGORIES)
                            // Initialize default settings
                            database.settingsDao().setSetting(UserSettingEntity("currency", "MYR"))
                            database.settingsDao().setSetting(UserSettingEntity("theme", "system"))
                            database.settingsDao().setSetting(UserSettingEntity("start_of_week", "sunday"))
                            database.settingsDao().setSetting(UserSettingEntity("budget_cycle_start_day", "1"))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
