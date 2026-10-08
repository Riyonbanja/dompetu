package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String) {
    EXPENSE("Pengeluaran"),
    INCOME("Pemasukan")
}

data class CategoryItem(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconKey: String,
    val colorHex: Long,
    val isDefault: Boolean = true
)

object DefaultCategories {
    val list = listOf(
        // Expense Categories
        CategoryItem("cat_groceries", "Belanja Harian", TransactionType.EXPENSE, "shopping_cart", 0xFF10B981),
        CategoryItem("cat_food", "Makanan & Minuman", TransactionType.EXPENSE, "restaurant", 0xFFF97316),
        CategoryItem("cat_transport", "Transportasi", TransactionType.EXPENSE, "directions_car", 0xFF3B82F6),
        CategoryItem("cat_online_shop", "Belanja Online", TransactionType.EXPENSE, "storefront", 0xFF06B6D4),
        CategoryItem("cat_bills", "Tagihan & Utilitas", TransactionType.EXPENSE, "receipt_long", 0xFF8B5CF6),
        CategoryItem("cat_entertainment", "Hiburan", TransactionType.EXPENSE, "movie", 0xFFEC4899),
        CategoryItem("cat_health", "Kesehatan", TransactionType.EXPENSE, "medical_services", 0xFFE11D48),
        CategoryItem("cat_education", "Pendidikan", TransactionType.EXPENSE, "school", 0xFF6366F1),
        CategoryItem("cat_home", "Rumah Tangga", TransactionType.EXPENSE, "home", 0xFF14B8A6),
        CategoryItem("cat_other_exp", "Lain-lain", TransactionType.EXPENSE, "category", 0xFF64748B),

        // Income Categories
        CategoryItem("cat_salary", "Gaji Pokok", TransactionType.INCOME, "payments", 0xFF10B981),
        CategoryItem("cat_bonus", "Bonus / THR", TransactionType.INCOME, "redeem", 0xFFF59E0B),
        CategoryItem("cat_business", "Bisnis & Usaha", TransactionType.INCOME, "trending_up", 0xFF06B6D4),
        CategoryItem("cat_investment", "Investasi", TransactionType.INCOME, "account_balance", 0xFF6366F1),
        CategoryItem("cat_other_inc", "Pemasukan Lain", TransactionType.INCOME, "savings", 0xFF8B5CF6)
    )

    fun getById(id: String): CategoryItem? {
        return list.find { it.id == id }
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val categoryId: String,
    val categoryName: String,
    val note: String,
    val paymentMethod: String,
    val timestamp: Long,
    val dateString: String,   // "yyyy-MM-dd"
    val monthString: String  // "yyyy-MM"
)

@Entity(tableName = "category_budgets", primaryKeys = ["categoryId", "monthString"])
data class CategoryBudgetEntity(
    val categoryId: String,
    val monthString: String, // "yyyy-MM"
    val monthlyLimit: Double
)

data class CategorySummary(
    val categoryId: String,
    val categoryName: String,
    val type: TransactionType,
    val totalAmount: Double,
    val transactionCount: Int,
    val budgetLimit: Double = 0.0,
    val colorHex: Long = 0xFF10B981,
    val iconKey: String = "shopping_cart"
)

data class MonthlyFinanceSummary(
    val monthString: String,
    val displayMonth: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val categoryBreakdown: List<CategorySummary>,
    val dailyExpenses: Map<String, Double>
)
