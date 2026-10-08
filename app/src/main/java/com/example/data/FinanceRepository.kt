package com.example.data

import com.example.model.CategoryBudgetEntity
import com.example.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    val availableMonths: Flow<List<String>> = dao.getAvailableMonths()

    fun getTransactionsByMonth(month: String): Flow<List<TransactionEntity>> {
        return dao.getTransactionsByMonth(month)
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? {
        return dao.getTransactionById(id)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        dao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        dao.deleteTransactionById(id)
    }

    fun getBudgetsForMonth(month: String): Flow<List<CategoryBudgetEntity>> {
        return dao.getBudgetsForMonth(month)
    }

    suspend fun setBudget(categoryId: String, month: String, amount: Double) {
        dao.insertOrUpdateBudget(CategoryBudgetEntity(categoryId, month, amount))
    }

    suspend fun deleteBudget(categoryId: String, month: String) {
        dao.deleteBudget(categoryId, month)
    }
}
