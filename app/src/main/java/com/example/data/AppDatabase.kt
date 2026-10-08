package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.CategoryBudgetEntity
import com.example.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [TransactionEntity::class, CategoryBudgetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dompetku_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate sample data so new users experience charts and categories immediately
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialData(database.transactionDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(dao: TransactionDao) {
            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

            val currentMonth = monthFormat.format(cal.time)

            // Setup budgets
            dao.insertOrUpdateBudget(CategoryBudgetEntity("cat_groceries", currentMonth, 1500000.0))
            dao.insertOrUpdateBudget(CategoryBudgetEntity("cat_food", currentMonth, 1200000.0))
            dao.insertOrUpdateBudget(CategoryBudgetEntity("cat_transport", currentMonth, 600000.0))
            dao.insertOrUpdateBudget(CategoryBudgetEntity("cat_bills", currentMonth, 800000.0))
            dao.insertOrUpdateBudget(CategoryBudgetEntity("cat_online_shop", currentMonth, 500000.0))

            // Initial transactions within this month
            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000

            val sampleList = listOf(
                TransactionEntity(
                    amount = 7500000.0,
                    type = "INCOME",
                    categoryId = "cat_salary",
                    categoryName = "Gaji Pokok",
                    note = "Gaji Bulan Ini",
                    paymentMethod = "Transfer Bank",
                    timestamp = now - (6 * dayMillis),
                    dateString = dateFormat.format(Date(now - (6 * dayMillis))),
                    monthString = currentMonth
                ),
                TransactionEntity(
                    amount = 285000.0,
                    type = "EXPENSE",
                    categoryId = "cat_groceries",
                    categoryName = "Belanja Harian",
                    note = "Belanja mingguan di Supermarket",
                    paymentMethod = "E-Wallet",
                    timestamp = now - (5 * dayMillis),
                    dateString = dateFormat.format(Date(now - (5 * dayMillis))),
                    monthString = currentMonth
                ),
                TransactionEntity(
                    amount = 45000.0,
                    type = "EXPENSE",
                    categoryId = "cat_food",
                    categoryName = "Makanan & Minuman",
                    note = "Makan siang ayam geprek & es teh",
                    paymentMethod = "Tunai",
                    timestamp = now - (4 * dayMillis),
                    dateString = dateFormat.format(Date(now - (4 * dayMillis))),
                    monthString = currentMonth
                ),
                TransactionEntity(
                    amount = 50000.0,
                    type = "EXPENSE",
                    categoryId = "cat_transport",
                    categoryName = "Transportasi",
                    note = "Isi bensin Pertamax motor",
                    paymentMethod = "Tunai",
                    timestamp = now - (3 * dayMillis),
                    dateString = dateFormat.format(Date(now - (3 * dayMillis))),
                    monthString = currentMonth
                ),
                TransactionEntity(
                    amount = 175000.0,
                    type = "EXPENSE",
                    categoryId = "cat_online_shop",
                    categoryName = "Belanja Online",
                    note = "Beli perlengkapan meja kerja",
                    paymentMethod = "Transfer Bank",
                    timestamp = now - (2 * dayMillis),
                    dateString = dateFormat.format(Date(now - (2 * dayMillis))),
                    monthString = currentMonth
                ),
                TransactionEntity(
                    amount = 350000.0,
                    type = "EXPENSE",
                    categoryId = "cat_bills",
                    categoryName = "Tagihan & Utilitas",
                    note = "Token listrik PLN & Wi-Fi",
                    paymentMethod = "E-Wallet",
                    timestamp = now - (1 * dayMillis),
                    dateString = dateFormat.format(Date(now - (1 * dayMillis))),
                    monthString = currentMonth
                ),
                TransactionEntity(
                    amount = 85000.0,
                    type = "EXPENSE",
                    categoryId = "cat_food",
                    categoryName = "Makanan & Minuman",
                    note = "Kopi & snack santai bareng teman",
                    paymentMethod = "E-Wallet",
                    timestamp = now - (3 * 3600 * 1000), // 3 hours ago
                    dateString = dateFormat.format(Date(now)),
                    monthString = currentMonth
                ),
                TransactionEntity(
                    amount = 145000.0,
                    type = "EXPENSE",
                    categoryId = "cat_groceries",
                    categoryName = "Belanja Harian",
                    note = "Beli sayuran & buah segar",
                    paymentMethod = "Tunai",
                    timestamp = now,
                    dateString = dateFormat.format(Date(now)),
                    monthString = currentMonth
                )
            )

            dao.insertTransactions(sampleList)
        }
    }
}
