package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CategoryVisuals {
    fun getIconForCategory(categoryName: String): ImageVector {
        return when (categoryName.lowercase().trim()) {
            "food" -> Icons.Default.Restaurant
            "transport" -> Icons.Default.DirectionsCar
            "housing" -> Icons.Default.Home
            "utilities" -> Icons.Default.FlashOn
            "entertainment" -> Icons.Default.Movie
            "health" -> Icons.Default.LocalHospital
            "shopping" -> Icons.Default.ShoppingBag
            "education" -> Icons.Default.School
            "savings" -> Icons.Default.Savings
            "salary" -> Icons.Default.Payments
            "freelance" -> Icons.Default.Work
            "investment" -> Icons.Default.AttachMoney
            else -> Icons.Default.Category
        }
    }

    fun getColorForCategory(categoryName: String): Color {
        return when (categoryName.lowercase().trim()) {
            "food" -> Color(0xFFEF4444)
            "transport" -> Color(0xFF3B82F6)
            "housing" -> Color(0xFF8B5CF6)
            "utilities" -> Color(0xFFF59E0B)
            "entertainment" -> Color(0xFFEC4899)
            "health" -> Color(0xFF10B981)
            "shopping" -> Color(0xFF06B6D4)
            "education" -> Color(0xFF6366F1)
            "savings" -> Color(0xFF14B8A6)
            "salary" -> Color(0xFF10B981)
            "freelance" -> Color(0xFF3B82F6)
            "investment" -> Color(0xFF8B5CF6)
            else -> Color(0xFF64748B)
        }
    }
}

@Composable
fun CategoryIconBadge(
    categoryName: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp
) {
    val tint = CategoryVisuals.getColorForCategory(categoryName)
    val icon = CategoryVisuals.getIconForCategory(categoryName)

    Box(
        modifier = modifier
            .size(size)
            .background(tint.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = categoryName,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
