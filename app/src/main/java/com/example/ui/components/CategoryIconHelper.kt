package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    fun getIcon(key: String): ImageVector {
        return when (key) {
            "shopping_cart" -> Icons.Default.ShoppingCart
            "restaurant" -> Icons.Default.Restaurant
            "directions_car" -> Icons.Default.DirectionsCar
            "storefront" -> Icons.Default.Store
            "receipt_long" -> Icons.Default.Receipt
            "movie" -> Icons.Default.Movie
            "medical_services" -> Icons.Default.LocalHospital
            "school" -> Icons.Default.School
            "home" -> Icons.Default.Home
            "payments" -> Icons.Default.Payments
            "redeem" -> Icons.Default.Redeem
            "trending_up" -> Icons.Default.TrendingUp
            "account_balance" -> Icons.Default.AccountBalance
            "savings" -> Icons.Default.Savings
            else -> Icons.Default.Category
        }
    }
}
