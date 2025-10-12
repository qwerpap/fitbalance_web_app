package com.example.features.calculator

import kotlinx.serialization.Serializable

@Serializable
data class CalculationResultDTO(
    val id: String,
    val userId: String? = null,  // Делаем nullable для анонимных расчетов
    val tdee: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val recommendedCalories: Double,
    val createdAt: Long = System.currentTimeMillis()  // Временная метка в миллисекундах
)